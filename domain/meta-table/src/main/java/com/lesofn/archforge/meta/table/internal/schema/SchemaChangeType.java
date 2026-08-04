package com.lesofn.archforge.meta.table.internal.schema;

/**
 * Schema 变更类型。
 */
public enum SchemaChangeType {
    ADD_COLUMN,
    DROP_COLUMN,
    RENAME_COLUMN,
    ALTER_TYPE,
    ALTER_DEFAULT,
    ALTER_NULL,
    ALTER_INDEX
}
