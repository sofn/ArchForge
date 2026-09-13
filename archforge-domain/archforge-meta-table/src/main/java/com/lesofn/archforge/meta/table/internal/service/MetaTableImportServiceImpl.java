package com.lesofn.archforge.meta.table.internal.service;

import static com.lesofn.archforge.meta.table.api.errors.MetaTableErrorCode.META_TABLE_IMPORT_INCOMPATIBLE;
import static com.lesofn.archforge.meta.table.api.errors.MetaTableErrorCode.META_TABLE_NOT_EXISTS;

import com.lesofn.archforge.meta.table.api.dao.MetaColumnRepository;
import com.lesofn.archforge.meta.table.api.dao.MetaTableRepository;
import com.lesofn.archforge.meta.table.api.domain.MetaColumn;
import com.lesofn.archforge.meta.table.api.domain.MetaTable;
import com.lesofn.archforge.meta.table.api.dto.ImportableTableInfo;
import com.lesofn.archforge.meta.table.api.dto.TableImportPreview;
import com.lesofn.archforge.meta.table.api.errors.MetaTableException;
import com.lesofn.archforge.meta.table.api.service.MetaTableImportService;
import com.lesofn.archforge.meta.table.internal.ddl.SqlIdentifier;
import com.lesofn.archforge.meta.table.internal.introspect.PgTypeMapping;
import com.lesofn.archforge.meta.table.internal.introspect.PostgresSchemaIntrospector;
import com.lesofn.archforge.meta.table.internal.introspect.PostgresSchemaIntrospector.ColumnInfo;
import com.lesofn.archforge.meta.table.internal.introspect.PostgresSchemaIntrospector.IndexScan;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 已有物理表纳管服务实现。
 *
 * <p>
 * 仅注册元数据，不执行任何 DDL：物理表保持原样（含数据）。审计列
 * （id/creator_id/create_time/updater_id/update_time/deleted）为固定设施列，
 * 不生成 {@link MetaColumn}。物理默认值不录入元数据（DB 侧仍生效）。
 */
@Service
@RequiredArgsConstructor
public class MetaTableImportServiceImpl implements MetaTableImportService {

    private static final Set<String> AUDIT_COLUMNS = Set.of(
            "id", "creator_id", "create_time", "updater_id", "update_time", "deleted");

    /** 平台保留表前缀 —— 与命名规则解耦的独立安全边界（meta_ 是自家前缀，validateTableCode 不拦） */
    private static final Set<String> PLATFORM_TABLE_PREFIXES = Set.of("sys_", "meta_", "qrtz_", "flyway_");

    private static final Set<String> INTEGER_UDTS = Set.of("int4", "int8");
    private static final Set<String> DELETED_UDTS = Set.of("int2", "int4", "int8");
    private static final Set<String> TIME_UDTS = Set.of("timestamp", "timestamptz");

    private final PostgresSchemaIntrospector introspector;
    private final MetaTableRepository metaTableRepository;
    private final MetaColumnRepository metaColumnRepository;

    @Override
    @Transactional(value = "metaTableTransactionManager", readOnly = true)
    public List<ImportableTableInfo> listImportable() {
        Set<String> registeredNames = registeredPhysicalNames();
        List<ImportableTableInfo> result = new ArrayList<>();
        for (PostgresSchemaIntrospector.TableInfo table : introspector.listTables()) {
            ImportableTableInfo info = new ImportableTableInfo();
            info.setTableName(table.name());
            info.setComment(table.comment());
            info.setEstimatedRows(table.estimatedRows());
            boolean registered = registeredNames.contains(table.name());
            info.setRegistered(registered);

            List<ColumnInfo> columns = introspector.listColumns(table.name());
            info.setColumnCount(columns.size());
            List<String> reasons = compatibilityReasons(
                    table.name(), columns, introspector.primaryKeyColumns(table.name()), registered);
            info.setReasons(reasons);
            info.setCompatible(reasons.isEmpty());
            result.add(info);
        }
        return result;
    }

    @Override
    @Transactional(value = "metaTableTransactionManager", readOnly = true)
    public TableImportPreview preview(String tableName) {
        requireTableExists(tableName);
        List<ColumnInfo> columns = introspector.listColumns(tableName);
        List<String> pkColumns = introspector.primaryKeyColumns(tableName);
        IndexScan indexScan = introspector.scanIndexes(tableName);
        boolean registered = registeredPhysicalNames().contains(tableName);

        TableImportPreview preview = new TableImportPreview();
        preview.setTableName(tableName);
        preview.setCompositeIndexes(indexScan.compositeIndexNames());

        Map<String, Boolean> indexFlags = new HashMap<>();
        indexScan.singleColumnIndexes()
                .forEach(idx -> indexFlags.merge(idx.columnName(), idx.unique(), (a, b) -> a || b));

        List<String> reasons = compatibilityReasons(tableName, columns, pkColumns, registered);
        preview.setReasons(reasons);
        preview.setCompatible(reasons.isEmpty());

        for (ColumnInfo column : columns) {
            preview.getColumns().add(toPreviewColumn(column, indexFlags));
        }
        // 表注释单独取（columns 查询不含表级注释）
        preview.setComment(tableComment(tableName));
        return preview;
    }

    @Override
    @Transactional("metaTableTransactionManager")
    public Long importTable(String tableName, @Nullable String displayName, @Nullable String description,
            Long operatorId) {
        // 独立 fail-fast 守卫：不依赖兼容判定链，防后续重构绕过
        if (isPlatformTable(tableName)) {
            throw new MetaTableException(META_TABLE_IMPORT_INCOMPATIBLE, "平台保留表，禁止导入: " + tableName);
        }
        TableImportPreview preview = preview(tableName);
        if (!preview.isCompatible()) {
            throw new MetaTableException(META_TABLE_IMPORT_INCOMPATIBLE, String.join("；", preview.getReasons()));
        }

        LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());
        MetaTable table = new MetaTable();
        table.setTableCode(tableName);
        table.setTablePrefix("");
        table.setTableName(
                displayName != null && !displayName.isBlank()
                        ? displayName
                        : preview.getComment() != null ? preview.getComment() : tableName);
        if (description != null) {
            table.setDescription(description);
        }
        table.setStatus(1);
        table.setSchemaVersion(1);
        table.setCreatorId(operatorId);
        table.setCreateTime(now);
        table.setUpdaterId(operatorId);
        table.setUpdateTime(now);
        table.setDeleted(false);
        MetaTable saved = metaTableRepository.save(table);

        List<MetaColumn> metaColumns = new ArrayList<>();
        for (TableImportPreview.PreviewColumn pc : preview.getColumns()) {
            if (pc.isAudit()) {
                continue;
            }
            metaColumns.add(toMetaColumn(pc, java.util.Objects.requireNonNull(saved.getId()), operatorId, now));
        }
        metaColumnRepository.saveAll(metaColumns);
        return java.util.Objects.requireNonNull(saved.getId());
    }

    // ---- 兼容判定 ----

    private List<String> compatibilityReasons(
            String tableName, List<ColumnInfo> columns, List<String> pkColumns, boolean registered) {
        List<String> reasons = new ArrayList<>();
        if (isPlatformTable(tableName)) {
            reasons.add("平台保留表（" + String.join("/", PLATFORM_TABLE_PREFIXES.stream().sorted().toList())
                    + " 前缀），禁止导入");
        }
        if (registered) {
            reasons.add("已注册为元表格");
        }
        try {
            SqlIdentifier.validateTableCode(tableName);
        } catch (MetaTableException e) {
            reasons.add("表名不合法: " + e.getMessage());
        }
        if (pkColumns.size() != 1 || !"id".equals(pkColumns.get(0))) {
            reasons.add("主键必须是单列 id（实际: " + (pkColumns.isEmpty() ? "无主键" : String.join(",", pkColumns)) + "）");
        }

        Map<String, ColumnInfo> byName = new HashMap<>();
        for (ColumnInfo c : columns) {
            byName.put(c.name(), c);
        }
        checkAuditColumn(byName.get("id"), INTEGER_UDTS, "id", true, reasons);
        checkAuditColumn(byName.get("deleted"), DELETED_UDTS, "deleted", false, reasons);
        checkAuditColumn(byName.get("creator_id"), INTEGER_UDTS, "creator_id", false, reasons);
        checkAuditColumn(byName.get("updater_id"), INTEGER_UDTS, "updater_id", false, reasons);
        checkAuditColumn(byName.get("create_time"), TIME_UDTS, "create_time", false, reasons);
        checkAuditColumn(byName.get("update_time"), TIME_UDTS, "update_time", false, reasons);

        int businessColumns = 0;
        for (ColumnInfo column : columns) {
            if (AUDIT_COLUMNS.contains(column.name())) {
                continue;
            }
            businessColumns++;
            try {
                SqlIdentifier.validateColumnCode(column.name());
            } catch (MetaTableException e) {
                reasons.add("列名 " + column.name() + " 不合法: " + e.getMessage());
                continue;
            }
            if (PgTypeMapping.map(column.udtName(), column.charLen(), column.precision(), column.scale()) == null) {
                reasons.add("列 " + column.name() + " 类型 " + column.udtName() + " 不支持导入");
            }
        }
        if (businessColumns == 0) {
            reasons.add("无业务列（仅审计列的表不生成字段定义）");
        }
        return reasons;
    }

    private void checkAuditColumn(
            @Nullable ColumnInfo column, Set<String> allowedUdts, String name, boolean needDefault,
            List<String> reasons) {
        if (column == null) {
            reasons.add("缺少审计列 " + name);
            return;
        }
        if (!allowedUdts.contains(column.udtName())) {
            reasons.add("审计列 " + name + " 类型应为 " + allowedUdts + "（实际: " + column.udtName() + "）");
        }
        if ("deleted".equals(name) && column.nullable()) {
            reasons.add("审计列 deleted 必须 NOT NULL");
        }
        if (needDefault && !column.identity() && column.columnDefault() == null) {
            reasons.add("主键 id 必须有默认值（identity 或 sequence），否则无法写入");
        }
    }

    // ---- 映射 ----

    private TableImportPreview.PreviewColumn toPreviewColumn(ColumnInfo column, Map<String, Boolean> indexFlags) {
        TableImportPreview.PreviewColumn pc = new TableImportPreview.PreviewColumn();
        pc.setColumnCode(column.name());
        pc.setComment(column.comment());
        pc.setSort(column.position());
        if (AUDIT_COLUMNS.contains(column.name())) {
            pc.setAudit(true);
            return pc;
        }
        PgTypeMapping.MappedType mapped = PgTypeMapping.map(
                column.udtName(), column.charLen(), column.precision(), column.scale());
        if (mapped == null) {
            pc.setWarning("类型 " + column.udtName() + " 不支持导入");
            return pc;
        }
        pc.setDataType(mapped.type().name());
        pc.setLength(mapped.length());
        pc.setPrecision(mapped.precision());
        pc.setScale(mapped.scale());
        pc.setRequired(!column.nullable() && column.columnDefault() == null && !column.identity());
        Boolean uniqueFlag = indexFlags.get(column.name());
        pc.setUnique(Boolean.TRUE.equals(uniqueFlag));
        pc.setIndexed(indexFlags.containsKey(column.name()));
        if (column.columnDefault() != null && !column.identity()) {
            pc.setWarning("物理默认值不录入元数据（DB 侧仍生效）: " + column.columnDefault());
        }
        return pc;
    }

    private MetaColumn toMetaColumn(TableImportPreview.PreviewColumn pc, Long tableId, Long operatorId,
            LocalDateTime now) {
        MetaColumn column = new MetaColumn();
        column.setTableId(tableId);
        column.setColumnCode(pc.getColumnCode());
        String comment = pc.getComment();
        column.setColumnName(comment != null && !comment.isBlank() ? comment : pc.getColumnCode());
        column.setDataType(com.lesofn.archforge.meta.table.api.domain.MetaColumnType.of(
                java.util.Objects.requireNonNull(pc.getDataType())));
        column.setLength(pc.getLength());
        if (pc.getPrecision() != null) {
            column.setPrecision(pc.getPrecision());
        }
        if (pc.getScale() != null) {
            column.setScale(pc.getScale());
        }
        column.setNullable(!pc.isRequired());
        column.setRequired(pc.isRequired());
        column.setUnique(pc.isUnique());
        column.setIndex(pc.isIndexed() && !pc.isUnique());
        column.setSearchable(false);
        column.setListVisible(true);
        column.setSort(pc.getSort());
        column.setCreatorId(operatorId);
        column.setCreateTime(now);
        column.setUpdaterId(operatorId);
        column.setUpdateTime(now);
        column.setDeleted(false);
        return column;
    }

    // ---- 辅助 ----

    private static boolean isPlatformTable(String tableName) {
        for (String prefix : PLATFORM_TABLE_PREFIXES) {
            if (tableName.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    private void requireTableExists(String tableName) {
        if (!introspector.tableExists(tableName)) {
            throw new MetaTableException(META_TABLE_NOT_EXISTS);
        }
    }

    private @Nullable String tableComment(String tableName) {
        return introspector.listTables().stream()
                .filter(t -> t.name().equals(tableName))
                .findFirst()
                .map(PostgresSchemaIntrospector.TableInfo::comment)
                .orElse(null);
    }

    private Set<String> registeredPhysicalNames() {
        Set<String> names = new java.util.HashSet<>();
        for (MetaTable t : metaTableRepository.findAllByDeletedFalse()) {
            names.add(t.physicalTableName());
        }
        return names;
    }
}
