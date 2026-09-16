package com.lesofn.archforge.meta.table.api.definition;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.List;
import lombok.Data;
import org.jspecify.annotations.Nullable;

/**
 * File-form of one {@code MetaTable} — the serializable shape stored at
 * {@code archforge/meta/<tableCode>.yaml}. Validated against
 * {@code spec/schemas/meta/table.schema.json}; property order is pinned for
 * stable diffs.
 */
@Data
@SuppressWarnings("NullAway.Init")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "tableCode", "tableName", "description", "tablePrefix", "status",
        "schemaVersion", "columns", "removedColumns"
})
public class TableDefinition {

    private String tableCode;

    private String tableName;

    private @Nullable String description;

    private @Nullable String tablePrefix;

    private @Nullable Integer status;

    private @Nullable Integer schemaVersion;

    private List<ColumnDefinition> columns = List.of();

    /**
     * Column codes intentionally deleted — import never drops DB columns
     * implicitly; listing a code here is the explicit delete.
     */
    private @Nullable List<String> removedColumns;
}
