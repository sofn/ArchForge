package com.lesofn.archforge.meta.table.api.ddl;

import com.lesofn.archforge.meta.table.api.domain.MetaColumn;
import com.lesofn.archforge.meta.table.api.domain.MetaColumnType;
import com.lesofn.archforge.meta.table.api.errors.MetaTableErrorCode;
import com.lesofn.archforge.meta.table.api.errors.MetaTableException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

/**
 * 元表格列类型与默认值解析器。
 */
@Component
public class ColumnTypeResolver {

    private static final int DEFAULT_VARCHAR_LENGTH = 255;
    private static final int DEFAULT_DECIMAL_PRECISION = 18;
    private static final int DEFAULT_DECIMAL_SCALE = 2;
    private static final int MAX_DECIMAL_DIGITS = 100;

    /** 根据字段定义解析 PostgreSQL 类型字符串。 */
    public String resolve(MetaColumn column) {
        MetaColumnType type = column.getDataType();
        if (type == null) {
            throw new MetaTableException(MetaTableErrorCode.META_COLUMN_TYPE_INVALID);
        }
        return switch (type) {
            case STRING, ENUM -> {
                int length = resolveVarcharLength(column);
                yield "VARCHAR(" + length + ")";
            }
            case TEXT -> "TEXT";
            case INTEGER -> "BIGINT";
            case DECIMAL -> {
                int precision = resolvePrecision(column);
                int scale = resolveScale(column);
                yield "NUMERIC(" + precision + "," + scale + ")";
            }
            case BOOLEAN -> "BOOLEAN";
            case DATE -> "DATE";
            case DATETIME -> "TIMESTAMP";
            case TIMESTAMPTZ -> "TIMESTAMPTZ";
            case JSON, GEO, MULTI_IMAGE -> "JSONB";
            case FILE, IMAGE, REFERENCE -> "BIGINT";
            case UUID -> "UUID";
            case ARRAY -> resolveArrayType(column);
        };
    }

    /**
     * 根据字段类型格式化默认值。
     *
     * <p>
     * 结果会被直接拼进 CREATE/ALTER TABLE 与回填 UPDATE：带引号的分支一律双写单引号；
     * 数值/布尔分支不带引号，所以必须先按类型解析再以规范形式输出，非法值抛 {@link MetaTableException}，
     * 绝不原样透传。
     */
    public String formatDefaultValue(MetaColumn column) {
        @Nullable
        String value = column.getDefaultValue();
        if (value == null || value.isEmpty()) {
            return "NULL";
        }
        return switch (column.getDataType()) {
            case STRING, TEXT, ENUM, DATE, DATETIME -> quote(value);
            case FILE, IMAGE, REFERENCE, INTEGER -> Long.toString(parseLong(column, value));
            case DECIMAL -> parseDecimal(column, value);
            case BOOLEAN -> Boolean.parseBoolean(value) ? "TRUE" : "FALSE";
            case TIMESTAMPTZ -> quote(value) + "::timestamptz";
            case JSON, GEO, MULTI_IMAGE -> quote(value) + "::jsonb";
            case UUID -> quote(value) + "::uuid";
            case ARRAY -> formatArrayDefaultValue(column);
        };
    }

    private static String quote(String value) {
        return "'" + value.replace("'", "''") + "'";
    }

    private static long parseLong(MetaColumn column, String value) {
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            throw invalidDefault(column, "整数");
        }
    }

    /** 以 {@link BigDecimal#toPlainString()} 输出，数位过长直接拒绝（防 {@code 1E+999999999} 撑爆内存）。 */
    private static String parseDecimal(MetaColumn column, String value) {
        try {
            BigDecimal decimal = new BigDecimal(value.trim());
            if (decimal.scale() > MAX_DECIMAL_DIGITS || decimal.precision() - decimal.scale() > MAX_DECIMAL_DIGITS) {
                throw invalidDefault(column, "小数");
            }
            return decimal.toPlainString();
        } catch (NumberFormatException e) {
            throw invalidDefault(column, "小数");
        }
    }

    private static String parseBoolean(MetaColumn column, String value) {
        String str = value.trim();
        if ("true".equalsIgnoreCase(str) || "1".equals(str)) {
            return "true";
        }
        if ("false".equalsIgnoreCase(str) || "0".equals(str)) {
            return "false";
        }
        throw invalidDefault(column, "布尔值");
    }

    private static MetaTableException invalidDefault(MetaColumn column, String expected) {
        return new MetaTableException(MetaTableErrorCode.META_COLUMN_VALUE_INVALID, "字段 " + column.getColumnCode() +
                " 的默认值不是合法的" + expected);
    }

    private int resolveVarcharLength(MetaColumn column) {
        Integer length = column.getLength();
        return (length == null || length <= 0) ? DEFAULT_VARCHAR_LENGTH : length;
    }

    private int resolvePrecision(MetaColumn column) {
        return (column.getPrecision() == null || column.getPrecision() <= 0)
                ? DEFAULT_DECIMAL_PRECISION
                : column.getPrecision();
    }

    private int resolveScale(MetaColumn column) {
        return column.getScale() == null ? DEFAULT_DECIMAL_SCALE : column.getScale();
    }

    private String resolveArrayType(MetaColumn column) {
        String elementType = column.getArrayElementType();
        if (elementType == null || elementType.isEmpty()) {
            elementType = "STRING";
        }
        return switch (elementType.toUpperCase(Locale.ROOT)) {
            case "STRING" -> {
                int length = resolveVarcharLength(column);
                yield "VARCHAR(" + length + ")[]";
            }
            case "INTEGER" -> "BIGINT[]";
            case "DECIMAL" -> {
                int precision = resolvePrecision(column);
                int scale = resolveScale(column);
                yield "NUMERIC(" + precision + "," + scale + ")[]";
            }
            case "BOOLEAN" -> "BOOLEAN[]";
            default -> "TEXT[]";
        };
    }

    private String formatArrayDefaultValue(MetaColumn column) {
        @Nullable
        String value = column.getDefaultValue();
        if (value == null || value.isEmpty()) {
            return "NULL";
        }
        List<String> elements = parseArrayElements(value);
        String elementType = column.getArrayElementType();
        if (elementType == null || elementType.isEmpty()) {
            elementType = "STRING";
        }
        return switch (elementType.toUpperCase(Locale.ROOT)) {
            case "INTEGER" -> arrayOf(elements, e -> Long.toString(parseLong(column, e))) + "::bigint[]";
            case "DECIMAL" -> arrayOf(elements, e -> parseDecimal(column, e)) + "::numeric[]";
            case "BOOLEAN" -> arrayOf(elements, e -> parseBoolean(column, e));
            default -> arrayOf(elements, ColumnTypeResolver::quote);
        };
    }

    private static String arrayOf(List<String> elements, Function<String, String> render) {
        return "ARRAY[" + elements.stream().map(render).collect(Collectors.joining(", ")) + "]";
    }

    private List<String> parseArrayElements(String value) {
        String trimmed = value.trim();
        if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
            trimmed = trimmed.substring(1, trimmed.length() - 1);
        }
        if (trimmed.isEmpty()) {
            return List.of();
        }
        List<String> result = new ArrayList<>();
        for (String e : Arrays.asList(trimmed.split(","))) {
            String element = e.trim();
            if (element.startsWith("\"") && element.endsWith("\"")) {
                element = element.substring(1, element.length() - 1);
            }
            result.add(element);
        }
        return result;
    }
}
