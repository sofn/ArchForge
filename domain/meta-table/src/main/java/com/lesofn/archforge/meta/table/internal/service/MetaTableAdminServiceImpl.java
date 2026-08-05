package com.lesofn.archforge.meta.table.internal.service;

import static com.lesofn.archforge.meta.table.internal.exception.MetaTableErrorCode.META_TABLE_CODE_EXISTS;
import static com.lesofn.archforge.meta.table.internal.exception.MetaTableErrorCode.META_TABLE_HAS_DATA;
import static com.lesofn.archforge.meta.table.internal.exception.MetaTableErrorCode.META_TABLE_NOT_EXISTS;

import com.lesofn.archforge.meta.table.api.dao.MetaColumnRepository;
import com.lesofn.archforge.meta.table.api.dao.MetaTableRepository;
import com.lesofn.archforge.meta.table.api.domain.MetaColumn;
import com.lesofn.archforge.meta.table.api.domain.MetaTable;
import com.lesofn.archforge.meta.table.api.domain.MetaTableMigration;
import com.lesofn.archforge.meta.table.api.service.MetaTableAdminService;
import com.lesofn.archforge.meta.table.internal.ddl.AlterTableDdlGenerator;
import com.lesofn.archforge.meta.table.internal.ddl.MetaTableDdlGenerator;
import com.lesofn.archforge.meta.table.internal.ddl.SchemaDdl;
import com.lesofn.archforge.meta.table.internal.exception.MetaTableErrorCode;
import com.lesofn.archforge.meta.table.internal.exception.MetaTableException;
import com.lesofn.archforge.meta.table.internal.schema.SchemaChange;
import com.lesofn.archforge.meta.table.internal.schema.SchemaChangeType;
import com.lesofn.archforge.meta.table.internal.schema.SchemaDiffEngine;
import com.lesofn.archforge.meta.table.internal.validator.MetaTableValidator;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
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
    @Transactional("metaTableTransactionManager")
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
            table.setUpdateTime(LocalDateTime.now());
        }
        MetaTable saved = metaTableRepository.save(table);

        for (MetaColumn column : columns) {
            column.setTableId(saved.getId());
        }
        metaColumnRepository.saveAll(columns);

        MetaTableDdlGenerator.DdlResult ddl = ddlGenerator.generateCreateTable(saved, columns);
        jdbcTemplate.getJdbcOperations().execute(ddl.createTableSql());
        ddl.indexSqls().forEach(sql -> jdbcTemplate.getJdbcOperations().execute(sql));

        return saved.getId();
    }

    @Override
    @Transactional("metaTableTransactionManager")
    public void update(Long id, MetaTable table, List<MetaColumn> columns, Long operatorId) {
        MetaTable existing = findById(id);
        existing.setUpdaterId(operatorId);

        if (columns == null || columns.isEmpty()) {
            existing.setTableName(table.getTableName());
            existing.setDescription(table.getDescription());
            if (table.getStatus() != null) {
                existing.setStatus(table.getStatus());
            }
            metaTableRepository.save(existing);
            return;
        }

        validator.validate(existing, columns);

        List<MetaColumn> oldColumns = findColumns(id);
        List<SchemaChange> changes = schemaDiffEngine.diff(existing, oldColumns, columns);
        if (changes.isEmpty()) {
            existing.setTableName(table.getTableName());
            existing.setDescription(table.getDescription());
            if (table.getStatus() != null) {
                existing.setStatus(table.getStatus());
            }
            metaTableRepository.save(existing);
            return;
        }

        List<SchemaDdl> ddlStatements = alterTableDdlGenerator.generate(existing, changes);

        int currentVersion = existing.getSchemaVersion() == null ? 1 : existing.getSchemaVersion();
        int nextVersion = currentVersion + 1;

        List<MetaTableMigration> records = migrationService.createPendingRecords(existing, nextVersion, ddlStatements,
                operatorId);

        for (SchemaDdl ddl : ddlStatements) {
            for (String sql : ddl.sqls()) {
                jdbcTemplate.getJdbcOperations().execute(sql);
            }
        }

        existing.setSchemaVersion(nextVersion);
        existing.setTableName(table.getTableName());
        existing.setDescription(table.getDescription());
        if (table.getStatus() != null) {
            existing.setStatus(table.getStatus());
        }
        metaTableRepository.save(existing);

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

        LocalDateTime executedAt = LocalDateTime.now();
        records.forEach(r -> {
            r.setStatus("APPLIED");
            r.setExecutedAt(executedAt);
        });
        migrationService.saveAll(records);
    }

    @Override
    @Transactional("metaTableTransactionManager")
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
    @Transactional("metaTableTransactionManager")
    public void delete(Long id, boolean force) {
        MetaTable table = findById(id);
        long dataCount = checkDelete(id);
        if (dataCount > 0 && !force) {
            throw new MetaTableException(META_TABLE_HAS_DATA, dataCount);
        }

        jdbcTemplate.getJdbcOperations().execute(ddlGenerator.generateDropTable(table.physicalTableName()));

        metaColumnRepository.deleteByTableId(id);
        metaTableRepository.deleteById(id);
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
        copy.setTenantColumn(source.getTenantColumn());
        copy.setOwnerColumn(source.getOwnerColumn());
        return copy;
    }

    private String quotePhysical(MetaTable table) {
        return "\"" + table.physicalTableName().replace("\"", "\"\"") + "\"";
    }
}
