package com.lesofn.archforge.meta.table.internal.ddl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.lesofn.archforge.meta.table.api.domain.MetaColumn;
import com.lesofn.archforge.meta.table.api.domain.MetaColumnType;
import com.lesofn.archforge.meta.table.api.domain.MetaTable;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Verifies user-defined unique indexes become partial (soft-deleted rows excluded). */
class MetaTableDdlGeneratorTest {

    private final MetaTableDdlGenerator generator = new MetaTableDdlGenerator(new ColumnTypeResolver());

    @Test
    void uniqueColumnIndexIsPartial() {
        MetaColumn column = column("order_no");
        column.setUnique(true);

        List<String> sqls = generator.generateCreateTable(table(), List.of(column)).indexSqls();

        assertEquals(1, sqls.size());
        String sql = sqls.get(0);
        assertTrue(sql.contains("CREATE UNIQUE INDEX IF NOT EXISTS \"uq_meta_test_order_no\""), sql);
        assertTrue(sql.endsWith("WHERE deleted = 0"), sql);
    }

    @Test
    void plainIndexIsNotPartial() {
        MetaColumn column = column("note");
        column.setIndex(true);

        List<String> sqls = generator.generateCreateTable(table(), List.of(column)).indexSqls();

        String sql = sqls.get(0);
        assertTrue(sql.startsWith("CREATE INDEX"));
        assertFalse(sql.contains("WHERE deleted = 0"));
    }

    private MetaTable table() {
        MetaTable table = new MetaTable();
        table.setTableCode("test");
        table.setTablePrefix("meta_");
        return table;
    }

    private MetaColumn column(String code) {
        MetaColumn column = new MetaColumn();
        column.setColumnCode(code);
        column.setColumnName(code);
        column.setDataType(MetaColumnType.STRING);
        column.setLength(50);
        return column;
    }
}
