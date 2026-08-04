package com.lesofn.archforge.meta.table.internal.generator.handler;

import com.lesofn.archforge.meta.table.api.domain.MetaColumn;
import com.lesofn.archforge.meta.table.api.domain.MetaColumnType;
import com.lesofn.archforge.meta.table.api.domain.OptionItem;
import com.lesofn.archforge.meta.table.internal.generator.CodeGenColumn;
import com.lesofn.archforge.meta.table.internal.generator.CodeGenTypeHandler;
import java.util.ArrayList;
import java.util.List;

public class DefaultCodeGenTypeHandler implements CodeGenTypeHandler {

    @Override
    public boolean supports(MetaColumnType type) {
        return true;
    }

    @Override
    public void enrich(CodeGenColumn col, MetaColumn meta) {
        MetaColumnType type = meta.getDataType();
        setTypeFlags(col, type);
        switch (type) {
            case STRING -> handleString(col, meta);
            case TEXT -> handleText(col, meta);
            case INTEGER -> handleInteger(col, meta);
            case DECIMAL -> handleDecimal(col, meta);
            case BOOLEAN -> handleBoolean(col, meta);
            case DATE -> handleDate(col, meta);
            case DATETIME -> handleDateTime(col, meta);
            case ENUM -> handleEnum(col, meta);
            case JSON -> handleJson(col, meta);
            case FILE -> handleFile(col, meta);
            case UUID -> handleUuid(col, meta);
            case TIMESTAMPTZ -> handleTimestampTz(col, meta);
            case ARRAY -> handleArray(col, meta);
            case GEO -> handleGeo(col, meta);
        }
        col.setSearchCondition(buildSearchCondition(col));
        col.setSearchPredicate(buildSearchPredicate(col));
        col.setKeywordPredicate(buildKeywordPredicate(col));
    }

    private void setTypeFlags(CodeGenColumn col, MetaColumnType type) {
        col.setString(type == MetaColumnType.STRING);
        col.setText(type == MetaColumnType.TEXT);
        col.setJson(type == MetaColumnType.JSON);
        col.setFile(type == MetaColumnType.FILE);
        col.setEnumType(type == MetaColumnType.ENUM);
        col.setInteger(type == MetaColumnType.INTEGER);
        col.setDecimal(type == MetaColumnType.DECIMAL);
        col.setBooleanType(type == MetaColumnType.BOOLEAN);
        col.setDate(type == MetaColumnType.DATE);
        col.setDateTime(type == MetaColumnType.DATETIME);
        col.setUuid(type == MetaColumnType.UUID);
        col.setTimestampTz(type == MetaColumnType.TIMESTAMPTZ);
        col.setArray(type == MetaColumnType.ARRAY);
        col.setGeo(type == MetaColumnType.GEO);
    }

    private void handleString(CodeGenColumn col, MetaColumn meta) {
        col.setJavaType("String");
        col.setTsType("string");
        col.setComponentType("input");
        col.setInputType("text");
        col.setLikeSearch(true);
        col.setKeywordSearchable(true);
        int len = defaultLength(col.getLength(), 255);
        col.setLength(len);
        col.setJavaDefaultValue(escape("测试"));
        col.setTsDefaultValue(escape(""));
        addStringValidators(col, len);
        addColumnAnnotation(col);
    }

    private void handleText(CodeGenColumn col, MetaColumn meta) {
        col.setJavaType("String");
        col.setTsType("string");
        col.setComponentType("input");
        col.setInputType("textarea");
        col.setLikeSearch(true);
        col.setKeywordSearchable(true);
        int len = defaultLength(col.getLength(), 2000);
        col.setLength(len);
        col.setJavaDefaultValue(escape("测试文本"));
        col.setTsDefaultValue(escape(""));
        addStringValidators(col, len);
        addColumnAnnotation(col);
    }

    private void handleInteger(CodeGenColumn col, MetaColumn meta) {
        col.setJavaType("Long");
        col.setTsType("number");
        col.setComponentType("input-number");
        col.setInputType("number");
        col.setJavaDefaultValue("1L");
        col.setTsDefaultValue("0");
        addNotNullValidator(col);
        addColumnAnnotation(col);
    }

    private void handleDecimal(CodeGenColumn col, MetaColumn meta) {
        col.setJavaType("BigDecimal");
        col.setTsType("number");
        col.setComponentType("input-number");
        col.setInputType("number");
        int precision = defaultInt(col.getPrecision(), 10);
        int scale = defaultInt(col.getScale(), 2);
        col.setPrecision(precision);
        col.setScale(scale);
        col.setJavaDefaultValue("new BigDecimal(\"1.00\")");
        col.setTsDefaultValue("0");
        col.getImports().add("java.math.BigDecimal");
        if (col.isRequired()) {
            col.getValidatorAnnotations().add("@NotNull(message = \"" + col.getColumnName() + "不能为空\")");
            int integerPart = Math.max(0, precision - scale);
            col.getValidatorAnnotations().add("@Digits(integer = " + integerPart + ", fraction = " + scale + ", message = \"" +
                    col.getColumnName() + "格式不正确\")");
            col.getValidatorImports().add("jakarta.validation.constraints.NotNull");
            col.getValidatorImports().add("jakarta.validation.constraints.Digits");
        }
        addColumnAnnotation(col);
    }

    private void handleBoolean(CodeGenColumn col, MetaColumn meta) {
        col.setJavaType("Boolean");
        col.setTsType("boolean");
        col.setComponentType("switch");
        col.setInputType("switch");
        col.setJavaDefaultValue("true");
        col.setTsDefaultValue("false");
        addNotNullValidator(col);
        addColumnAnnotation(col);
    }

    private void handleDate(CodeGenColumn col, MetaColumn meta) {
        col.setJavaType("LocalDate");
        col.setTsType("string");
        col.setComponentType("date-picker");
        col.setInputType("date");
        col.setDateType("date");
        col.setDateValueFormat("YYYY-MM-DD");
        col.setJavaDefaultValue("LocalDate.now()");
        col.setTsDefaultValue(escape("2026-01-01"));
        col.getImports().add("java.time.LocalDate");
        addNotNullValidator(col);
        addColumnAnnotation(col);
    }

    private void handleDateTime(CodeGenColumn col, MetaColumn meta) {
        col.setJavaType("LocalDateTime");
        col.setTsType("string");
        col.setComponentType("date-picker");
        col.setInputType("datetime");
        col.setDateType("datetime");
        col.setDateValueFormat("YYYY-MM-DD HH:mm:ss");
        col.setJavaDefaultValue("LocalDateTime.now()");
        col.setTsDefaultValue(escape("2026-01-01 00:00:00"));
        col.getImports().add("java.time.LocalDateTime");
        addNotNullValidator(col);
        addColumnAnnotation(col);
    }

    private void handleEnum(CodeGenColumn col, MetaColumn meta) {
        col.setJavaType("String");
        col.setTsType("string");
        col.setComponentType("select");
        col.setInputType("select");
        col.setLikeSearch(true);
        col.setKeywordSearchable(true);
        int len = defaultLength(col.getLength(), 50);
        col.setLength(len);
        List<OptionItem> options = meta.getOptions();
        String defaultValue = "A";
        if (options != null && !options.isEmpty() && options.get(0).getValue() != null) {
            defaultValue = String.valueOf(options.get(0).getValue());
        }
        col.setJavaDefaultValue(escape(defaultValue));
        col.setTsDefaultValue(escape(defaultValue));
        if (col.isRequired()) {
            col.getValidatorAnnotations().add("@NotBlank(message = \"" + col.getColumnName() + "不能为空\")");
            col.getValidatorImports().add("jakarta.validation.constraints.NotBlank");
        }
        addColumnAnnotation(col);
    }

    private void handleJson(CodeGenColumn col, MetaColumn meta) {
        col.setJavaType("String");
        col.setTsType("string");
        col.setComponentType("input");
        col.setInputType("textarea");
        col.setJavaDefaultValue(escape("{}"));
        col.setTsDefaultValue(escape("{}"));
        col.getJpaAnnotations().add("@Type(JsonbStringUserType.class)");
        col.getJpaAnnotations().add(buildJsonbColumnAnnotation(col));
        col.getJpaImports().add("com.lesofn.archforge.common.repository.converter.JsonbStringUserType");
        col.getJpaImports().add("org.hibernate.annotations.Type");
        if (col.isRequired()) {
            col.getValidatorAnnotations().add("@NotBlank(message = \"" + col.getColumnName() + "不能为空\")");
            col.getValidatorImports().add("jakarta.validation.constraints.NotBlank");
        }
    }

    private void handleFile(CodeGenColumn col, MetaColumn meta) {
        col.setJavaType("String");
        col.setTsType("string");
        col.setComponentType("input");
        col.setInputType("file");
        col.setLikeSearch(true);
        col.setKeywordSearchable(true);
        int len = defaultLength(col.getLength(), 500);
        col.setLength(len);
        col.setJavaDefaultValue(escape("https://example.com/file.txt"));
        col.setTsDefaultValue(escape(""));
        addStringValidators(col, len);
        addColumnAnnotation(col);
    }

    private void handleUuid(CodeGenColumn col, MetaColumn meta) {
        col.setJavaType("UUID");
        col.setTsType("string");
        col.setComponentType("input");
        col.setInputType("text");
        col.setJavaDefaultValue("UUID.randomUUID()");
        col.setTsDefaultValue(escape("00000000-0000-0000-0000-000000000000"));
        col.getImports().add("java.util.UUID");
        col.getJpaAnnotations().add("@JdbcTypeCode(SqlTypes.UUID)");
        addColumnAnnotation(col);
        col.getJpaImports().add("org.hibernate.annotations.JdbcTypeCode");
        col.getJpaImports().add("org.hibernate.type.SqlTypes");
        if (col.isRequired()) {
            col.getValidatorAnnotations().add("@NotNull(message = \"" + col.getColumnName() + "不能为空\")");
            col.getValidatorImports().add("jakarta.validation.constraints.NotNull");
        }
    }

    private void handleTimestampTz(CodeGenColumn col, MetaColumn meta) {
        col.setJavaType("OffsetDateTime");
        col.setTsType("string");
        col.setComponentType("date-picker");
        col.setInputType("datetime");
        col.setDateType("datetime");
        col.setDateValueFormat("YYYY-MM-DDTHH:mm:ssZ");
        col.setJavaDefaultValue("OffsetDateTime.now()");
        col.setTsDefaultValue(escape("2026-01-01T00:00:00+00:00"));
        col.getImports().add("java.time.OffsetDateTime");
        col.getJpaAnnotations().add("@JdbcTypeCode(SqlTypes.TIMESTAMP_WITH_TIMEZONE)");
        addColumnAnnotation(col);
        col.getJpaImports().add("org.hibernate.annotations.JdbcTypeCode");
        col.getJpaImports().add("org.hibernate.type.SqlTypes");
        if (col.isRequired()) {
            col.getValidatorAnnotations().add("@NotNull(message = \"" + col.getColumnName() + "不能为空\")");
            col.getValidatorImports().add("jakarta.validation.constraints.NotNull");
        }
    }

    private void handleArray(CodeGenColumn col, MetaColumn meta) {
        String elementType = meta.getArrayElementType() == null ? "STRING" : meta.getArrayElementType().toUpperCase();
        col.setArrayElementType(elementType);
        col.setTsType("string[]");
        col.setComponentType("select");
        col.setInputType("array");
        switch (elementType) {
            case "INTEGER" -> {
                col.setJavaType("Long[]");
                col.setJavaDefaultValue("new Long[]{1L, 2L}");
            }
            case "DECIMAL" -> {
                col.setJavaType("BigDecimal[]");
                col.setJavaDefaultValue("new BigDecimal[]{new BigDecimal(\"1.00\"), new BigDecimal(\"2.00\")}");
                col.getImports().add("java.math.BigDecimal");
            }
            case "BOOLEAN" -> {
                col.setJavaType("Boolean[]");
                col.setJavaDefaultValue("new Boolean[]{true, false}");
            }
            default -> {
                col.setJavaType("String[]");
                col.setJavaDefaultValue("new String[]{\"a\", \"b\"}");
            }
        }
        col.setTsDefaultValue("[]");
        col.getJpaAnnotations().add("@JdbcTypeCode(SqlTypes.ARRAY)");
        addColumnAnnotation(col);
        col.getJpaImports().add("org.hibernate.annotations.JdbcTypeCode");
        col.getJpaImports().add("org.hibernate.type.SqlTypes");
        if (col.isRequired()) {
            col.getValidatorAnnotations().add("@NotNull(message = \"" + col.getColumnName() + "不能为空\")");
            col.getValidatorImports().add("jakarta.validation.constraints.NotNull");
        }
    }

    private void handleGeo(CodeGenColumn col, MetaColumn meta) {
        col.setJavaType("String");
        col.setTsType("string");
        col.setComponentType("input");
        col.setInputType("textarea");
        col.setJavaDefaultValue(escape("{\"type\":\"Point\",\"coordinates\":[0,0]}"));
        col.setTsDefaultValue(escape("{\"type\":\"Point\",\"coordinates\":[0,0]}"));
        col.getJpaAnnotations().add("@Type(JsonbStringUserType.class)");
        col.getJpaAnnotations().add(buildJsonbColumnAnnotation(col));
        col.getJpaImports().add("com.lesofn.archforge.common.repository.converter.JsonbStringUserType");
        col.getJpaImports().add("org.hibernate.annotations.Type");
        if (col.isRequired()) {
            col.getValidatorAnnotations().add("@NotBlank(message = \"" + col.getColumnName() + "不能为空\")");
            col.getValidatorImports().add("jakarta.validation.constraints.NotBlank");
        }
    }

    private void addStringValidators(CodeGenColumn col, int len) {
        if (col.isRequired()) {
            col.getValidatorAnnotations().add("@NotBlank(message = \"" + col.getColumnName() + "不能为空\")");
            col.getValidatorAnnotations().add("@Size(max = " + len + ", message = \"" + col.getColumnName() + "长度不能超过" + len +
                    "\")");
            col.getValidatorImports().add("jakarta.validation.constraints.NotBlank");
            col.getValidatorImports().add("jakarta.validation.constraints.Size");
        }
    }

    private void addNotNullValidator(CodeGenColumn col) {
        if (col.isRequired()) {
            col.getValidatorAnnotations().add("@NotNull(message = \"" + col.getColumnName() + "不能为空\")");
            col.getValidatorImports().add("jakarta.validation.constraints.NotNull");
        }
    }

    private void addColumnAnnotation(CodeGenColumn col) {
        String anno = buildColumnAnnotation(col);
        if (anno != null) {
            col.getJpaAnnotations().add(anno);
        }
    }

    private String buildColumnAnnotation(CodeGenColumn col) {
        List<String> attrs = new ArrayList<>();
        if (col.isRequired()) {
            attrs.add("nullable = false");
        }
        if (col.isUnique()) {
            attrs.add("unique = true");
        }
        if (col.isString() || col.isText() || col.isFile() || col.isEnum()) {
            if (col.getLength() != null && col.getLength() > 0) {
                attrs.add("length = " + col.getLength());
            }
        }
        if (col.isDecimal()) {
            if (col.getPrecision() != null) {
                attrs.add("precision = " + col.getPrecision());
            }
            if (col.getScale() != null) {
                attrs.add("scale = " + col.getScale());
            }
        }
        if (attrs.isEmpty()) {
            return null;
        }
        return "@Column(" + String.join(", ", attrs) + ")";
    }

    private String buildJsonbColumnAnnotation(CodeGenColumn col) {
        if (col.isRequired()) {
            return "@Column(nullable = false, columnDefinition = \"jsonb\")";
        }
        return "@Column(columnDefinition = \"jsonb\")";
    }

    private String buildSearchCondition(CodeGenColumn col) {
        String getter = "request.get" + cap(col.getFieldName()) + "()";
        if (col.isArray()) {
            return getter + " != null && " + getter + ".length > 0";
        }
        if ("String".equals(col.getJavaType())) {
            return "StringUtils.hasText(" + getter + ")";
        }
        return getter + " != null";
    }

    private String buildSearchPredicate(CodeGenColumn col) {
        String field = col.getFieldName();
        if (col.isLikeSearch()) {
            return "cb.like(root.get(\"" + field + "\"), \"%\" + request.get" + cap(field) + "() + \"%\", '!')";
        }
        return "cb.equal(root.get(\"" + field + "\"), request.get" + cap(field) + "())";
    }

    private String buildKeywordPredicate(CodeGenColumn col) {
        if (!col.isKeywordSearchable()) {
            return null;
        }
        return "cb.like(root.get(\"" + col.getFieldName() + "\"), \"%\" + request.getKeyword() + \"%\", '!')";
    }

    private String cap(String name) {
        if (name == null || name.isEmpty()) {
            return name;
        }
        return Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }

    private int defaultLength(Integer value, int fallback) {
        return value != null && value > 0 ? value : fallback;
    }

    private int defaultInt(Integer value, int fallback) {
        return value != null ? value : fallback;
    }

    private String escape(String value) {
        return "\"" + value.replace("\"", "\\\"") + "\"";
    }
}
