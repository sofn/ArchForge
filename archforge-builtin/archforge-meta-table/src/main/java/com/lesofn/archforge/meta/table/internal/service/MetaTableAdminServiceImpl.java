package com.lesofn.archforge.meta.table.internal.service;

import static com.lesofn.archforge.meta.table.api.errors.MetaTableErrorCode.META_TABLE_CODE_EXISTS;
import static com.lesofn.archforge.meta.table.api.errors.MetaTableErrorCode.META_TABLE_COLUMNS_REQUIRED;
import static com.lesofn.archforge.meta.table.api.errors.MetaTableErrorCode.META_TABLE_CONCURRENT_MODIFY;
import static com.lesofn.archforge.meta.table.api.errors.MetaTableErrorCode.META_TABLE_EVOLUTION_INVALID;
import static com.lesofn.archforge.meta.table.api.errors.MetaTableErrorCode.META_TABLE_HAS_DATA;
import static com.lesofn.archforge.meta.table.api.errors.MetaTableErrorCode.META_TABLE_NOT_EXISTS;

import com.lesofn.archforge.meta.table.api.dao.MetaColumnRepository;
import com.lesofn.archforge.meta.table.api.dao.MetaTableRepository;
import com.lesofn.archforge.meta.table.api.domain.MetaColumn;
import com.lesofn.archforge.meta.table.api.domain.MetaTable;
import com.lesofn.archforge.meta.table.api.domain.MetaTableMigration;
import com.lesofn.archforge.meta.table.api.dto.SchemaPreview;
import com.lesofn.archforge.meta.table.api.errors.MetaTableException;
import com.lesofn.archforge.meta.table.api.service.MetaTableAdminService;
import com.lesofn.archforge.meta.table.api.service.MetaTableMigrationService;
import com.lesofn.archforge.meta.table.internal.ddl.AlterTableDdlGenerator;
import com.lesofn.archforge.meta.table.internal.ddl.MetaTableDdlGenerator;
import com.lesofn.archforge.meta.table.internal.ddl.SchemaDdl;
import com.lesofn.archforge.meta.table.internal.schema.SchemaChange;
import com.lesofn.archforge.meta.table.internal.schema.SchemaChangeType;
import com.lesofn.archforge.meta.table.internal.schema.SchemaDiffEngine;
import com.lesofn.archforge.meta.table.internal.validator.MetaTableValidator;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 元表格定义管理服务实现。
 */
@Service
@RequiredArgsConstructor
public class MetaTableAdminServiceImpl implements MetaTableAdminService {

    private final MetaTableRepository metaTableRepository;
    private final MetaColumnRepository metaColumnRepository;
    private final MetaTableDdlGenerator ddlGenerator;
    private final MetaTableValidator validator;
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final SchemaDiffEngine schemaDiffEngine;
    private final AlterTableDdlGenerator alterTableDdlGenerator;
    private final MetaTableMigrationService migrationService;

    @Override
    @Transactional
    public Long create(MetaTable table, List<MetaColumn> columns) {
        if (metaTableRepository.existsByTableCodeAndDeletedFalse(table.getTableCode())) {
            throw new MetaTableException(META_TABLE_CODE_EXISTS);
        }
        validator.validate(table, columns);
        if (table.getTablePrefix() == null || table.getTablePrefix().isEmpty()) {
            table.setTablePrefix("meta_");
        }
        table.setStatus(1);
        table.setSchemaVersion(1);
        if (table.getUpdaterId() == null && table.getCreatorId() != null) {
            table.setUpdaterId(table.getCreatorId());
        }
        if (table.getUpdateTime() == null) {
            table.setUpdateTime(LocalDateTime.now(ZoneId.systemDefault()));
        }
        MetaTable saved = metaTableRepository.save(table);

        for (MetaColumn column : columns) {
            column.setTableId(java.util.Objects.requireNonNull(saved.getId()));
        }
        metaColumnRepository.saveAll(columns);

        MetaTableDdlGenerator.DdlResult ddl = ddlGenerator.generateCreateTable(saved, columns);
        jdbcTemplate.getJdbcOperations().execute(ddl.createTableSql());
        ddl.indexSqls().forEach(sql -> jdbcTemplate.getJdbcOperations().execute(sql));

        return java.util.Objects.requireNonNull(saved.getId());
    }

    @Override
    @Transactional
    public void updateMeta(Long id, MetaTable table, Long operatorId) {
        MetaTable existing = findById(id);
        existing.setUpdaterId(operatorId);
        existing.setTableName(table.getTableName());
        existing.setDescription(table.getDescription());
        if (table.getStatus() != null) {
            existing.setStatus(table.getStatus());
        }
        saveGuarded(existing);
    }

    @Override
    @Transactional
    public void update(Long id, MetaTable table, List<MetaColumn> columns, Long operatorId) {
        if (columns == null || columns.isEmpty()) {
            throw new MetaTableException(META_TABLE_COLUMNS_REQUIRED);
        }
        MetaTable existing = findById(id);
        existing.setUpdaterId(operatorId);

        validator.validate(existing, columns);

        List<MetaColumn> oldColumns = findColumns(id);
        List<SchemaChange> changes = schemaDiffEngine.diff(existing, oldColumns, columns);
        if (changes.isEmpty()) {
            existing.setTableName(table.getTableName());
            existing.setDescription(table.getDescription());
            if (table.getStatus() != null) {
                existing.setStatus(table.getStatus());
            }
            saveGuarded(existing);
            return;
        }

        List<SchemaDdl> ddlStatements = alterTableDdlGenerator.generate(existing, changes);

        int currentVersion = existing.getSchemaVersion() == null ? 1 : existing.getSchemaVersion();
        int nextVersion = currentVersion + 1;

        List<MetaTableMigration> records = migrationService.saveAll(
                buildPendingMigrations(existing, nextVersion, ddlStatements, operatorId));

        for (SchemaDdl ddl : ddlStatements) {
            executeWithPreflight(existing, ddl);
        }

        existing.setSchemaVersion(nextVersion);
        existing.setTableName(table.getTableName());
        existing.setDescription(table.getDescription());
        if (table.getStatus() != null) {
            existing.setStatus(table.getStatus());
        }
        saveGuarded(existing);

        // 软删除被 DROP 的字段
        for (MetaColumn old : oldColumns) {
            if (changes.stream().anyMatch(c -> c.getType() == SchemaChangeType.DROP_COLUMN && old.getId() != null && Objects
                    .equals(old.getId(), c.getOldColumn().getId()))) {
                old.setDeleted(true);
                metaColumnRepository.save(old);
            }
        }

        for (MetaColumn col : columns) {
            if (col.getTableId() == null) {
                col.setTableId(id);
            }
        }
        metaColumnRepository.saveAll(columns);

        LocalDateTime executedAt = LocalDateTime.now(ZoneId.systemDefault());
        records.forEach(r -> {
            r.setStatus("APPLIED");
            r.setExecutedAt(executedAt);
        });
        migrationService.saveAll(records);
    }

    /**
     * 与 {@link #update} 同一条 diff + preflight 路径，但只读：
     * violation 计数照常执行（SELECT COUNT），DDL 只生成不执行。
     */
    @Override
    public SchemaPreview previewSchema(Long id, MetaTable table, @Nullable List<MetaColumn> columns) {
        MetaTable existing = findById(id);
        SchemaPreview preview = new SchemaPreview();
        if (columns == null || columns.isEmpty()) {
            return preview;
        }
        validator.validate(existing, columns);
        List<MetaColumn> oldColumns = findColumns(id);
        List<SchemaChange> changes = schemaDiffEngine.diff(existing, oldColumns, columns);
        List<SchemaDdl> ddlStatements = alterTableDdlGenerator.generate(existing, changes);

        for (SchemaDdl ddl : ddlStatements) {
            SchemaChange change = ddl.change();
            SchemaPreview.PreviewChange item = new SchemaPreview.PreviewChange();
            item.setType(change.getType().name());
            item.setColumnCode(resolveChangeColumnCode(change));
            if (change.getType() == SchemaChangeType.RENAME_COLUMN && change.getOldColumn() != null) {
                item.setOldColumnCode(change.getOldColumn().getColumnCode());
            }
            item.setOldType(change.getOldType());
            item.setNewType(change.getNewType());
            item.setOldDefault(change.getOldDefault());
            item.setNewDefault(change.getNewDefault());
            if (change.getType() == SchemaChangeType.ALTER_NULL && change.getNewColumn() != null) {
                item.setOldNullable(change.getOldColumn() == null || change.getOldColumn().isNullableColumn());
                item.setNewNullable(change.getNewColumn().isNullableColumn());
            }

            long violations = alterTableDdlGenerator.buildViolationCountSql(existing, change)
                    .map(this::countViolations)
                    .orElse(0L);
            item.setViolations(violations);
            boolean backfillable = alterTableDdlGenerator.buildBackfillUpdateSql(existing, change).isPresent();
            item.setAction(violations == 0 ? "NONE" : backfillable ? "BACKFILL" : "BLOCKED");
            item.setDdl(ddl.sqls());
            preview.getChanges().add(item);

            if (violations > 0 || isDangerousType(change.getType())) {
                preview.setDangerous(true);
            }
        }
        return preview;
    }

    private static boolean isDangerousType(SchemaChangeType type) {
        return switch (type) {
            case DROP_COLUMN, RENAME_COLUMN, ALTER_TYPE -> true;
            default -> false;
        };
    }

    private static @Nullable String resolveChangeColumnCode(SchemaChange change) {
        if (change.getNewIndexGroup() != null) {
            return change.getNewIndexGroup();
        }
        if (change.getOldIndexGroup() != null) {
            return change.getOldIndexGroup();
        }
        if (change.getNewColumn() != null) {
            return change.getNewColumn().getColumnCode();
        }
        return change.getOldColumn() != null ? change.getOldColumn().getColumnCode() : null;
    }

    @Override
    @Transactional
    public Long copy(Long id) {
        MetaTable source = findById(id);
        List<MetaColumn> sourceColumns = findColumns(id);

        String newCode = generateCopyCode(source.getTableCode());
        MetaTable clone = new MetaTable();
        clone.setTableCode(newCode);
        clone.setTableName(source.getTableName() + "_副本");
        clone.setDescription(source.getDescription());
        clone.setTablePrefix(source.getTablePrefix());
        clone.setStatus(1);

        List<MetaColumn> cloneColumns = sourceColumns.stream().map(this::copyColumn).toList();
        return create(clone, cloneColumns);
    }

    @Override
    public MetaTable findById(Long id) {
        return metaTableRepository.findById(id)
                .filter(t -> !Boolean.TRUE.equals(t.getDeleted()))
                .orElseThrow(() -> new MetaTableException(META_TABLE_NOT_EXISTS));
    }

    @Override
    public List<MetaColumn> findColumns(Long tableId) {
        return metaColumnRepository.findByTableIdAndDeletedFalseOrderBySortAsc(tableId);
    }

    @Override
    public Page<MetaTable> list(String keyword, Pageable pageable) {
        Specification<MetaTable> spec = (root, query, cb) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("deleted"), false));
            if (StringUtils.hasText(keyword)) {
                String like = "%" + keyword + "%";
                predicates.add(cb.or(
                        cb.like(root.get("tableCode"), like, '!'),
                        cb.like(root.get("tableName"), like, '!')));
            }
            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
        return metaTableRepository.findAll(spec, pageable);
    }

    @Override
    public long checkDelete(Long id) {
        MetaTable table = findById(id);
        Long count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM " + quotePhysical(table) + " WHERE deleted = 0",
                Map.of(),
                Long.class);
        return count == null ? 0L : count;
    }

    @Override
    @Transactional
    public void delete(Long id, boolean force) {
        MetaTable table = findById(id);
        long dataCount = checkDelete(id);
        if (dataCount > 0 && !force) {
            throw new MetaTableException(META_TABLE_HAS_DATA, dataCount);
        }

        jdbcTemplate.getJdbcOperations().execute(ddlGenerator.generateDropTable(table.physicalTableName()));

        // 软删列与表（物理表已 DROP）：硬删会撞 sys_meta_table_migration 的外键
        List<MetaColumn> columns = metaColumnRepository.findByTableIdAndDeletedFalseOrderBySortAsc(id);
        columns.forEach(c -> c.setDeleted(true));
        metaColumnRepository.saveAll(columns);
        table.setDeleted(true);
        saveGuarded(table);
    }

    private void saveGuarded(MetaTable table) {
        try {
            metaTableRepository.saveAndFlush(table);
        } catch (OptimisticLockingFailureException e) {
            throw new MetaTableException(META_TABLE_CONCURRENT_MODIFY);
        }
    }

    private void executeWithPreflight(MetaTable table, SchemaDdl ddl) {
        Optional<String> violationSql = alterTableDdlGenerator.buildViolationCountSql(table, ddl.change());
        long violations = violationSql.map(this::countViolations).orElse(0L);
        if (violations > 0) {
            Optional<String> backfillSql = alterTableDdlGenerator.buildBackfillUpdateSql(table, ddl.change());
            if (backfillSql.isPresent()) {
                jdbcTemplate.getJdbcOperations().execute(backfillSql.get());
            } else {
                throw new MetaTableException(META_TABLE_EVOLUTION_INVALID, describeEvolutionBlocker(table, ddl.change(),
                        violations));
            }
        }
        for (String sql : ddl.sqls()) {
            jdbcTemplate.getJdbcOperations().execute(sql);
        }
    }

    private long countViolations(String sql) {
        try {
            Long count = jdbcTemplate.getJdbcOperations().queryForObject(sql, Long.class);
            return count == null ? 0L : count;
        } catch (DataAccessException e) {
            throw new MetaTableException(META_TABLE_EVOLUTION_INVALID, "存在无法转换的存量数据: " + e.getMostSpecificCause().getMessage());
        }
    }

    private String describeEvolutionBlocker(MetaTable table, SchemaChange change, long violations) {
        String columnCode = change.getNewColumn() != null
                ? change.getNewColumn().getColumnCode()
                : change.getOldColumn().getColumnCode();
        return "表 " + table.physicalTableName() + " 字段 " + columnCode + " 有 " + violations + " 行数据不满足变更要求";
    }

    private String generateCopyCode(String originalCode) {
        String base = originalCode + "_copy";
        String code = base;
        int suffix = 2;
        while (metaTableRepository.existsByTableCodeAndDeletedFalse(code)) {
            code = base + suffix;
            suffix++;
        }
        return code;
    }

    private MetaColumn copyColumn(MetaColumn source) {
        MetaColumn copy = new MetaColumn();
        copy.setColumnCode(source.getColumnCode());
        copy.setColumnName(source.getColumnName());
        copy.setDataType(source.getDataType());
        copy.setLength(source.getLength());
        copy.setPrecision(source.getPrecision());
        copy.setScale(source.getScale());
        copy.setNullable(source.getNullable());
        copy.setDefaultValue(source.getDefaultValue());
        copy.setUnique(source.getUnique());
        copy.setRequired(source.getRequired());
        copy.setSearchable(source.getSearchable());
        copy.setListVisible(source.getListVisible());
        copy.setIndex(source.getIndex());
        copy.setIndexType(source.getIndexType());
        copy.setIndexGroup(source.getIndexGroup());
        copy.setSort(source.getSort());
        copy.setArrayElementType(source.getArrayElementType());
        copy.setSearchType(source.getSearchType());
        copy.setOptions(source.getOptions());
        copy.setDictCode(source.getDictCode());
        copy.setReferenceTable(source.getReferenceTable());
        copy.setReferenceColumn(source.getReferenceColumn());
        copy.setDisplayExpression(source.getDisplayExpression());
        copy.setTenantColumn(source.getTenantColumn());
        copy.setOwnerColumn(source.getOwnerColumn());
        return copy;
    }

    private String quotePhysical(MetaTable table) {
        return "\"" + table.physicalTableName().replace("\"", "\"\"") + "\"";
    }

    /**
     * 一版本一记录（uq_meta_table_migration_version 约束）：单变更保留明细字段，
     * 多变更聚合为一条 changeType=MULTI、ddlSql 拼接全部语句的记录。
     */
    private List<MetaTableMigration> buildPendingMigrations(MetaTable table, int version, List<SchemaDdl> ddlList,
            Long operatorId) {
        LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());
        if (ddlList.size() == 1) {
            return List.of(buildMigrationRecord(table, version, ddlList.getFirst(), operatorId, now));
        }
        MetaTableMigration record = new MetaTableMigration();
        fillRecordBase(record, table, version, operatorId, now);
        record.setChangeType("MULTI");
        record.setDdlSql(ddlList.stream().flatMap(ddl -> ddl.sqls().stream())
                .collect(Collectors.joining(";\n")));
        return List.of(record);
    }

    private MetaTableMigration buildMigrationRecord(MetaTable table, int version, SchemaDdl ddl, Long operatorId,
            LocalDateTime now) {
        MetaTableMigration record = new MetaTableMigration();
        fillRecordBase(record, table, version, operatorId, now);
        record.setChangeType(ddl.change().getType().name());
        record.setDdlSql(String.join(";\n", ddl.sqls()));

        MetaColumn oldColumn = ddl.change().getOldColumn();
        MetaColumn newColumn = ddl.change().getNewColumn();

        if (ddl.change().getOldIndexGroup() != null) {
            record.setColumnCode(ddl.change().getOldIndexGroup());
        } else if (ddl.change().getNewIndexGroup() != null) {
            record.setColumnCode(ddl.change().getNewIndexGroup());
        } else if (oldColumn != null) {
            record.setColumnCode(oldColumn.getColumnCode());
        } else if (newColumn != null) {
            record.setColumnCode(newColumn.getColumnCode());
        }
        if (newColumn != null && oldColumn != null && ddl.change().getOldIndexGroup() == null) {
            record.setOldColumnCode(oldColumn.getColumnCode());
        }

        record.setOldType(ddl.change().getOldType());
        record.setNewType(ddl.change().getNewType());
        record.setOldDefault(ddl.change().getOldDefault());
        record.setNewDefault(ddl.change().getNewDefault());
        return record;
    }

    private void fillRecordBase(MetaTableMigration record, MetaTable table, int version, Long operatorId,
            LocalDateTime now) {
        record.setTableId(java.util.Objects.requireNonNull(table.getId()));
        record.setVersion(version);
        record.setStatus("PENDING");
        record.setCreatorId(operatorId);
        record.setCreateTime(now);
        record.setDeleted(false);
    }
}
