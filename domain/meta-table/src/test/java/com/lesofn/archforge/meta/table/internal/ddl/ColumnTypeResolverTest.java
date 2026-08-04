package com.lesofn.archforge.meta.table.internal.ddl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.lesofn.archforge.meta.table.api.domain.MetaColumn;
import com.lesofn.archforge.meta.table.api.domain.MetaColumnType;
import org.junit.jupiter.api.Test;

class ColumnTypeResolverTest {

    private final ColumnTypeResolver resolver = new ColumnTypeResolver();

    @Test
    void shouldResolveStringWithLength() {
        MetaColumn column = newColumn(MetaColumnType.STRING);
        column.setLength(100);
        assertEquals("VARCHAR(100)", resolver.resolve(column));
    }

    @Test
    void shouldResolveText() {
        MetaColumn column = newColumn(MetaColumnType.TEXT);
        assertEquals("TEXT", resolver.resolve(column));
    }

    @Test
    void shouldResolveInteger() {
        MetaColumn column = newColumn(MetaColumnType.INTEGER);
        assertEquals("BIGINT", resolver.resolve(column));
    }

    @Test
    void shouldResolveDecimal() {
        MetaColumn column = newColumn(MetaColumnType.DECIMAL);
        column.setPrecision(10);
        column.setScale(2);
        assertEquals("NUMERIC(10,2)", resolver.resolve(column));
    }

    @Test
    void shouldResolveUuid() {
        MetaColumn column = newColumn(MetaColumnType.UUID);
        assertEquals("UUID", resolver.resolve(column));
    }

    @Test
    void shouldResolveTimestamptz() {
        MetaColumn column = newColumn(MetaColumnType.TIMESTAMPTZ);
        assertEquals("TIMESTAMPTZ", resolver.resolve(column));
    }

    @Test
    void shouldResolveJsonAndGeoAsJsonb() {
        MetaColumn json = newColumn(MetaColumnType.JSON);
        assertEquals("JSONB", resolver.resolve(json));

        MetaColumn geo = newColumn(MetaColumnType.GEO);
        assertEquals("JSONB", resolver.resolve(geo));
    }

    @Test
    void shouldResolveArrayByElementType() {
        MetaColumn column = newColumn(MetaColumnType.ARRAY);
        column.setArrayElementType("STRING");
        assertEquals("VARCHAR(255)[]", resolver.resolve(column));

        column.setArrayElementType("INTEGER");
        assertEquals("BIGINT[]", resolver.resolve(column));

        column.setArrayElementType("DECIMAL");
        assertEquals("NUMERIC(18,2)[]", resolver.resolve(column));

        column.setArrayElementType("BOOLEAN");
        assertEquals("BOOLEAN[]", resolver.resolve(column));
    }

    @Test
    void shouldFormatStringDefaultWithQuotes() {
        MetaColumn column = newColumn(MetaColumnType.STRING);
        column.setDefaultValue("test");
        assertEquals("'test'", resolver.formatDefaultValue(column));
    }

    @Test
    void shouldFormatNumericDefaultWithoutQuotes() {
        MetaColumn column = newColumn(MetaColumnType.INTEGER);
        column.setDefaultValue("42");
        assertEquals("42", resolver.formatDefaultValue(column));
    }

    @Test
    void shouldFormatJsonbDefault() {
        MetaColumn column = newColumn(MetaColumnType.JSON);
        column.setDefaultValue("{\"a\":1}");
        assertEquals("'{\"a\":1}'::jsonb", resolver.formatDefaultValue(column));
    }

    private MetaColumn newColumn(MetaColumnType type) {
        MetaColumn column = new MetaColumn();
        column.setDataType(type);
        return column;
    }
}
