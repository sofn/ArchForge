package com.lesofn.archforge.meta.table.api.definition;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.lesofn.archforge.meta.table.api.domain.OptionItem;
import java.util.List;
import lombok.Data;
import org.jspecify.annotations.Nullable;

/**
 * File-form of one {@code MetaColumn} — the serializable shape stored under
 * {@code archforge/meta/<tableCode>.yaml}. Property order is pinned for stable
 * diffs; null fields are omitted.
 */
@Data
@SuppressWarnings("NullAway.Init")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "columnCode", "columnName", "dataType", "length", "precision", "scale",
        "nullable", "defaultValue", "unique", "required", "searchable", "listVisible",
        "index", "sort", "options", "referenceTable", "referenceColumn",
        "displayExpression", "tenantColumn", "ownerColumn", "indexType",
        "indexGroup", "arrayElementType", "searchType", "dictCode"
})
public class ColumnDefinition {

    private String columnCode;

    private String columnName;

    private String dataType;

    private @Nullable Integer length;

    private @Nullable Integer precision;

    private @Nullable Integer scale;

    private @Nullable Boolean nullable;

    private @Nullable String defaultValue;

    private @Nullable Boolean unique;

    private @Nullable Boolean required;

    private @Nullable Boolean searchable;

    private @Nullable Boolean listVisible;

    private @Nullable Boolean index;

    private @Nullable Integer sort;

    private @Nullable List<OptionItem> options;

    private @Nullable String referenceTable;

    private @Nullable String referenceColumn;

    private @Nullable String displayExpression;

    private @Nullable Boolean tenantColumn;

    private @Nullable Boolean ownerColumn;

    private @Nullable String indexType;

    private @Nullable String indexGroup;

    private @Nullable String arrayElementType;

    private @Nullable String searchType;

    private @Nullable String dictCode;
}
