package com.lesofn.archforge.server.admin.dto;

import com.lesofn.archforge.meta.table.api.domain.MetaColumn;
import com.lesofn.archforge.meta.table.api.domain.MetaTable;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

/**
 * 元表格响应 DTO
 */
@Data
public class MetaTableResponse {

    private Long id;

    private String tableCode;

    private String tableName;

    private String description;

    private String tablePrefix;

    private Integer status;

    private Long creatorId;

    private String creatorName;

    private LocalDateTime createTime;

    private Long updaterId;

    private String updaterName;

    private LocalDateTime updateTime;

    private List<MetaColumn> columns;

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
