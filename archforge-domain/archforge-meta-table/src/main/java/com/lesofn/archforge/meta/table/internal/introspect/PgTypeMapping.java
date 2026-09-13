package com.lesofn.archforge.meta.table.internal.introspect;

import com.lesofn.archforge.meta.table.api.domain.MetaColumnType;
import org.jspecify.annotations.Nullable;

/**
 * PostgreSQL 列类型 → {@link MetaColumnType} 的反向映射。
 *
 * <p>
 * 以 information_schema 的 {@code udt_name} 为准（数组类型为 {@code _xxx} 形式）。
 * 无法映射的类型返回 {@code null}，由调用方判定为不兼容。
 */
public final class PgTypeMapping {

    private PgTypeMapping() {
    }

    /**
     * 映射结果。
     *
     * @param type 元字段类型
     * @param length varchar 长度（非 varchar 系为 null）
     * @param precision numeric 精度
     * @param scale numeric 小数位
     * @param arrayElementType ARRAY 元素类型名（STRING/INTEGER/DECIMAL/BOOLEAN）
     */
    public record MappedType(
            MetaColumnType type,
            @Nullable Integer length,
            @Nullable Integer precision,
            @Nullable Integer scale,
            @Nullable String arrayElementType) {

        static MappedType of(MetaColumnType type) {
            return new MappedType(type, null, null, null, null);
        }
    }

    /**
     * 映射 PG 列类型。
     *
     * @param udtName information_schema.columns.udt_name（如 varchar/int8/_int4）
     * @param charLen character_maximum_length，可空
     * @param precision numeric_precision，可空
     * @param scale numeric_scale，可空
     * @return 映射结果；不支持的类型返回 null
     */
    public static @Nullable MappedType map(
            String udtName, @Nullable Integer charLen, @Nullable Integer precision, @Nullable Integer scale) {
        return switch (udtName) {
            case "varchar", "bpchar" -> new MappedType(MetaColumnType.STRING, charLen, null, null, null);
            case "text", "name", "char" -> MappedType.of(MetaColumnType.TEXT);
            case "int2", "int4", "int8" -> MappedType.of(MetaColumnType.INTEGER);
            case "float4", "float8", "numeric" -> new MappedType(MetaColumnType.DECIMAL, null, precision, scale, null);
            case "bool" -> MappedType.of(MetaColumnType.BOOLEAN);
            case "date" -> MappedType.of(MetaColumnType.DATE);
            case "timestamp" -> MappedType.of(MetaColumnType.DATETIME);
            case "timestamptz" -> MappedType.of(MetaColumnType.TIMESTAMPTZ);
            case "json", "jsonb" -> MappedType.of(MetaColumnType.JSON);
            case "uuid" -> MappedType.of(MetaColumnType.UUID);
            default -> udtName.startsWith("_") ? mapArray(udtName.substring(1)) : null;
        };
    }

    private static @Nullable MappedType mapArray(String elementUdt) {
        String elementType = switch (elementUdt) {
            case "varchar", "bpchar", "text", "name", "char" -> "STRING";
            case "int2", "int4", "int8" -> "INTEGER";
            case "float4", "float8", "numeric" -> "DECIMAL";
            case "bool" -> "BOOLEAN";
            default -> null;
        };
        return elementType == null
                ? null
                : new MappedType(MetaColumnType.ARRAY, null, null, null, elementType);
    }
}
