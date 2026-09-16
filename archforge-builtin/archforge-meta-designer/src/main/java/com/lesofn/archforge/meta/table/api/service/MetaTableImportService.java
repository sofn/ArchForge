package com.lesofn.archforge.meta.table.api.service;

import com.lesofn.archforge.meta.table.api.dto.ImportableTableInfo;
import com.lesofn.archforge.meta.table.api.dto.TableImportPreview;
import java.util.List;
import org.jspecify.annotations.Nullable;

/**
 * 已有物理表纳管服务：内省当前 schema 的表结构，评估兼容性并注册为元表格。
 *
 * <p>
 * 兼容条件（全部满足才可导入）：表名过 {@code SqlIdentifier.validateTableCode}；
 * 含 {@code id} 单列主键（bigint/int 且有默认值）、{@code deleted}（int + NOT NULL）、
 * {@code creator_id}/{@code updater_id}（bigint 系）、{@code create_time}/{@code update_time}
 * （timestamp 系）五个审计列；每个非审计列类型可映射 {@code MetaColumnType}。
 */
public interface MetaTableImportService {

    /** 当前 schema 全部物理表及其兼容性评估。 */
    List<ImportableTableInfo> listImportable();

    /** 指定表的列级映射预览；表不存在时抛 META_TABLE_NOT_EXISTS。 */
    TableImportPreview preview(String tableName);

    /**
     * 导入指定物理表：重新校验兼容性后注册 MetaTable + MetaColumn（不执行 DDL）。
     *
     * @param tableName 物理表名
     * @param displayName 显示名（空则用表注释或物理名）
     * @param description 描述
     * @param operatorId 操作人
     * @return 新注册的 MetaTable id
     */
    Long importTable(String tableName, @Nullable String displayName, @Nullable String description, Long operatorId);
}
