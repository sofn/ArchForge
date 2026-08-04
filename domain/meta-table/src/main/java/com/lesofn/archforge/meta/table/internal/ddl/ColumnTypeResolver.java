package com.lesofn.archforge.meta.table.internal.ddl;

import com.lesofn.archforge.meta.table.api.domain.MetaColumn;
import com.lesofn.archforge.meta.table.api.domain.MetaColumnType;
import com.lesofn.archforge.meta.table.internal.exception.MetaTableErrorCode;
import com.lesofn.archforge.meta.table.internal.exception.MetaTableException;
import org.springframework.stereotype.Component;

/**
 * 元表格列类型与默认值解析器。
 */
@Component
public class ColumnTypeResolver {

    private static final int DEFAULT_VARCHAR_LENGTH = 255;
    private static final int DEFAULT_FILE_LENGTH = 512;
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
                int length = (column.getLength() == null || column.getLength() <= 0)
                        ? DEFAULT_VARCHAR_LENGTH
                        : column.getLength();
                yield "VARCHAR(" + length + ")";
            }
            case TEXT -> "TEXT";
            case INTEGER -> "BIGINT";
            case DECIMAL -> {
                int precision = (column.getPrecision() == null || column.getPrecision() <= 0)
                        ? DEFAULT_DECIMAL_PRECISION
                        : column.getPrecision();
                int scale = column.getScale() == null ? DEFAULT_DECIMAL_SCALE : column.getScale();
                yield "NUMERIC(" + precision + "," + scale + ")";
            }
            case BOOLEAN -> "BOOLEAN";
            case DATE -> "DATE";
            case DATETIME -> "TIMESTAMP";
            case JSON -> "JSONB";
            case FILE -> "VARCHAR(" + DEFAULT_FILE_LENGTH + ")";
        };
    }

    /** 根据字段类型格式化默认值。 */
    public String formatDefaultValue(MetaColumn column) {
        String value = column.getDefaultValue();
        if (value == null) {
            return "NULL";
        }
        return switch (column.getDataType()) {
            case STRING, TEXT, FILE, ENUM -> "'" + value.replace("'", "''") + "'";
            case INTEGER, DECIMAL -> value;
            case BOOLEAN -> Boolean.parseBoolean(value) ? "TRUE" : "FALSE";
            case DATE, DATETIME -> "'" + value + "'";
            case JSON -> "'" + value.replace("'", "''") + "'::jsonb";
        };
    }
}
