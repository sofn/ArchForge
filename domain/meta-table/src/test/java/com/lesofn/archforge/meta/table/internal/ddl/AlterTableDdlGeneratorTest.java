package com.lesofn.archforge.meta.table.internal.ddl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.lesofn.archforge.meta.table.api.domain.MetaColumn;
import com.lesofn.archforge.meta.table.api.domain.MetaColumnType;
import com.lesofn.archforge.meta.table.api.domain.MetaTable;
import com.lesofn.archforge.meta.table.internal.schema.SchemaChange;
import com.lesofn.archforge.meta.table.internal.schema.SchemaChangeType;
import java.util.List;
import org.junit.jupiter.api.Test;

class AlterTableDdlGeneratorTest {

    private final ColumnTypeResolver resolver = new ColumnTypeResolver();
    private final AlterTableDdlGenerator generator = new AlterTableDdlGenerator(resolver);

    @Test
    void shouldGenerateAddColumn() {
        MetaTable table = table("test");
        MetaColumn column = column("name", MetaColumnType.STRING, 100);

        SchemaChange change = new SchemaChange();
        change.setType(SchemaChangeType.ADD_COLUMN);
        change.setNewColumn(column);

        List<SchemaDdl> result = generator.generate(table, List.of(change));
        assertEquals(1, result.size());
        List<String> sqls = result.get(0).sqls();
        assertEquals(1, sqls.size());
        assertTrue(sqls.get(0).contains("ADD COLUMN IF NOT EXISTS \"name\" VARCHAR(100)"));
    }

    @Test
    void shouldGenerateCompositeIndex() {
        MetaTable table = table("test");
        MetaColumn a = column("a", MetaColumnType.STRING, 50);
        a.setIndex(true);
        a.setIndexGroup("group1");
        a.setSort(1);

        MetaColumn b = column("b", MetaColumnType.STRING, 50);
        b.setIndex(true);
        b.setIndexGroup("group1");
        b.setSort(2);

        SchemaChange change = new SchemaChange();
        change.setType(SchemaChangeType.ALTER_INDEX);
        change.setNewIndexGroup("group1");
        change.setNewIndexType("GIN");
        change.setNewGroupColumns(List.of(a, b));

        List<SchemaDdl> result = generator.generate(table, List.of(change));
        assertEquals(1, result.size());
        List<String> sqls = result.get(0).sqls();
        assertEquals(1, sqls.size());
        assertTrue(sqls.get(0).contains("CREATE INDEX IF NOT EXISTS \"idx_meta_test_group1\""));
        assertTrue(sqls.get(0).contains("USING GIN"));
        assertTrue(sqls.get(0).contains("\"a\", \"b\""));
    }

    @Test
    void shouldGenerateFulltextIndexExpression() {
        MetaTable table = table("test");
        MetaColumn column = column("content", MetaColumnType.TEXT, null);
        column.setIndex(true);
        column.setIndexType("FULLTEXT");

        SchemaChange change = new SchemaChange();
        change.setType(SchemaChangeType.ALTER_INDEX);
        change.setNewIndexGroup("content");
        change.setNewIndexType("FULLTEXT");
        change.setNewGroupColumns(List.of(column));

        List<SchemaDdl> result = generator.generate(table, List.of(change));
        List<String> sqls = result.get(0).sqls();
        assertTrue(sqls.get(0).contains("to_tsvector('chinese', \"content\")"));
        assertTrue(sqls.get(0).contains("USING GIN"));
    }

    private MetaTable table(String code) {
        MetaTable table = new MetaTable();
        table.setTableCode(code);
        table.setTablePrefix("meta_");
        return table;
    }

    private MetaColumn column(String code, MetaColumnType type, Integer length) {
        MetaColumn column = new MetaColumn();
        column.setColumnCode(code);
        column.setColumnName(code);
        column.setDataType(type);
        column.setLength(length);
        return column;
    }
}
