package com.lesofn.archforge.meta.table.internal.ddl;

import com.lesofn.archforge.meta.table.api.domain.MetaColumn;
import com.lesofn.archforge.meta.table.api.domain.MetaColumnType;
import com.lesofn.archforge.meta.table.internal.exception.MetaTableErrorCode;
import com.lesofn.archforge.meta.table.internal.exception.MetaTableException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * 元表格列类型与默认值解析器。
 */
@Component
public class ColumnTypeResolver {

    private static final int DEFAULT_VARCHAR_LENGTH = 255;
    private static final int DEFAULT_DECIMAL_PRECISION = 18;
    private static final int DEFAULT_DECIMAL_SCALE = 2;

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

    /** 根据字段类型格式化默认值。 */
    public String formatDefaultValue(MetaColumn column) {
        String value = column.getDefaultValue();
        if (value == null || value.isEmpty()) {
            return "NULL";
        }
        return switch (column.getDataType()) {
            case STRING, TEXT, ENUM -> "'" + value.replace("'", "''") + "'";
            case FILE, IMAGE, REFERENCE -> value;
            case INTEGER -> value;
            case DECIMAL -> value;
            case BOOLEAN -> Boolean.parseBoolean(value) ? "TRUE" : "FALSE";
            case DATE -> "'" + value + "'";
            case DATETIME -> "'" + value + "'";
            case TIMESTAMPTZ -> "'" + value + "'::timestamptz";
            case JSON, GEO, MULTI_IMAGE -> "'" + value.replace("'", "''") + "'" + "::jsonb";
            case UUID -> "'" + value.replace("'", "''") + "'" + "::uuid";
            case ARRAY -> formatArrayDefaultValue(column);
        };
    }

    private int resolveVarcharLength(MetaColumn column) {
        return (column.getLength() == null || column.getLength() <= 0)
                ? DEFAULT_VARCHAR_LENGTH
                : column.getLength();
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
        return switch (elementType.toUpperCase()) {
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
        String value = column.getDefaultValue();
        if (value == null || value.isEmpty()) {
            return "NULL";
        }
        List<String> elements = parseArrayElements(value);
        String elementType = column.getArrayElementType();
        if (elementType == null || elementType.isEmpty()) {
            elementType = "STRING";
        }
        return switch (elementType.toUpperCase()) {
            case "STRING" -> "ARRAY[" + elements.stream().map(e -> "'" + e.replace("'", "''") + "'").reduce((a, b) -> a + ", " +
                    b).orElse("") + "]";
            case "INTEGER" -> "ARRAY[" + elements.stream().reduce((a, b) -> a + ", " + b).orElse("") + "]::bigint[]";
            case "DECIMAL" -> "ARRAY[" + elements.stream().reduce((a, b) -> a + ", " + b).orElse("") + "]::numeric[]";
            case "BOOLEAN" -> "ARRAY[" + elements.stream().map(String::toLowerCase).reduce((a, b) -> a + ", " + b).orElse("") +
                    "]";
            default -> "ARRAY[" + elements.stream().map(e -> "'" + e.replace("'", "''") + "'").reduce((a, b) -> a + ", " + b)
                    .orElse("") + "]";
        };
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
