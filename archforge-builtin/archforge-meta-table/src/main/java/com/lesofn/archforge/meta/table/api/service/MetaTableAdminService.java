package com.lesofn.archforge.meta.table.api.service;

import com.lesofn.archforge.meta.table.api.domain.MetaColumn;
import org.jspecify.annotations.Nullable;
import com.lesofn.archforge.meta.table.api.domain.MetaTable;
import com.lesofn.archforge.meta.table.api.dto.SchemaPreview;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 元表格定义管理服务。
 */
public interface MetaTableAdminService {

    Long create(MetaTable table, List<MetaColumn> columns);

    /**
     * 结构更新：columns 必填且非空（只改元信息请走 {@link #updateMeta}）。
     * 执行 schema diff + preflight，不落空变更。
     */
    void update(Long id, MetaTable table, List<MetaColumn> columns, Long operatorId);

    /** 元信息更新：仅 tableName / description / status，完全不碰列结构。 */
    void updateMeta(Long id, MetaTable table, Long operatorId);

    /**
     * 预览 {@link #update} 将执行的 Schema 变更：diff 明细 + 每条变更的违规行数 +
     * 处置方式（NONE/BACKFILL/BLOCKED）+ 将执行的 DDL。只读，不落任何写操作。
     */
    SchemaPreview previewSchema(Long id, MetaTable table, @Nullable List<MetaColumn> columns);

    Long copy(Long id);

    MetaTable findById(Long id);

    List<MetaColumn> findColumns(Long tableId);

    Page<MetaTable> list(String keyword, Pageable pageable);

    long checkDelete(Long id);

    void delete(Long id, boolean force);
}
