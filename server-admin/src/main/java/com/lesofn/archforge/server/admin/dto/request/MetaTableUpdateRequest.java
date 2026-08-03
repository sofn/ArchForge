package com.lesofn.archforge.server.admin.dto.request;

import com.lesofn.archforge.meta.table.api.domain.MetaTable;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 元表格更新请求
 */
@Data
public class MetaTableUpdateRequest {

    @NotBlank
    private String tableName;

    private String description;

    private Integer status;

    public MetaTable toTable() {
        MetaTable table = new MetaTable();
        table.setTableName(tableName);
        table.setDescription(description);
        table.setStatus(status);
        return table;
    }
}
