package com.lesofn.archforge.meta.table.internal.validator;

import com.lesofn.archforge.meta.table.api.domain.MetaColumn;
import com.lesofn.archforge.meta.table.api.domain.MetaColumnType;
import com.lesofn.archforge.meta.table.api.domain.MetaTable;
import com.lesofn.archforge.meta.table.api.domain.OptionItem;
import com.lesofn.archforge.meta.table.internal.ddl.SqlIdentifier;
import com.lesofn.archforge.meta.table.internal.exception.MetaTableErrorCode;
import com.lesofn.archforge.meta.table.internal.exception.MetaTableException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/**
 * 元表格定义与数据校验器。
 */
@Component
public class MetaTableValidator {

    private static final String DATE_PATTERN = "yyyy-MM-dd";
    private static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    /** 校验表定义与字段定义。 */
    public void validate(MetaTable table, List<MetaColumn> columns) {
        if (table == null || table.getTableCode() == null || table.getTableCode().isEmpty()) {
            throw new MetaTableException(MetaTableErrorCode.META_TABLE_CODE_INVALID);
        }
        SqlIdentifier.validateTableCode(table.getTableCode());

        if (columns == null || columns.isEmpty()) {
            throw new MetaTableException(MetaTableErrorCode.META_COLUMN_TYPE_INVALID, "至少需要定义一个字段");
        }

        Set<String> columnCodes = new HashSet<>();
        for (MetaColumn column : columns) {
            if (column.getColumnCode() == null || column.getColumnCode().isEmpty()) {
                throw new MetaTableException(MetaTableErrorCode.META_COLUMN_CODE_INVALID);
            }
            SqlIdentifier.validateColumnCode(column.getColumnCode());
            if (!columnCodes.add(column.getColumnCode())) {
                throw new MetaTableException(MetaTableErrorCode.META_COLUMN_CODE_INVALID, "字段编码重复: " + column.getColumnCode());
            }
            if (column.getDataType() == null) {
                throw new MetaTableException(MetaTableErrorCode.META_COLUMN_TYPE_INVALID);
            }
            validateColumnConfig(column);
        }
    }

    /** 校验数据行值。 */
    public void validateValues(Map<String, Object> row, List<MetaColumn> columns, boolean insert) {
        for (MetaColumn column : columns) {
            Object value = row.get(column.getColumnCode());
            if (insert && Boolean.TRUE.equals(column.getRequired()) && isEmpty(value)) {
                throw new MetaTableException(MetaTableErrorCode.META_COLUMN_VALUE_INVALID, column.getColumnName() + " 不能为空");
            }
            if (value == null) {
                continue;
            }
            validateValue(column, value);
        }
    }

    private void validateColumnConfig(MetaColumn column) {
        MetaColumnType type = column.getDataType();
        if (type == MetaColumnType.DECIMAL) {
            if (column.getPrecision() != null && column.getPrecision() < column.getScale()) {
                throw new MetaTableException(MetaTableErrorCode.META_COLUMN_TYPE_INVALID, "精度必须大于等于小数位数");
            }
        }
        if (type == MetaColumnType.ENUM && (column.getOptions() == null || column.getOptions().isEmpty())) {
            throw new MetaTableException(MetaTableErrorCode.META_COLUMN_TYPE_INVALID, "枚举类型必须配置选项");
        }
    }

    private void validateValue(MetaColumn column, Object value) {
        MetaColumnType type = column.getDataType();
        try {
            switch (type) {
                case STRING, TEXT, FILE -> validateString(column, value);
                case ENUM -> validateEnum(column, value);
                case INTEGER -> parseLong(value);
                case DECIMAL -> new BigDecimal(value.toString());
                case BOOLEAN -> parseBoolean(value);
                case DATE -> LocalDate.parse(value.toString());
                case DATETIME -> LocalDateTime.parse(value.toString().replace(' ', 'T'));
                case JSON -> {
                    // 仅校验非空字符串；复杂 JSON 由数据库保证
                    if (!(value instanceof String) || ((String) value).trim().isEmpty()) {
                        throw new MetaTableException(MetaTableErrorCode.META_COLUMN_VALUE_INVALID, column.getColumnName() +
                                " JSON 格式错误");
                    }
                }
            }
        } catch (DateTimeParseException | NumberFormatException e) {
            throw new MetaTableException(MetaTableErrorCode.META_COLUMN_VALUE_INVALID, column.getColumnName() + " 格式错误: " +
                    value);
        }
    }

    private void validateString(MetaColumn column, Object value) {
        String str = value.toString();
        if (column.getLength() != null && column.getLength() > 0 && str.length() > column.getLength()) {
            throw new MetaTableException(MetaTableErrorCode.META_COLUMN_VALUE_INVALID, column.getColumnName() + " 超过最大长度 " +
                    column.getLength());
        }
    }

    private void validateEnum(MetaColumn column, Object value) {
        if (column.getOptions() == null || column.getOptions().isEmpty()) {
            return;
        }
        Set<Object> optionValues = column.getOptions().stream()
                .map(OptionItem::getValue)
                .collect(Collectors.toSet());
        if (!optionValues.contains(value) && !optionValues.contains(value.toString())) {
            throw new MetaTableException(MetaTableErrorCode.META_COLUMN_VALUE_INVALID, column.getColumnName() + " 不是有效枚举值");
        }
    }

    private long parseLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        return Long.parseLong(value.toString());
    }

    private boolean parseBoolean(Object value) {
        if (value instanceof Boolean b) {
            return b;
        }
        String str = value.toString().trim();
        return "true".equalsIgnoreCase(str) || "1".equals(str);
    }

    private boolean isEmpty(Object value) {
        return value == null || (value instanceof String string && string.isEmpty());
    }

    /** 根据字段类型转换值为适合数据库存储的类型。 */
    public Object convertValue(MetaColumn column, Object value) {
        if (value == null) {
            return null;
        }
        return switch (column.getDataType()) {
            case STRING, TEXT, FILE, ENUM -> value.toString();
            case INTEGER -> parseLong(value);
            case DECIMAL -> new BigDecimal(value.toString());
            case BOOLEAN -> parseBoolean(value);
            case DATE -> java.sql.Date.valueOf(LocalDate.parse(value.toString()));
            case DATETIME -> java.sql.Timestamp.valueOf(LocalDateTime.parse(value.toString().replace(' ', 'T')));
            case JSON -> value.toString();
        };
    }

    /** 解析日期/时间字符串为显示文本。 */
    public String formatValue(MetaColumn column, Object value) {
        if (value == null) {
            return "";
        }
        return switch (column.getDataType()) {
            case DATE, DATETIME -> value.toString();
            case BOOLEAN -> Boolean.TRUE.equals(value) ? "是" : "否";
            case ENUM -> {
                if (column.getOptions() == null) {
                    yield value.toString();
                }
                yield column.getOptions().stream()
                        .filter(o -> Objects.equals(o.getValue(), value) || Objects.equals(o.getValue().toString(), value
                                .toString()))
                        .findFirst()
                        .map(OptionItem::getLabel)
                        .orElse(value.toString());
            }
            default -> value.toString();
        };
    }
}
