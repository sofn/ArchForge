package com.lesofn.archforge.server.admin.dto.request;

import com.lesofn.archforge.meta.table.api.domain.MetaColumn;
import com.lesofn.archforge.meta.table.api.domain.MetaTable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.Data;

/**
 * 元表格创建请求
 */
@Data
public class MetaTableCreateRequest {

    @NotBlank
    private String tableCode;

    @NotBlank
    private String tableName;

    private String description;

    private String tablePrefix = "meta_";

    private Integer status = 1;

    @NotEmpty
    @Valid
    private List<MetaColumnRequest> columns;

    public List<com.lesofn.archforge.meta.table.api.domain.MetaColumn> toColumns() {
        return columns.stream().map(MetaColumnRequest::toColumn).toList();
    }

    /**
     * 转换为领域对象。
     */
    public MetaTable toTable() {
        MetaTable table = new MetaTable();
        table.setTableCode(tableCode);
        table.setTableName(tableName);
        table.setDescription(description);
        table.setTablePrefix(tablePrefix);
        table.setStatus(status);
        return table;
    }

    /**
     * 元表格字段请求
     */
    @Data
    public static class MetaColumnRequest {

        private Long id;

        @NotBlank
        private String columnCode;

        @NotBlank
        private String columnName;

        @NotBlank
        private String dataType;

        private Integer length;

        private Integer precision;

        private Integer scale;

        private Boolean nullable = true;

        private String defaultValue;

        private Boolean unique = false;

        private Boolean required = false;

        private Boolean searchable = true;

        private Boolean listVisible = true;

        private Boolean index = false;

        private String indexType;

        private String indexGroup;

        private Integer sort = 0;

        private String arrayElementType;

        private String searchType;

        private String dictCode;

        private List<com.lesofn.archforge.meta.table.api.domain.OptionItem> options;

        private String referenceTable;

        private String referenceColumn;

        private Boolean tenantColumn = false;

        private Boolean ownerColumn = false;

        public com.lesofn.archforge.meta.table.api.domain.MetaColumn toColumn() {
            com.lesofn.archforge.meta.table.api.domain.MetaColumn column = new com.lesofn.archforge.meta.table.api.domain.MetaColumn();
            column.setId(id);
            column.setColumnCode(columnCode);
            column.setColumnName(columnName);
            column.setDataType(com.lesofn.archforge.meta.table.api.domain.MetaColumnType.valueOf(dataType));
            column.setLength(length);
            column.setPrecision(precision);
            column.setScale(scale);
            column.setNullable(nullable);
            column.setDefaultValue(defaultValue);
            column.setUnique(unique);
            column.setRequired(required);
            column.setSearchable(searchable);
            column.setListVisible(listVisible);
            column.setIndex(index);
            column.setIndexType(indexType);
            column.setIndexGroup(indexGroup);
            column.setSort(sort);
            column.setOptions(options);
            column.setReferenceTable(referenceTable);
            column.setReferenceColumn(referenceColumn);
            column.setTenantColumn(tenantColumn);
            column.setOwnerColumn(ownerColumn);
            column.setArrayElementType(arrayElementType);
            column.setSearchType(searchType);
            column.setDictCode(dictCode);
            return column;
        }
    }
}
