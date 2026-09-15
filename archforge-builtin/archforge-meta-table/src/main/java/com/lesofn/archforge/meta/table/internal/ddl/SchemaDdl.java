package com.lesofn.archforge.meta.table.internal.ddl;

import com.lesofn.archforge.meta.table.internal.schema.SchemaChange;
import java.util.List;

/**
 * 一个 Schema 变更对应的一条或多条 DDL 语句。
 */
public record SchemaDdl(SchemaChange change, List<String> sqls) {
}
