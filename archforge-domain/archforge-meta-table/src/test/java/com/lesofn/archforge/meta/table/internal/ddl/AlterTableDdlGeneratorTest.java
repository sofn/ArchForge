package com.lesofn.archforge.meta.table.internal.ddl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.lesofn.archforge.meta.table.api.domain.MetaColumn;
import com.lesofn.archforge.meta.table.api.domain.MetaColumnType;
import com.lesofn.archforge.meta.table.api.domain.MetaTable;
import com.lesofn.archforge.meta.table.internal.schema.SchemaChange;
import com.lesofn.archforge.meta.table.internal.schema.SchemaChangeType;
import java.util.List;
import java.util.Optional;
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

    @Test
    void uniqueIndexBecomesPartial() {
        MetaTable table = table("test");
        MetaColumn column = column("code", MetaColumnType.STRING, 50);
        column.setUnique(true);

        SchemaChange change = new SchemaChange();
        change.setType(SchemaChangeType.ALTER_INDEX);
        change.setNewIndexGroup("code");
        change.setNewIndexType("BTREE");
        change.setNewUnique(true);
        change.setNewGroupColumns(List.of(column));

        String sql = generator.generate(table, List.of(change)).get(0).sqls().get(0);
        assertTrue(sql.contains("CREATE UNIQUE INDEX IF NOT EXISTS \"uq_meta_test_code\""));
        assertTrue(sql.endsWith("WHERE deleted = 0"));
    }

    @Test
    void nonUniqueIndexHasNoPartialPredicate() {
        MetaTable table = table("test");
        MetaColumn column = column("code", MetaColumnType.STRING, 50);
        column.setIndex(true);

        SchemaChange change = new SchemaChange();
        change.setType(SchemaChangeType.ALTER_INDEX);
        change.setNewIndexGroup("code");
        change.setNewIndexType("BTREE");
        change.setNewUnique(false);
        change.setNewGroupColumns(List.of(column));

        String sql = generator.generate(table, List.of(change)).get(0).sqls().get(0);
        assertTrue(sql.startsWith("CREATE INDEX"));
        assertFalse(sql.contains("WHERE deleted = 0"));
    }

    @Test
    void violationCountSqlForSetNotNullCountsNulls() {
        MetaTable table = table("test");
        SchemaChange change = alterNullChange(true);

        Optional<String> sql = generator.buildViolationCountSql(table, change);
        assertEquals("SELECT COUNT(*) FROM \"meta_test\" WHERE \"name\" IS NULL", sql.orElseThrow());
    }

    @Test
    void dropNotNullNeedsNoPreflight() {
        Optional<String> sql = generator.buildViolationCountSql(table("test"), alterNullChange(false));
        assertTrue(sql.isEmpty());
    }

    @Test
    void backfillUpdateUsesFormattedDefault() {
        MetaTable table = table("test");
        MetaColumn column = column("status", MetaColumnType.STRING, 20);
        column.setRequired(true);
        column.setDefaultValue("n/a");

        SchemaChange change = new SchemaChange();
        change.setType(SchemaChangeType.ALTER_NULL);
        change.setNewColumn(column);
        change.setNewRequired(true);

        assertEquals("UPDATE \"meta_test\" SET \"status\" = 'n/a' WHERE \"status\" IS NULL",
                generator.buildBackfillUpdateSql(table, change).orElseThrow());
    }

    @Test
    void noBackfillWithoutDefault() {
        MetaColumn column = column("name", MetaColumnType.STRING, 50);
        column.setRequired(true);

        SchemaChange change = new SchemaChange();
        change.setType(SchemaChangeType.ALTER_NULL);
        change.setNewColumn(column);

        assertTrue(generator.buildBackfillUpdateSql(table("test"), change).isEmpty());
    }

    @Test
    void typeLossCountSqlRoundTripsPlainTypes() {
        MetaTable table = table("test");
        SchemaChange change = alterTypeChange("content", "TEXT", "BIGINT");

        Optional<String> sql = generator.buildViolationCountSql(table, change);
        assertEquals("SELECT COUNT(*) FROM \"meta_test\" WHERE \"content\" IS NOT NULL AND " +
                "CAST(\"content\"::BIGINT AS TEXT) IS DISTINCT FROM \"content\"", sql.orElseThrow());
    }

    @Test
    void typeLossCountSqlRelaysTextualSources() {
        MetaTable table = table("test");
        SchemaChange change = alterTypeChange("payload", "JSONB", "TEXT");

        String sql = generator.buildViolationCountSql(table, change).orElseThrow();
        assertTrue(sql.contains("\"payload\"::text::TEXT"), sql);
        assertTrue(sql.contains("::text::JSONB IS DISTINCT FROM \"payload\""), sql);
    }

    @Test
    void addAndDropChangesNeedNoPreflight() {
        MetaColumn column = column("name", MetaColumnType.STRING, 50);
        SchemaChange add = new SchemaChange();
        add.setType(SchemaChangeType.ADD_COLUMN);
        add.setNewColumn(column);
        SchemaChange drop = new SchemaChange();
        drop.setType(SchemaChangeType.DROP_COLUMN);
        drop.setOldColumn(column);

        assertTrue(generator.buildViolationCountSql(table("test"), add).isEmpty());
        assertTrue(generator.buildViolationCountSql(table("test"), drop).isEmpty());
    }

    private SchemaChange alterNullChange(boolean required) {
        MetaColumn column = column("name", MetaColumnType.STRING, 50);
        column.setRequired(required);
        SchemaChange change = new SchemaChange();
        change.setType(SchemaChangeType.ALTER_NULL);
        change.setNewColumn(column);
        change.setNewRequired(required);
        return change;
    }

    private SchemaChange alterTypeChange(String code, String oldType, String newType) {
        MetaColumn oldColumn = column(code, MetaColumnType.TEXT, null);
        MetaColumn newColumn = column(code, MetaColumnType.INTEGER, null);
        SchemaChange change = new SchemaChange();
        change.setType(SchemaChangeType.ALTER_TYPE);
        change.setOldColumn(oldColumn);
        change.setNewColumn(newColumn);
        change.setOldType(oldType);
        change.setNewType(newType);
        return change;
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
