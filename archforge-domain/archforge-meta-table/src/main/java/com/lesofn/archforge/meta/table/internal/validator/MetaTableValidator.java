package com.lesofn.archforge.meta.table.internal.validator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lesofn.archforge.meta.table.api.dao.MetaColumnRepository;
import com.lesofn.archforge.meta.table.api.dao.MetaTableRepository;
import com.lesofn.archforge.meta.table.api.domain.MetaColumn;
import com.lesofn.archforge.meta.table.api.domain.MetaColumnType;
import com.lesofn.archforge.meta.table.api.domain.MetaTable;
import com.lesofn.archforge.meta.table.api.domain.OptionItem;
import com.lesofn.archforge.meta.table.api.service.DictionaryProvider;
import com.lesofn.archforge.meta.table.internal.ddl.SqlIdentifier;
import com.lesofn.archforge.meta.table.api.errors.MetaTableErrorCode;
import com.lesofn.archforge.meta.table.api.errors.MetaTableException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import java.math.BigDecimal;
import java.sql.Array;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.postgresql.util.PGobject;
import org.springframework.stereotype.Component;

/**
 * 元表格定义与数据校验器。
 */
@Component
public class MetaTableValidator {

    private DictionaryProvider dictionaryProvider;
    private NamedParameterJdbcTemplate jdbcTemplate;
    private MetaTableRepository metaTableRepository;
    private MetaColumnRepository metaColumnRepository;

    @Autowired(required = false)
    public void setDictionaryProvider(DictionaryProvider dictionaryProvider) { this.dictionaryProvider = dictionaryProvider; }

    @Autowired(required = false)
    public void setJdbcTemplate(@Qualifier("metaTableJdbcTemplate") NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Autowired(required = false)
    public void setMetaTableRepository(MetaTableRepository metaTableRepository) {
        this.metaTableRepository = metaTableRepository;
    }

    @Autowired(required = false)
    public void setMetaColumnRepository(MetaColumnRepository metaColumnRepository) {
        this.metaColumnRepository = metaColumnRepository;
    }

    private static final String DATE_PATTERN = "yyyy-MM-dd";
    private static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    private static final String TIMESTAMPTZ_PATTERN = "yyyy-MM-dd HH:mm:ssXXX";
    private static final DateTimeFormatter TIMESTAMPTZ_FORMATTER = DateTimeFormatter.ofPattern(TIMESTAMPTZ_PATTERN);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

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
        Set<String> groupNames = new HashSet<>();
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
            validateIndexGroup(column, columns, groupNames);
        }
        validateReferenceTargets(table, columns);
    }

    /** 校验数据行值。 */
    public void validateValues(Map<String, Object> row, List<MetaColumn> columns, boolean insert) {
        for (MetaColumn column : columns) {
            Object value = row.get(column.getColumnCode());
            boolean provided = row.containsKey(column.getColumnCode());
            if (Boolean.TRUE.equals(column.getRequired()) && (insert || provided) && isEmpty(value)) {
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
        if (type == MetaColumnType.ENUM && (column.getDictCode() == null || column.getDictCode().isEmpty())) {
            throw new MetaTableException(MetaTableErrorCode.META_COLUMN_TYPE_INVALID, "枚举类型必须选择字典");
        }
        if (type == MetaColumnType.REFERENCE) {
            if (column.getReferenceTable() == null || column.getReferenceTable().isEmpty()) {
                throw new MetaTableException(MetaTableErrorCode.META_COLUMN_TYPE_INVALID, "关联类型必须填写被关联表");
            }
            if (column.getReferenceColumn() == null || column.getReferenceColumn().isEmpty()) {
                column.setReferenceColumn("id");
            }
            if (column.getDisplayExpression() == null || column.getDisplayExpression().isEmpty()) {
                throw new MetaTableException(MetaTableErrorCode.META_COLUMN_TYPE_INVALID, "关联类型必须填写显示表达式");
            }
            validateDisplayExpression(column.getDisplayExpression());
        }
        if (type == MetaColumnType.ARRAY && (column.getArrayElementType() == null || column.getArrayElementType().isEmpty())) {
            throw new MetaTableException(MetaTableErrorCode.META_COLUMN_TYPE_INVALID, "ARRAY 类型必须配置元素类型");
        }
        if (column.getSearchType() != null && !column.getSearchType().isEmpty()) {
            Set<String> validSearchTypes = Set.of("EXACT", "LIKE", "RANGE");
            if (!validSearchTypes.contains(column.getSearchType().toUpperCase())) {
                throw new MetaTableException(MetaTableErrorCode.META_COLUMN_TYPE_INVALID, "不支持的搜索方式: " + column
                        .getSearchType());
            }
        }
        if (column.getIndexType() != null && !column.getIndexType().isEmpty()) {
            Set<String> validTypes = Set.of("BTREE", "GIN", "GIST", "FULLTEXT");
            if (!validTypes.contains(column.getIndexType().toUpperCase())) {
                throw new MetaTableException(MetaTableErrorCode.META_COLUMN_TYPE_INVALID, "不支持的索引类型: " + column.getIndexType());
            }
        }
    }

    private static final Set<String> FORBIDDEN_EXPRESSION_KEYWORDS = Set.of(
            "SELECT", "FROM", "WHERE", "JOIN", "UNION", "INTERSECT", "EXCEPT", "WITH",
            "INSERT", "UPDATE", "DELETE", "DROP", "CREATE", "ALTER", "TRUNCATE", "GRANT", "REVOKE",
            "EXEC", "EXECUTE");

    private void validateDisplayExpression(String expression) {
        if (expression == null || expression.isBlank()) {
            return;
        }
        String noStrings = expression.replaceAll("'(?:''|[^'])*'", "''");
        String upper = noStrings.toUpperCase();
        if (upper.contains("--") || upper.contains("/*") || upper.contains("*/") || upper.contains(";")) {
            throw new MetaTableException(MetaTableErrorCode.META_COLUMN_TYPE_INVALID, "显示表达式包含非法字符");
        }
        for (String keyword : FORBIDDEN_EXPRESSION_KEYWORDS) {
            if (upper.matches(".*\\b" + keyword + "\\b.*")) {
                throw new MetaTableException(MetaTableErrorCode.META_COLUMN_TYPE_INVALID, "显示表达式不允许使用关键字: " + keyword);
            }
        }
        if (upper.matches(".*\\b(COUNT|SUM|AVG|MIN|MAX)\\s*\\(.*")) {
            throw new MetaTableException(MetaTableErrorCode.META_COLUMN_TYPE_INVALID, "显示表达式不允许使用聚合函数");
        }
        if (upper.matches(".*\\(\\s*(SELECT|WITH)\\b.*")) {
            throw new MetaTableException(MetaTableErrorCode.META_COLUMN_TYPE_INVALID, "显示表达式不允许包含子查询");
        }
    }

    private void validateReferenceTargets(MetaTable table, List<MetaColumn> columns) {
        if (metaTableRepository == null || metaColumnRepository == null) {
            return;
        }
        for (MetaColumn column : columns) {
            if (column.getDataType() != MetaColumnType.REFERENCE) {
                continue;
            }
            String refTable = column.getReferenceTable();
            String refColumn = column.getReferenceColumn() == null ? "id" : column.getReferenceColumn();
            boolean selfReference = table != null && refTable != null && refTable.equals(table.physicalTableName());
            MetaColumn target = null;
            if (selfReference) {
                target = columns.stream()
                        .filter(c -> c.getColumnCode().equals(refColumn))
                        .findFirst()
                        .orElse(null);
            } else if (refTable != null) {
                target = metaTableRepository.findAllByDeletedFalse().stream()
                        .filter(t -> refTable.equals(t.physicalTableName()))
                        .findFirst()
                        .flatMap(t -> metaColumnRepository.findByTableIdAndDeletedFalseOrderBySortAsc(t.getId()).stream()
                                .filter(c -> c.getColumnCode().equals(refColumn))
                                .findFirst())
                        .orElse(null);
            }
            boolean primaryKeyRef = "id".equals(refColumn);
            if (target == null && !primaryKeyRef) {
                throw new MetaTableException(MetaTableErrorCode.META_COLUMN_TYPE_INVALID, "关联字段不存在于被关联表元数据中: " + refTable +
                        "." + refColumn);
            }
            if (target != null && !Boolean.TRUE.equals(target.getUnique())) {
                throw new MetaTableException(MetaTableErrorCode.META_COLUMN_TYPE_INVALID, "关联字段必须唯一，否则关联查询会产生重复行: " + refTable +
                        "." + refColumn);
            }
        }
    }

    private void validateIndexGroup(MetaColumn column, List<MetaColumn> columns, Set<String> groupNames) {
        String group = column.getIndexGroup();
        if (group == null || group.isEmpty()) {
            return;
        }
        if (!isIndexEnabled(column)) {
            throw new MetaTableException(MetaTableErrorCode.META_COLUMN_TYPE_INVALID, "字段 " + column.getColumnCode() +
                    " 加入索引组但未启用索引");
        }
        if (!groupNames.add(group)) {
            return;
        }
        List<MetaColumn> members = columns.stream()
                .filter(c -> group.equals(c.getIndexGroup()))
                .toList();
        String indexType = null;
        boolean first = true;
        for (MetaColumn member : members) {
            String memberType = member.getIndexType();
            if (memberType == null || memberType.isEmpty()) {
                memberType = "BTREE";
            }
            if (first) {
                indexType = memberType;
                first = false;
            } else if (!indexType.equalsIgnoreCase(memberType)) {
                throw new MetaTableException(MetaTableErrorCode.META_COLUMN_TYPE_INVALID, "索引组 " + group + " 内的字段索引类型必须一致");
            }
        }
    }

    private boolean isIndexEnabled(MetaColumn column) {
        return Boolean.TRUE.equals(column.getUnique()) || Boolean.TRUE.equals(column.getIndex());
    }

    private void validateValue(MetaColumn column, Object value) {
        MetaColumnType type = column.getDataType();
        try {
            switch (type) {
                case STRING, TEXT -> validateString(column, value);
                case FILE, IMAGE -> validateFileReference(column, value);
                case MULTI_IMAGE -> validateMultiImage(value);
                case ENUM -> validateEnum(column, value);
                case INTEGER -> parseLong(value);
                case REFERENCE -> validateReference(column, value);
                case DECIMAL -> new BigDecimal(value.toString());
                case BOOLEAN -> parseBoolean(value);
                case DATE -> LocalDate.parse(value.toString());
                case DATETIME -> LocalDateTime.parse(value.toString().replace(' ', 'T'));
                case UUID -> UUID.fromString(value.toString());
                case TIMESTAMPTZ -> parseTimestampTz(value.toString());
                case JSON -> validateJson(value);
                case GEO -> validateGeo(value);
                case ARRAY -> validateArray(column, value);
            }
        } catch (DateTimeParseException | IllegalArgumentException e) {
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

    private void validateFileReference(MetaColumn column, Object value) {
        if (value instanceof Number) {
            return;
        }
        String str = value.toString();
        if (str.isBlank()) {
            throw new MetaTableException(MetaTableErrorCode.META_COLUMN_VALUE_INVALID, column.getColumnName() + " 文件引用不能为空");
        }
        try {
            Long.parseLong(str);
        } catch (NumberFormatException e) {
            throw new MetaTableException(MetaTableErrorCode.META_COLUMN_VALUE_INVALID, column.getColumnName() +
                    " 文件引用必须是 fileId");
        }
    }

    private void validateMultiImage(Object value) {
        if (value instanceof Collection<?>) {
            return;
        }
        String str = value.toString().trim();
        if (str.isEmpty()) {
            throw new MetaTableException(MetaTableErrorCode.META_COLUMN_VALUE_INVALID, "多图片不能为空");
        }
        try {
            JsonNode node = OBJECT_MAPPER.readTree(str);
            if (!node.isArray()) {
                throw new MetaTableException(MetaTableErrorCode.META_COLUMN_VALUE_INVALID, "多图片必须是 JSON 数组");
            }
        } catch (Exception e) {
            throw new MetaTableException(MetaTableErrorCode.META_COLUMN_VALUE_INVALID, "多图片格式错误");
        }
    }

    private void validateEnum(MetaColumn column, Object value) {
        List<OptionItem> items = findDictItems(column);
        if (items == null || items.isEmpty()) {
            return;
        }
        Set<Object> optionValues = items.stream()
                .map(OptionItem::getValue)
                .collect(Collectors.toSet());
        if (!optionValues.contains(value) && !optionValues.contains(value.toString())) {
            throw new MetaTableException(MetaTableErrorCode.META_COLUMN_VALUE_INVALID, column.getColumnName() + " 不是有效枚举值");
        }
    }

    private void validateReference(MetaColumn column, Object value) {
        long id = parseLong(value);
        if (jdbcTemplate == null) {
            return;
        }
        String table = column.getReferenceTable();
        String refColumn = column.getReferenceColumn();
        if (table == null || table.isEmpty() || refColumn == null || refColumn.isEmpty()) {
            throw new MetaTableException(MetaTableErrorCode.META_COLUMN_VALUE_INVALID, column.getColumnName() + " 关联配置不完整");
        }
        String sql = "SELECT COUNT(*) FROM " + SqlIdentifier.quote(table) + " WHERE " + SqlIdentifier.quote(refColumn) +
                " = :id";
        Map<String, Object> params = Map.of("id", id);
        Integer count = jdbcTemplate.queryForObject(sql, params, Integer.class);
        if (count == null || count == 0) {
            throw new MetaTableException(MetaTableErrorCode.META_COLUMN_VALUE_INVALID, column.getColumnName() + " 引用的记录不存在: " +
                    id);
        }
    }

    private List<OptionItem> findDictItems(MetaColumn column) {
        if (column.getDictCode() == null || column.getDictCode().isEmpty()) {
            return column.getOptions();
        }
        if (dictionaryProvider == null) {
            return column.getOptions();
        }
        return dictionaryProvider.findItems(column.getDictCode());
    }

    private void validateJson(Object value) {
        if (!(value instanceof String)) {
            return;
        }
        String str = ((String) value).trim();
        if (str.isEmpty()) {
            throw new MetaTableException(MetaTableErrorCode.META_COLUMN_VALUE_INVALID, "JSON 不能为空");
        }
        try {
            OBJECT_MAPPER.readTree(str);
        } catch (Exception e) {
            throw new MetaTableException(MetaTableErrorCode.META_COLUMN_VALUE_INVALID, "JSON 格式错误");
        }
    }

    private void validateGeo(Object value) {
        JsonNode node = parseJsonNode(value);
        if (!node.has("lat") || !node.has("lng")) {
            throw new MetaTableException(MetaTableErrorCode.META_COLUMN_VALUE_INVALID, "地理位置必须包含 lat 和 lng");
        }
        if (!node.get("lat").isNumber() || !node.get("lng").isNumber()) {
            throw new MetaTableException(MetaTableErrorCode.META_COLUMN_VALUE_INVALID, "lat 和 lng 必须是数字");
        }
    }

    private void validateArray(MetaColumn column, Object value) {
        List<String> elements = parseArrayElements(value);
        String elementType = column.getArrayElementType() == null ? "STRING" : column.getArrayElementType().toUpperCase();
        for (String element : elements) {
            switch (elementType) {
                case "INTEGER" -> Long.parseLong(element);
                case "DECIMAL" -> new BigDecimal(element);
                case "BOOLEAN" -> parseBooleanString(element);
                case "STRING" -> {
                    // 字符串无需额外校验
                }
                default -> throw new MetaTableException(MetaTableErrorCode.META_COLUMN_VALUE_INVALID, "不支持的数组元素类型: " +
                        elementType);
            }
        }
    }

    private JsonNode parseJsonNode(Object value) {
        try {
            if (value instanceof String) {
                return OBJECT_MAPPER.readTree((String) value);
            }
            return OBJECT_MAPPER.valueToTree(value);
        } catch (Exception e) {
            throw new MetaTableException(MetaTableErrorCode.META_COLUMN_VALUE_INVALID, "JSON 格式错误: " + value);
        }
    }

    private List<String> parseArrayElements(Object value) {
        if (value instanceof Collection<?> collection) {
            return collection.stream().map(Object::toString).toList();
        }
        if (value instanceof String string) {
            String trimmed = string.trim();
            if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
                trimmed = trimmed.substring(1, trimmed.length() - 1);
            }
            if (trimmed.isEmpty()) {
                return List.of();
            }
            return Arrays.stream(trimmed.split(","))
                    .map(s -> {
                        String e = s.trim();
                        if (e.startsWith("\"") && e.endsWith("\"")) {
                            e = e.substring(1, e.length() - 1);
                        }
                        return e;
                    })
                    .toList();
        }
        throw new MetaTableException(MetaTableErrorCode.META_COLUMN_VALUE_INVALID, "ARRAY 值格式错误: " + value);
    }

    private OffsetDateTime parseTimestampTz(String value) {
        String normalized = value.trim().replace(' ', 'T');
        try {
            return OffsetDateTime.parse(normalized);
        } catch (DateTimeParseException e) {
            return OffsetDateTime.parse(value, TIMESTAMPTZ_FORMATTER);
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
        return parseBooleanString(value.toString());
    }

    private boolean parseBooleanString(String value) {
        String str = value.trim();
        if ("true".equalsIgnoreCase(str) || "1".equals(str)) {
            return true;
        }
        if ("false".equalsIgnoreCase(str) || "0".equals(str)) {
            return false;
        }
        throw new IllegalArgumentException("Invalid boolean value: " + value);
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
            case STRING, TEXT, ENUM -> value.toString();
            case FILE, IMAGE -> parseLong(value);
            case MULTI_IMAGE -> toPgJsonb(toJsonString(value));
            case INTEGER, REFERENCE -> parseLong(value);
            case DECIMAL -> new BigDecimal(value.toString());
            case BOOLEAN -> parseBoolean(value);
            case DATE -> java.sql.Date.valueOf(LocalDate.parse(value.toString()));
            case DATETIME -> java.sql.Timestamp.valueOf(LocalDateTime.parse(value.toString().replace(' ', 'T')));
            case TIMESTAMPTZ -> parseTimestampTz(value.toString());
            case UUID -> UUID.fromString(value.toString());
            case JSON, GEO -> toPgJsonb(value.toString());
            case ARRAY -> toPgArray(column, value);
        };
    }

    private PGobject toPgJsonb(String value) {
        try {
            PGobject pgObject = new PGobject();
            pgObject.setType("jsonb");
            pgObject.setValue(value);
            return pgObject;
        } catch (Exception e) {
            throw new MetaTableException(MetaTableErrorCode.META_COLUMN_VALUE_INVALID, "JSONB 转换失败: " + value);
        }
    }

    private String toJsonString(Object value) {
        if (value instanceof String string) {
            return string;
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(value);
        } catch (Exception e) {
            throw new MetaTableException(MetaTableErrorCode.META_COLUMN_VALUE_INVALID, "JSON 序列化失败: " + value);
        }
    }

    private PGobject toPgArray(MetaColumn column, Object value) {
        List<String> elements = parseArrayElements(value);
        String elementType = column.getArrayElementType() == null ? "STRING" : column.getArrayElementType().toUpperCase();
        String arrayType = switch (elementType) {
            case "INTEGER" -> "bigint[]";
            case "DECIMAL" -> "numeric[]";
            case "BOOLEAN" -> "boolean[]";
            default -> "text[]";
        };
        String arrayValue = buildPgArrayLiteral(elements, elementType);
        try {
            PGobject pgObject = new PGobject();
            pgObject.setType(arrayType);
            pgObject.setValue(arrayValue);
            return pgObject;
        } catch (Exception e) {
            throw new MetaTableException(MetaTableErrorCode.META_COLUMN_VALUE_INVALID, "ARRAY 转换失败: " + value);
        }
    }

    private String buildPgArrayLiteral(List<String> elements, String elementType) {
        if (elements.isEmpty()) {
            return "{}";
        }
        if ("STRING".equalsIgnoreCase(elementType)) {
            return "{" + elements.stream()
                    .map(e -> "\"" + e.replace("\\", "\\\\").replace("\"", "\\\"") + "\"")
                    .collect(Collectors.joining(",")) + "}";
        }
        return "{" + String.join(",", elements) + "}";
    }

    /** 解析日期/时间字符串为显示文本。 */
    public String formatValue(MetaColumn column, Object value) {
        if (value == null) {
            return "";
        }
        return switch (column.getDataType()) {
            case DATE, DATETIME, TIMESTAMPTZ -> value.toString();
            case BOOLEAN -> Boolean.TRUE.equals(value) ? "是" : "否";
            case JSON, GEO, MULTI_IMAGE -> formatJsonValue(value);
            case ARRAY -> formatArrayValue(value);
            case FILE, IMAGE -> value.toString();
            case ENUM -> {
                List<OptionItem> items = findDictItems(column);
                if (items == null || items.isEmpty()) {
                    yield value.toString();
                }
                yield items.stream()
                        .filter(o -> Objects.equals(o.getValue(), value) || Objects.equals(o.getValue().toString(), value
                                .toString()))
                        .findFirst()
                        .map(OptionItem::getLabel)
                        .orElse(value.toString());
            }
            default -> value.toString();
        };
    }

    private String formatJsonValue(Object value) {
        if (value instanceof PGobject pgObject) {
            return pgObject.getValue();
        }
        return value.toString();
    }

    private String formatArrayValue(Object value) {
        if (value instanceof PGobject pgObject) {
            return pgObject.getValue();
        }
        if (value instanceof List<?> list) {
            return list.toString();
        }
        if (value instanceof Array sqlArray) {
            try {
                Object array = sqlArray.getArray();
                if (array instanceof Object[]) {
                    return Arrays.toString((Object[]) array);
                }
                return array.toString();
            } catch (SQLException e) {
                throw new MetaTableException(MetaTableErrorCode.META_COLUMN_VALUE_INVALID, "读取数组字段失败");
            }
        }
        return value.toString();
    }
}
