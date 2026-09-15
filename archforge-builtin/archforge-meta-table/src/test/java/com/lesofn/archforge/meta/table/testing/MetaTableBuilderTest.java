package com.lesofn.archforge.meta.table.testing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.lesofn.archforge.meta.table.api.domain.MetaColumn;
import com.lesofn.archforge.meta.table.api.domain.MetaTable;
import com.lesofn.archforge.meta.table.api.domain.MetaColumnType;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Verifies the default-valid contract of the meta table test data builder. */
class MetaTableBuilderTest {

    @Test
    void buildsEnabledTableByDefault() {
        MetaTable table = MetaTableTestBuilder.aTable().build();

        assertTrue(table.isEnabled());
        assertNotNull(table.getTableCode());
        assertEquals(1, table.getSchemaVersion());
    }

    @Test
    void physicalTableNameUsesPrefixAndCode() {
        MetaTable table = MetaTableTestBuilder.aTable()
                .withTableCode("order")
                .withTablePrefix("biz_")
                .build();

        assertEquals("biz_order", table.physicalTableName());
    }

    @Test
    void columnsGetDefaultsAndSortOrder() {
        MetaTableTestBuilder builder = MetaTableTestBuilder.aTable();
        builder.withColumn("title").required().listVisible().add();
        builder.withColumn("price").ofType(MetaColumnType.DECIMAL).add();

        MetaTable table = builder.build();
        List<MetaColumn> columns = builder.buildColumns();

        assertEquals(2, columns.size());
        assertEquals(MetaColumnType.STRING, columns.get(0).getDataType());
        assertFalse(columns.get(0).isNullableColumn());
        assertTrue(columns.get(0).isListVisibleColumn());
        assertEquals(MetaColumnType.DECIMAL, columns.get(1).getDataType());
        assertEquals(1, columns.get(0).getSort());
        assertEquals(2, columns.get(1).getSort());
        for (MetaColumn column : columns) {
            assertEquals(table.getId(), column.getTableId());
            assertFalse(column.getDeleted());
        }
    }
}
