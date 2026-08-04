package com.lesofn.archforge.meta.table.internal.schema;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.lesofn.archforge.meta.table.api.domain.MetaColumn;
import com.lesofn.archforge.meta.table.api.domain.MetaColumnType;
import com.lesofn.archforge.meta.table.api.domain.MetaTable;
import com.lesofn.archforge.meta.table.internal.ddl.ColumnTypeResolver;
import java.util.List;
import org.junit.jupiter.api.Test;

class SchemaDiffEngineTest {

    private final ColumnTypeResolver resolver = new ColumnTypeResolver();
    private final SchemaDiffEngine engine = new SchemaDiffEngine(resolver);

    @Test
    void shouldDetectAddedColumn() {
        MetaColumn oldCol = column("old", MetaColumnType.STRING, 50);
        MetaColumn newCol = column("new", MetaColumnType.STRING, 50);
        newCol.setId(null);

        List<SchemaChange> changes = engine.diff(table(), List.of(oldCol), List.of(oldCol, newCol));

        assertEquals(1, changes.size());
        assertEquals(SchemaChangeType.ADD_COLUMN, changes.get(0).getType());
        assertEquals("new", changes.get(0).getNewColumn().getColumnCode());
    }

    @Test
    void shouldDetectDroppedColumn() {
        MetaColumn oldCol = column("old", MetaColumnType.STRING, 50);
        MetaColumn newCol = column("new", MetaColumnType.STRING, 50);

        List<SchemaChange> changes = engine.diff(table(), List.of(oldCol, newCol), List.of(newCol));

        assertEquals(1, changes.size());
        assertEquals(SchemaChangeType.DROP_COLUMN, changes.get(0).getType());
    }

    @Test
    void shouldDetectIndexGroupChange() {
        MetaColumn oldA = column("a", MetaColumnType.STRING, 50);
        oldA.setIndex(true);
        oldA.setIndexGroup("group1");
        oldA.setSort(1);

        MetaColumn oldB = column("b", MetaColumnType.STRING, 50);
        oldB.setIndex(true);
        oldB.setIndexGroup("group1");
        oldB.setSort(2);

        MetaColumn newA = column("a", MetaColumnType.STRING, 50);
        newA.setIndex(true);
        newA.setIndexGroup("group1");
        newA.setSort(1);
        newA.setIndexType("GIN");

        MetaColumn newB = column("b", MetaColumnType.STRING, 50);
        newB.setIndex(true);
        newB.setIndexGroup("group1");
        newB.setSort(2);
        newB.setIndexType("GIN");

        List<SchemaChange> changes = engine.diff(table(), List.of(oldA, oldB), List.of(newA, newB));

        assertEquals(1, changes.size());
        SchemaChange indexChange = changes.get(0);
        assertEquals(SchemaChangeType.ALTER_INDEX, indexChange.getType());
        assertEquals("GIN", indexChange.getNewIndexType());
    }

    @Test
    void shouldDetectArrayElementTypeChange() {
        MetaColumn oldCol = column("tags", MetaColumnType.ARRAY, null);
        oldCol.setArrayElementType("STRING");
        MetaColumn newCol = column("tags", MetaColumnType.ARRAY, null);
        newCol.setArrayElementType("INTEGER");

        List<SchemaChange> changes = engine.diff(table(), List.of(oldCol), List.of(newCol));

        assertEquals(1, changes.size());
        assertEquals(SchemaChangeType.ALTER_TYPE, changes.get(0).getType());
        assertTrue(changes.get(0).getNewType().contains("BIGINT"));
    }

    private MetaTable table() {
        MetaTable table = new MetaTable();
        table.setTableCode("demo");
        table.setTablePrefix("meta_");
        return table;
    }

    private MetaColumn column(String code, MetaColumnType type, Integer length) {
        MetaColumn column = new MetaColumn();
        column.setId(1L + code.hashCode());
        column.setColumnCode(code);
        column.setColumnName(code);
        column.setDataType(type);
        column.setLength(length);
        return column;
    }
}
