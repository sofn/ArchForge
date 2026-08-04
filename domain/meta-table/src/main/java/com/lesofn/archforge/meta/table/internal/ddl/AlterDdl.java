package com.lesofn.archforge.meta.table.internal.ddl;

import com.lesofn.archforge.meta.table.internal.schema.SchemaChange;

/**
 * 一次 Schema 变更对应的单条 DDL 语句。
 */
public record AlterDdl(SchemaChange change, String sql) {
}
