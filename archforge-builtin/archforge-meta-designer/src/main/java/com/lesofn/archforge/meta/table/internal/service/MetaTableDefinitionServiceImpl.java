package com.lesofn.archforge.meta.table.internal.service;

import com.lesofn.archforge.meta.table.api.dao.MetaColumnRepository;
import com.lesofn.archforge.meta.table.api.dao.MetaTableRepository;
import com.lesofn.archforge.meta.table.api.definition.ColumnDefinition;
import com.lesofn.archforge.meta.table.api.definition.DefinitionSource;
import com.lesofn.archforge.meta.table.api.definition.FsDefinitionSource;
import com.lesofn.archforge.meta.table.api.definition.MetaTableDefinitionCodec;
import com.lesofn.archforge.meta.table.api.definition.TableDefinition;
import com.lesofn.archforge.meta.table.api.domain.MetaColumn;
import com.lesofn.archforge.meta.table.api.domain.MetaTable;
import com.lesofn.archforge.meta.table.api.errors.MetaTableException;
import com.lesofn.archforge.meta.table.api.service.MetaTableDefinitionService;
import com.lesofn.archforge.meta.table.internal.validator.MetaTableValidator;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * {@link MetaTableDefinitionService} over the meta repositories. Applying
 * mutates the JPA-managed rows in place (dirty checking writes only what
 * changed — audit fields and {@code version} survive), then validates every
 * touched table with the designer's {@link MetaTableValidator}; any failure
 * rolls the whole sync back.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MetaTableDefinitionServiceImpl implements MetaTableDefinitionService {

    private final MetaTableRepository tableRepository;
    private final MetaColumnRepository columnRepository;
    private final MetaTableValidator validator;
    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    @Transactional(readOnly = true)
    public List<Path> exportTo(Path dir, @Nullable String tableCode) {
        List<MetaTable> tables = tableCode == null
                ? tableRepository.findAllByDeletedFalse()
                : tableRepository.findByTableCodeAndDeletedFalse(tableCode)
                        .map(List::of).orElse(List.of());
        List<Path> written = new ArrayList<>();
        try {
            Files.createDirectories(dir);
            for (MetaTable table : tables) {
                List<MetaColumn> columns = columnRepository.findByTableIdAndDeletedFalseOrderBySortAsc(
                        Objects.requireNonNull(table.getId()));
                TableDefinition definition = MetaTableDefinitionCodec.toDefinition(table, columns);
                // Schema hint gives IDE completion/validation in any yaml-aware editor.
                String yaml = "# yaml-language-server: $schema=../../spec/schemas/meta/table.schema.json\n" +
                        MetaTableDefinitionCodec.toYaml(definition);

                // Fidelity self-check: re-read what we would write and compare.
                TableDefinition roundTripped = MetaTableDefinitionCodec.fromYaml(yaml);
                if (!roundTripped.equals(definition)) {
                    throw new IllegalStateException("export fidelity check failed for " + table.getTableCode());
                }

                Path file = dir.resolve(table.getTableCode() + ".yaml");
                Files.writeString(file, yaml, StandardCharsets.UTF_8);
                written.add(file);
                log.info("exported {} -> {}", table.getTableCode(), file);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return written;
    }

    @Override
    @Transactional
    public SyncReport syncFrom(Path dir, @Nullable String tableCode, boolean apply) {
        return syncFrom(new FsDefinitionSource(dir), tableCode, apply);
    }

    @Override
    @Transactional
    public SyncReport syncFrom(DefinitionSource source, @Nullable String tableCode, boolean apply) {
        Map<String, TableDefinition> definitions = loadDefinitions(source, tableCode);
        Diff diff = new Diff();
        List<MetaTable> touched = new ArrayList<>();
        for (TableDefinition def : definitions.values()) {
            MetaTable existing = tableRepository.findByTableCodeAndDeletedFalse(def.getTableCode()).orElse(null);
            if (existing == null) {
                diff.createdTables.add(def.getTableCode());
                def.getColumns().forEach(c -> diff.newColumns.add(def.getTableCode() + "." + c.getColumnCode()));
                if (apply) {
                    touched.add(create(def));
                }
            } else if (diffExisting(def, existing, diff, apply) && apply) {
                touched.add(existing);
            }
        }
        if (tableCode == null) {
            tableRepository.findAllByDeletedFalse().stream()
                    .map(MetaTable::getTableCode)
                    .filter(code -> !definitions.containsKey(code))
                    .sorted()
                    .forEach(diff.orphanTables::add);
        }
        if (apply) {
            validate(touched);
        }
        return diff.report();
    }

    @Override
    @Transactional
    public SyncReport materialize(DefinitionSource source, Duration lockTimeout) {
        JdbcOperations jdbc = jdbcTemplate.getJdbcOperations();
        // Transaction-local (SET LOCAL semantics); a non-positive value would mean "wait forever".
        jdbc.queryForObject("SELECT set_config('lock_timeout', ?, true)", String.class,
                Math.max(1L, lockTimeout.toMillis()) + "ms");
        jdbc.queryForList("SELECT pg_advisory_xact_lock(?)", APPLY_LOCK_KEY);
        return syncFrom(source, null, true);
    }

    /** Diffs one live table against its file; mutates the managed rows when applying. Returns "anything changed". */
    private boolean diffExisting(TableDefinition def, MetaTable existing, Diff diff, boolean apply) {
        String code = def.getTableCode();
        boolean dirty = false;
        if (!MetaTableDefinitionCodec.matches(def, MetaTableDefinitionCodec.toDefinition(existing, List.of()))) {
            diff.updatedTables.add(code);
            dirty = true;
            if (apply) {
                MetaTableDefinitionCodec.applyTo(def, existing);
            }
        }

        long tableId = Objects.requireNonNull(existing.getId());
        Map<String, MetaColumn> dbColumns = new LinkedHashMap<>();
        columnRepository.findByTableIdAndDeletedFalseOrderBySortAsc(tableId)
                .forEach(c -> dbColumns.put(c.getColumnCode(), c));
        Set<String> fileCodes = new HashSet<>();
        for (ColumnDefinition fileColumn : def.getColumns()) {
            fileCodes.add(fileColumn.getColumnCode());
            MetaColumn dbColumn = dbColumns.get(fileColumn.getColumnCode());
            String key = code + "." + fileColumn.getColumnCode();
            if (dbColumn == null) {
                diff.newColumns.add(key);
                dirty = true;
                if (apply) {
                    insertColumn(fileColumn, tableId);
                }
            } else if (!MetaTableDefinitionCodec.matches(fileColumn, MetaTableDefinitionCodec.toDefinition(dbColumn))) {
                diff.changedColumns.add(key);
                dirty = true;
                if (apply) {
                    MetaTableDefinitionCodec.applyTo(fileColumn, dbColumn);
                }
            }
        }

        List<String> removals = def.getRemovedColumns() == null ? List.of() : def.getRemovedColumns();
        for (MetaColumn dbColumn : dbColumns.values()) {
            if (fileCodes.contains(dbColumn.getColumnCode())) {
                continue;
            }
            String key = code + "." + dbColumn.getColumnCode();
            if (removals.contains(dbColumn.getColumnCode())) {
                diff.removedColumns.add(key);
                dirty = true;
                if (apply) {
                    dbColumn.setDeleted(true);
                }
            } else {
                diff.orphanColumns.add(key);
            }
        }
        return dirty;
    }

    /** New table — reviving a soft-deleted row: {@code table_code} is globally unique (V9). */
    private MetaTable create(TableDefinition def) {
        MetaTable table = tableRepository.findByTableCode(def.getTableCode()).orElseGet(MetaTable::new);
        table.setDeleted(false);
        MetaTableDefinitionCodec.applyTo(def, table);
        MetaTable saved = tableRepository.save(table);
        long tableId = Objects.requireNonNull(saved.getId());
        def.getColumns().forEach(c -> insertColumn(c, tableId));
        return saved;
    }

    private void insertColumn(ColumnDefinition def, long tableId) {
        MetaColumn column = new MetaColumn();
        MetaTableDefinitionCodec.applyTo(def, column);
        column.setTableId(tableId);
        columnRepository.save(column);
    }

    /**
     * After every file is written (cross-file references resolve), each touched
     * table must pass the same validation the designer applies.
     */
    private void validate(List<MetaTable> touched) {
        for (MetaTable table : touched) {
            List<MetaColumn> columns = columnRepository.findByTableIdAndDeletedFalseOrderBySortAsc(
                    Objects.requireNonNull(table.getId()));
            try {
                validator.validate(table, columns);
            } catch (MetaTableException e) {
                throw new IllegalStateException("definition " + table.getTableCode() + " failed validation: " +
                        e.getMessage(), e);
            }
        }
    }

    private static Map<String, TableDefinition> loadDefinitions(DefinitionSource source, @Nullable String tableCode) {
        Map<String, TableDefinition> definitions = new LinkedHashMap<>();
        Map<String, String> fileOf = new LinkedHashMap<>();
        for (Map.Entry<String, String> doc : source.load().entrySet()) {
            TableDefinition def = parse(doc.getKey(), doc.getValue());
            String previous = fileOf.putIfAbsent(def.getTableCode(), doc.getKey());
            if (previous != null) {
                throw new IllegalStateException("tableCode " + def.getTableCode() + " is defined by both " + previous +
                        " and " + doc.getKey());
            }
            if (tableCode == null || tableCode.equals(def.getTableCode())) {
                definitions.put(def.getTableCode(), def);
            }
        }
        if (tableCode != null && !definitions.containsKey(tableCode)) {
            throw new IllegalStateException("no definition file for tableCode=" + tableCode);
        }
        return definitions;
    }

    private static TableDefinition parse(String fileName, String yaml) {
        try {
            return MetaTableDefinitionCodec.fromYaml(yaml);
        } catch (RuntimeException e) {
            throw new IllegalStateException("invalid definition file " + fileName + ": " + e.getMessage(), e);
        }
    }

    /** Mutable accumulator behind the immutable {@link SyncReport}. */
    private static final class Diff {
        final List<String> createdTables = new ArrayList<>();
        final List<String> updatedTables = new ArrayList<>();
        final List<String> newColumns = new ArrayList<>();
        final List<String> changedColumns = new ArrayList<>();
        final List<String> orphanColumns = new ArrayList<>();
        final List<String> removedColumns = new ArrayList<>();
        final List<String> orphanTables = new ArrayList<>();

        SyncReport report() {
            return new SyncReport(List.copyOf(createdTables), List.copyOf(updatedTables), List.copyOf(newColumns), List.copyOf(
                    changedColumns), List.copyOf(orphanColumns), List.copyOf(removedColumns), List.copyOf(orphanTables));
        }
    }
}
