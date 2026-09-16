package com.lesofn.archforge.meta.table.api.service;

import com.lesofn.archforge.meta.table.api.dao.MetaColumnRepository;
import com.lesofn.archforge.meta.table.api.dao.MetaTableRepository;
import com.lesofn.archforge.meta.table.api.definition.ColumnDefinition;
import com.lesofn.archforge.meta.table.api.definition.MetaTableDefinitionCodec;
import com.lesofn.archforge.meta.table.api.definition.TableDefinition;
import com.lesofn.archforge.meta.table.api.domain.MetaColumn;
import com.lesofn.archforge.meta.table.api.domain.MetaTable;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * DB ↔ YAML synchronization for meta-table definitions
 * ({@code archforge/meta/<tableCode>.yaml}). Export is fidelity-checked;
 * import is dry-run by default and never deletes columns implicitly —
 * {@code removedColumns} in the file is the explicit delete.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MetaTableDefinitionService {

    private final MetaTableRepository tableRepository;
    private final MetaColumnRepository columnRepository;

    /** What one import pass would change — populated identically in dry-run and apply modes. */
    public record SyncReport(
            List<String> createdTables,
            List<String> updatedTables,
            List<String> newColumns,
            List<String> changedColumns,
            List<String> orphanColumns,
            List<String> removedColumns) {

        public boolean isEmpty() {
            return createdTables.isEmpty() && updatedTables.isEmpty() && newColumns.isEmpty() && changedColumns.isEmpty() &&
                    orphanColumns.isEmpty() && removedColumns.isEmpty();
        }

        public List<String> lines() {
            List<String> out = new ArrayList<>();
            createdTables.forEach(c -> out.add("+ table " + c));
            updatedTables.forEach(c -> out.add("~ table " + c));
            newColumns.forEach(c -> out.add("+ column " + c));
            changedColumns.forEach(c -> out.add("~ column " + c));
            orphanColumns.forEach(c -> out.add("! orphan " + c + " (in DB, absent from file — kept)"));
            removedColumns.forEach(c -> out.add("- column " + c + " (removedColumns — soft-deleted)"));
            return out;
        }
    }

    /** Export enabled tables to {@code <dir>/<tableCode>.yaml}; returns written files. */
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
                List<MetaColumn> columns = columnRepository.findByTableIdAndDeletedFalseOrderBySortAsc(java.util.Objects
                        .requireNonNull(table.getId()));
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

    /**
     * Import {@code <dir>/*.yaml} into the DB. Dry-run by default; pass
     * {@code apply=true} to write. Columns absent from the file are reported as
     * orphans and kept — deletion requires {@code removedColumns} in the file.
     */
    @Transactional
    public SyncReport syncFrom(Path dir, @Nullable String tableCode, boolean apply) {
        Map<String, TableDefinition> definitions = loadDefinitions(dir, tableCode);
        List<String> createdTables = new ArrayList<>();
        List<String> updatedTables = new ArrayList<>();
        List<String> newColumns = new ArrayList<>();
        List<String> changedColumns = new ArrayList<>();
        List<String> orphanColumns = new ArrayList<>();
        List<String> removedColumns = new ArrayList<>();

        for (TableDefinition def : definitions.values()) {
            MetaTable existing = tableRepository
                    .findByTableCodeAndDeletedFalse(def.getTableCode())
                    .orElse(null);

            if (existing == null) {
                createdTables.add(def.getTableCode());
                def.getColumns().forEach(c -> newColumns.add(def.getTableCode() + "." + c.getColumnCode()));
                if (apply) {
                    MetaTable table = MetaTableDefinitionCodec.toTableEntity(def);
                    table = tableRepository.save(table);
                    for (MetaColumn column : MetaTableDefinitionCodec.toColumnEntities(def)) {
                        column.setTableId(table.getId());
                        columnRepository.save(column);
                    }
                }
                continue;
            }

            TableDefinition current = MetaTableDefinitionCodec.toDefinition(
                    existing, columnRepository.findByTableIdAndDeletedFalseOrderBySortAsc(java.util.Objects.requireNonNull(
                            existing.getId())));
            if (!sameTableFields(current, def)) {
                updatedTables.add(def.getTableCode());
            }

            Map<String, ColumnDefinition> fileColumns = new LinkedHashMap<>();
            for (ColumnDefinition c : def.getColumns()) {
                fileColumns.put(c.getColumnCode(), c);
            }
            Map<String, ColumnDefinition> dbColumns = new LinkedHashMap<>();
            for (ColumnDefinition c : current.getColumns()) {
                dbColumns.put(c.getColumnCode(), c);
            }

            for (ColumnDefinition fileCol : def.getColumns()) {
                String key = def.getTableCode() + "." + fileCol.getColumnCode();
                if (!dbColumns.containsKey(fileCol.getColumnCode())) {
                    newColumns.add(key);
                } else if (!dbColumns.get(fileCol.getColumnCode()).equals(fileCol)) {
                    changedColumns.add(key);
                }
            }
            List<String> removed = def.getRemovedColumns();
            for (String code : dbColumns.keySet()) {
                if (!fileColumns.containsKey(code)) {
                    if (removed != null && removed.contains(code)) {
                        removedColumns.add(def.getTableCode() + "." + code);
                    } else {
                        orphanColumns.add(def.getTableCode() + "." + code);
                    }
                }
            }

            if (apply) {
                applyUpdate(existing, def, fileColumns, dbColumns, removedColumns);
            }
        }
        return new SyncReport(createdTables, updatedTables, newColumns, changedColumns, orphanColumns, removedColumns);
    }

    private void applyUpdate(
            MetaTable existing,
            TableDefinition def,
            Map<String, ColumnDefinition> fileColumns,
            Map<String, ColumnDefinition> dbColumns,
            List<String> removedColumns) {
        MetaTable updated = MetaTableDefinitionCodec.toTableEntity(def);
        updated.setId(existing.getId());
        updated.setVersion(existing.getVersion());
        tableRepository.save(updated);

        List<MetaColumn> dbEntities = columnRepository.findByTableIdAndDeletedFalseOrderBySortAsc(java.util.Objects
                .requireNonNull(existing.getId()));
        Map<String, MetaColumn> dbEntityByCode = new LinkedHashMap<>();
        for (MetaColumn c : dbEntities) {
            dbEntityByCode.put(c.getColumnCode(), c);
        }

        List<MetaColumn> fresh = MetaTableDefinitionCodec.toColumnEntities(def);
        for (MetaColumn column : fresh) {
            MetaColumn prior = dbEntityByCode.get(column.getColumnCode());
            if (prior != null) {
                column.setId(prior.getId());
            }
            column.setTableId(existing.getId());
            columnRepository.save(column);
        }

        for (Map.Entry<String, MetaColumn> entry : dbEntityByCode.entrySet()) {
            if (!fileColumns.containsKey(entry.getKey()) && removedColumns.contains(existing.getTableCode() + "." + entry
                    .getKey())) {
                MetaColumn orphan = entry.getValue();
                orphan.setDeleted(true);
                columnRepository.save(orphan);
            }
        }
    }

    private static boolean sameTableFields(TableDefinition a, TableDefinition b) {
        return Objects.equals(a.getTableCode(), b.getTableCode()) && Objects.equals(a.getTableName(), b.getTableName()) &&
                Objects.equals(a.getDescription(), b.getDescription()) && Objects.equals(a.getTablePrefix(), b
                        .getTablePrefix()) && Objects.equals(a.getStatus(), b.getStatus()) && Objects.equals(a
                                .getSchemaVersion(), b.getSchemaVersion());
    }

    private static Map<String, TableDefinition> loadDefinitions(Path dir, @Nullable String tableCode) {
        if (!Files.isDirectory(dir)) {
            throw new IllegalStateException("definition directory not found: " + dir);
        }
        Map<String, TableDefinition> definitions = new LinkedHashMap<>();
        try (Stream<Path> files = Files.list(dir)) {
            for (Path file : files
                    .filter(f -> f.toString().endsWith(".yaml"))
                    .sorted()
                    .toList()) {
                TableDefinition def = MetaTableDefinitionCodec.fromYaml(Files.readString(file, StandardCharsets.UTF_8));
                if (tableCode == null || tableCode.equals(def.getTableCode())) {
                    definitions.put(def.getTableCode(), def);
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        if (tableCode != null && !definitions.containsKey(tableCode)) {
            throw new IllegalStateException("no definition file for tableCode=" + tableCode + " in " + dir);
        }
        return definitions;
    }
}
