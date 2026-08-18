package com.lesofn.archforge.server.admin.dto.request;

import com.lesofn.archforge.meta.table.api.domain.MetaColumn;
import com.lesofn.archforge.meta.table.api.domain.MetaTable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
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

    @Valid
    private List<MetaTableCreateRequest.MetaColumnRequest> columns;

    private Boolean force;

    public MetaTable toTable() {
        MetaTable table = new MetaTable();
        table.setTableName(tableName);
        table.setDescription(description);
        table.setStatus(status);
        return table;
    }

    public List<MetaColumn> toColumns() {
        if (columns == null) {
            return null;
        }
        return columns.stream().map(MetaTableCreateRequest.MetaColumnRequest::toColumn).toList();
    }
}
