package com.lesofn.archforge.meta.table.api.ddl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.lesofn.archforge.meta.table.api.domain.MetaColumn;
import com.lesofn.archforge.meta.table.api.domain.MetaColumnType;
import com.lesofn.archforge.meta.table.api.errors.MetaTableException;
import java.util.List;
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

    /** The default value is spliced into CREATE/ALTER TABLE — unquoted branches must be typed, not passed through. */
    @Test
    void unquotedDefaultsRejectInjectionPayloads() {
        for (MetaColumnType type : List.of(MetaColumnType.INTEGER, MetaColumnType.DECIMAL, MetaColumnType.FILE,
                MetaColumnType.IMAGE, MetaColumnType.REFERENCE)) {
            for (String payload : INJECTIONS) {
                MetaColumn column = newColumn(type);
                column.setDefaultValue(payload);
                assertThrows(MetaTableException.class, () -> resolver.formatDefaultValue(column), type + " <- " + payload);
            }
        }
    }

    @Test
    void numericDefaultsRenderCanonically() {
        assertEquals("42", defaultOf(MetaColumnType.INTEGER, " 42 "));
        assertEquals("-7", defaultOf(MetaColumnType.REFERENCE, "-7"));
        assertEquals("1.50", defaultOf(MetaColumnType.DECIMAL, "1.50"));
        assertEquals("100", defaultOf(MetaColumnType.DECIMAL, "1E+2"));
        assertEquals("-0.5", defaultOf(MetaColumnType.DECIMAL, "-0.5"));
    }

    @Test
    void quotedTemporalDefaultsEscapeEmbeddedQuotes() {
        String payload = "2020-01-01'; DROP TABLE sys_user; --";
        String escaped = "'2020-01-01''; DROP TABLE sys_user; --'";
        assertEquals(escaped, defaultOf(MetaColumnType.DATE, payload));
        assertEquals(escaped, defaultOf(MetaColumnType.DATETIME, payload));
        assertEquals(escaped + "::timestamptz", defaultOf(MetaColumnType.TIMESTAMPTZ, payload));
        assertEquals("'2020-01-01'", defaultOf(MetaColumnType.DATE, "2020-01-01"));
    }

    @Test
    void typedArrayDefaultsRejectInjectionPayloads() {
        for (String elementType : List.of("INTEGER", "DECIMAL", "BOOLEAN")) {
            for (String payload : INJECTIONS) {
                MetaColumn column = newColumn(MetaColumnType.ARRAY);
                column.setArrayElementType(elementType);
                column.setDefaultValue("[" + payload.replace(",", ";") + "]");
                assertThrows(MetaTableException.class, () -> resolver.formatDefaultValue(column), elementType + " <- " +
                        payload);
            }
        }
    }

    @Test
    void arrayDefaultsRenderTypedElements() {
        MetaColumn ints = newColumn(MetaColumnType.ARRAY);
        ints.setArrayElementType("INTEGER");
        ints.setDefaultValue("[1, 2, 3]");
        assertEquals("ARRAY[1, 2, 3]::bigint[]", resolver.formatDefaultValue(ints));

        MetaColumn bools = newColumn(MetaColumnType.ARRAY);
        bools.setArrayElementType("BOOLEAN");
        bools.setDefaultValue("[TRUE, 0, false]");
        assertEquals("ARRAY[true, false, false]", resolver.formatDefaultValue(bools));

        MetaColumn strings = newColumn(MetaColumnType.ARRAY);
        strings.setArrayElementType("STRING");
        strings.setDefaultValue("[\"a'b\", c]");
        assertEquals("ARRAY['a''b', 'c']", resolver.formatDefaultValue(strings));
    }

    private static final List<String> INJECTIONS = List.of(
            "1); DROP TABLE sys_user; --",
            "0 CHECK (amount >= 0)",
            "1 /* x */",
            "1'",
            "abc",
            "0x10",
            "1 2");

    private String defaultOf(MetaColumnType type, String value) {
        MetaColumn column = newColumn(type);
        column.setDefaultValue(value);
        return resolver.formatDefaultValue(column);
    }

    private MetaColumn newColumn(MetaColumnType type) {
        MetaColumn column = new MetaColumn();
        column.setDataType(type);
        return column;
    }
}
