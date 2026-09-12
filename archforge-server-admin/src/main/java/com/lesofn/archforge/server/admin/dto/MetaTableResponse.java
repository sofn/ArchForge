package com.lesofn.archforge.server.admin.dto;

import com.lesofn.archforge.meta.table.api.domain.MetaColumn;
import org.jspecify.annotations.Nullable;
import com.lesofn.archforge.meta.table.api.domain.MetaTable;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

/**
 * 元表格响应 DTO
 */
@Data
@SuppressWarnings("NullAway.Init")
public class MetaTableResponse {

    private @Nullable Long id;

    private @Nullable String tableCode;

    private @Nullable String tableName;

    private @Nullable String description;

    private @Nullable String tablePrefix;

    private @Nullable Integer status;

    private @Nullable Long creatorId;

    private @Nullable String creatorName;

    private @Nullable LocalDateTime createTime;

    private @Nullable Long updaterId;

    private @Nullable String updaterName;

    private @Nullable LocalDateTime updateTime;

    private @Nullable List<MetaColumn> columns;

    public static MetaTableResponse of(MetaTable table) {
        MetaTableResponse response = new MetaTableResponse();
        response.setId(table.getId());
        response.setTableCode(table.getTableCode());
        response.setTableName(table.getTableName());
        response.setDescription(table.getDescription());
        response.setTablePrefix(table.getTablePrefix());
        response.setStatus(table.getStatus());
        response.setCreatorId(table.getCreatorId());
        response.setCreateTime(table.getCreateTime());
        response.setUpdaterId(table.getUpdaterId());
        response.setUpdateTime(table.getUpdateTime());
        return response;
    }

    public static MetaTableResponse of(MetaTable table, List<MetaColumn> columns) {
        MetaTableResponse response = of(table);
        response.setColumns(columns);
        return response;
    }
}
