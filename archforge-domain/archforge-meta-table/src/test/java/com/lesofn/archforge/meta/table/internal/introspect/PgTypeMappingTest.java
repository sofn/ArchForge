package com.lesofn.archforge.meta.table.internal.introspect;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.lesofn.archforge.meta.table.api.domain.MetaColumnType;
import com.lesofn.archforge.meta.table.internal.introspect.PgTypeMapping.MappedType;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/** PG udt_name → MetaColumnType mapping matrix. */
class PgTypeMappingTest {

    static Stream<Arguments> mappings() {
        return Stream.of(
                Arguments.of("varchar", MetaColumnType.STRING),
                Arguments.of("bpchar", MetaColumnType.STRING),
                Arguments.of("text", MetaColumnType.TEXT),
                Arguments.of("int2", MetaColumnType.INTEGER),
                Arguments.of("int4", MetaColumnType.INTEGER),
                Arguments.of("int8", MetaColumnType.INTEGER),
                Arguments.of("float4", MetaColumnType.DECIMAL),
                Arguments.of("float8", MetaColumnType.DECIMAL),
                Arguments.of("numeric", MetaColumnType.DECIMAL),
                Arguments.of("bool", MetaColumnType.BOOLEAN),
                Arguments.of("date", MetaColumnType.DATE),
                Arguments.of("timestamp", MetaColumnType.DATETIME),
                Arguments.of("timestamptz", MetaColumnType.TIMESTAMPTZ),
                Arguments.of("json", MetaColumnType.JSON),
                Arguments.of("jsonb", MetaColumnType.JSON),
                Arguments.of("uuid", MetaColumnType.UUID));
    }

    @ParameterizedTest
    @MethodSource("mappings")
    void scalarTypesMap(String udt, MetaColumnType expected) {
        MappedType mapped = PgTypeMapping.map(udt, null, null, null);
        assertNotNull(mapped, udt);
        assertEquals(expected, mapped.type());
    }

    @Test
    void varcharCarriesLength() {
        MappedType mapped = PgTypeMapping.map("varchar", 128, null, null);
        assertNotNull(mapped);
        assertEquals(128, mapped.length());
    }

    @Test
    void numericCarriesPrecisionScale() {
        MappedType mapped = PgTypeMapping.map("numeric", null, 10, 4);
        assertNotNull(mapped);
        assertEquals(10, mapped.precision());
        assertEquals(4, mapped.scale());
    }

    static Stream<Arguments> arrayMappings() {
        return Stream.of(
                Arguments.of("_varchar", "STRING"),
                Arguments.of("_text", "STRING"),
                Arguments.of("_int4", "INTEGER"),
                Arguments.of("_int8", "INTEGER"),
                Arguments.of("_numeric", "DECIMAL"),
                Arguments.of("_bool", "BOOLEAN"));
    }

    @ParameterizedTest
    @MethodSource("arrayMappings")
    void arrayTypesMapToArrayWithElement(String udt, String elementType) {
        MappedType mapped = PgTypeMapping.map(udt, null, null, null);
        assertNotNull(mapped, udt);
        assertEquals(MetaColumnType.ARRAY, mapped.type());
        assertEquals(elementType, mapped.arrayElementType());
    }

    static Stream<Arguments> unsupported() {
        return Stream.of(
                Arguments.of("bytea"),
                Arguments.of("inet"),
                Arguments.of("money"),
                Arguments.of("interval"),
                Arguments.of("point"),
                Arguments.of("oid"),
                Arguments.of("_bytea"),
                Arguments.of("_jsonb"));
    }

    @ParameterizedTest
    @MethodSource("unsupported")
    void unsupportedTypesReturnNull(String udt) {
        assertNull(PgTypeMapping.map(udt, null, null, null));
    }
}
