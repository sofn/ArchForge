package com.lesofn.archforge.meta.table.internal.generator;

import com.lesofn.archforge.meta.table.api.domain.MetaColumn;
import com.lesofn.archforge.meta.table.api.domain.MetaColumnType;
import com.lesofn.archforge.meta.table.api.domain.MetaTable;
import com.lesofn.archforge.meta.table.api.domain.OptionItem;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

public final class CodeGenModelFactory {

    private static final Pattern NON_ALPHANUMERIC = Pattern.compile("[^A-Za-z0-9]+");

    private CodeGenModelFactory() {
    }

    public static Map<String, Object> buildModel(MetaTable table, List<MetaColumn> columns, CodeGenOptions options) {
        Map<String, Object> model = new LinkedHashMap<>();

        String tableCode = table.getTableCode();
        String tableName = table.getTableName();
        String javaPackage = toJavaPackage(tableCode);
        String className = toClassName(tableCode);
        String basePath = options.getBasePath();
        if (basePath == null || basePath.isBlank()) {
            basePath = "/generated/" + tableCode;
        }

        String packageBase = "com.lesofn.archforge.generated." + javaPackage;

        model.put("tableCode", tableCode);
        model.put("tableName", tableName == null ? "" : tableName);
        model.put("description", table.getDescription() == null ? "" : table.getDescription());
        model.put("javaPackage", javaPackage);
        model.put("className", className);
        model.put("entityName", className);
        model.put("packageBase", packageBase);
        model.put("basePath", basePath);
        model.put("moduleCode", generateModuleCode(tableCode));

        List<Map<String, Object>> columnModels = new ArrayList<>();
        boolean hasDecimal = false;
        boolean hasDate = false;
        boolean hasDateTime = false;
        boolean hasBoolean = false;
        boolean hasEnum = false;

        for (MetaColumn column : columns) {
            Map<String, Object> col = buildColumnModel(column);
            columnModels.add(col);
            if (Boolean.TRUE.equals(col.get("isDecimal"))) {
                hasDecimal = true;
            }
            if (Boolean.TRUE.equals(col.get("isDate"))) {
                hasDate = true;
            }
            if (Boolean.TRUE.equals(col.get("isDateTime"))) {
                hasDateTime = true;
            }
            if (Boolean.TRUE.equals(col.get("isBoolean"))) {
                hasBoolean = true;
            }
            if (Boolean.TRUE.equals(col.get("isEnum"))) {
                hasEnum = true;
            }
        }

        model.put("columns", columnModels);
        model.put("hasDecimal", hasDecimal);
        model.put("hasDate", hasDate);
        model.put("hasDateTime", hasDateTime);
        model.put("hasBoolean", hasBoolean);
        model.put("hasEnum", hasEnum);

        List<Map<String, Object>> searchableColumns = columnModels.stream()
                .filter(c -> Boolean.TRUE.equals(c.get("searchable")))
                .toList();
        List<Map<String, Object>> listVisibleColumns = columnModels.stream()
                .filter(c -> Boolean.TRUE.equals(c.get("listVisible")))
                .toList();
        List<Map<String, Object>> requiredColumns = columnModels.stream()
                .filter(c -> Boolean.TRUE.equals(c.get("required")))
                .toList();
        List<Map<String, Object>> keywordColumns = columnModels.stream()
                .filter(c -> Boolean.TRUE.equals(c.get("searchable")) && (Boolean.TRUE.equals(c.get("isString")) || Boolean.TRUE
                        .equals(c.get("isText")) || Boolean.TRUE.equals(c.get("isJson")) || Boolean.TRUE.equals(c.get(
                                "isFile")) || Boolean.TRUE.equals(c.get("isEnum"))))
                .toList();

        model.put("searchableColumns", searchableColumns);
        model.put("listVisibleColumns", listVisibleColumns);
        model.put("requiredColumns", requiredColumns);
        model.put("keywordColumns", keywordColumns);

        return model;
    }

    private static Map<String, Object> buildColumnModel(MetaColumn column) {
        Map<String, Object> col = new LinkedHashMap<>();

        String columnCode = column.getColumnCode();
        String fieldName = toCamelCase(columnCode);
        MetaColumnType type = column.getDataType();
        String javaType = javaType(type);
        String tsType = tsType(type);

        col.put("columnCode", columnCode);
        col.put("fieldName", fieldName);
        col.put("columnName", column.getColumnName() == null ? columnCode : column.getColumnName());
        col.put("dataType", type.name());
        col.put("javaType", javaType);
        col.put("tsType", tsType);
        col.put("componentType", componentType(type));
        col.put("inputType", inputType(type));
        col.put("dateType", dateType(type));
        col.put("dateValueFormat", dateValueFormat(type));
        col.put("isString", type == MetaColumnType.STRING);
        col.put("isText", type == MetaColumnType.TEXT);
        col.put("isJson", type == MetaColumnType.JSON);
        col.put("isFile", type == MetaColumnType.FILE);
        col.put("isInteger", type == MetaColumnType.INTEGER);
        col.put("isDecimal", type == MetaColumnType.DECIMAL);
        col.put("isBoolean", type == MetaColumnType.BOOLEAN);
        col.put("isDate", type == MetaColumnType.DATE);
        col.put("isDateTime", type == MetaColumnType.DATETIME);
        col.put("isEnum", type == MetaColumnType.ENUM);
        col.put("searchable", Boolean.TRUE.equals(column.getSearchable()));
        col.put("listVisible", Boolean.TRUE.equals(column.getListVisible()));
        col.put("required", Boolean.TRUE.equals(column.getRequired()));
        col.put("unique", Boolean.TRUE.equals(column.getUnique()));
        col.put("nullable", !Boolean.TRUE.equals(column.getRequired()));
        col.put("length", column.getLength() != null ? column.getLength() : 255);
        col.put("precision", column.getPrecision() != null ? column.getPrecision() : 10);
        col.put("scale", column.getScale() != null ? column.getScale() : 2);
        col.put("options", buildOptions(column.getOptions()));
        col.put("hasOptions", type == MetaColumnType.ENUM && column.getOptions() != null && !column.getOptions().isEmpty());
        col.put("defaultJavaValue", javaDefaultValue(type, column.getOptions()));

        return col;
    }

    private static List<Map<String, Object>> buildOptions(List<OptionItem> options) {
        List<Map<String, Object>> result = new ArrayList<>();
        if (options == null) {
            return result;
        }
        for (OptionItem option : options) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("label", option.getLabel() == null ? "" : option.getLabel());
            map.put("value", option.getValue() == null ? "" : String.valueOf(option.getValue()));
            result.add(map);
        }
        return result;
    }

    public static String toJavaPackage(String tableCode) {
        return tableCode.replaceAll("[^A-Za-z0-9]", "").toLowerCase(Locale.ROOT);
    }

    public static String toClassName(String tableCode) {
        String[] parts = NON_ALPHANUMERIC.split(tableCode);
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (part.isEmpty()) {
                continue;
            }
            sb.append(Character.toUpperCase(part.charAt(0)));
            if (part.length() > 1) {
                sb.append(part.substring(1).toLowerCase(Locale.ROOT));
            }
        }
        return sb.toString();
    }

    public static String toCamelCase(String columnCode) {
        String[] parts = NON_ALPHANUMERIC.split(columnCode);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            if (part.isEmpty()) {
                continue;
            }
            if (i == 0) {
                sb.append(part.substring(0, 1).toLowerCase(Locale.ROOT));
                if (part.length() > 1) {
                    sb.append(part.substring(1).toLowerCase(Locale.ROOT));
                }
            } else {
                sb.append(Character.toUpperCase(part.charAt(0)));
                if (part.length() > 1) {
                    sb.append(part.substring(1).toLowerCase(Locale.ROOT));
                }
            }
        }
        return sb.toString();
    }

    private static String javaType(MetaColumnType type) {
        return switch (type) {
            case STRING, TEXT, JSON, FILE, ENUM -> "String";
            case INTEGER -> "Long";
            case DECIMAL -> "BigDecimal";
            case BOOLEAN -> "Boolean";
            case DATE -> "LocalDate";
            case DATETIME -> "LocalDateTime";
        };
    }

    private static String tsType(MetaColumnType type) {
        return switch (type) {
            case STRING, TEXT, JSON, FILE, ENUM, DATE, DATETIME -> "string";
            case INTEGER, DECIMAL -> "number";
            case BOOLEAN -> "boolean";
        };
    }

    private static String componentType(MetaColumnType type) {
        return switch (type) {
            case STRING, TEXT, JSON, FILE -> "input";
            case INTEGER, DECIMAL -> "input-number";
            case BOOLEAN -> "switch";
            case DATE, DATETIME -> "date-picker";
            case ENUM -> "select";
        };
    }

    private static String inputType(MetaColumnType type) {
        return switch (type) {
            case TEXT, JSON -> "textarea";
            case FILE -> "file";
            default -> "text";
        };
    }

    private static String dateType(MetaColumnType type) {
        return switch (type) {
            case DATE -> "date";
            case DATETIME -> "datetime";
            default -> "";
        };
    }

    private static String dateValueFormat(MetaColumnType type) {
        return switch (type) {
            case DATE -> "YYYY-MM-DD";
            case DATETIME -> "YYYY-MM-DD HH:mm:ss";
            default -> "";
        };
    }

    private static int generateModuleCode(String tableCode) {
        int hash = tableCode.hashCode();
        return Math.floorMod(hash, 9000) + 100;
    }

    private static String javaDefaultValue(MetaColumnType type, List<OptionItem> options) {
        return switch (type) {
            case STRING, TEXT, JSON, FILE -> "\"测试\"";
            case INTEGER -> "1L";
            case DECIMAL -> "new BigDecimal(\"1.00\")";
            case BOOLEAN -> "true";
            case DATE -> "LocalDate.now()";
            case DATETIME -> "LocalDateTime.now()";
            case ENUM -> {
                if (options != null && !options.isEmpty() && options.get(0).getValue() != null) {
                    yield "\"" + options.get(0).getValue() + "\"";
                }
                yield "\"A\"";
            }
        };
    }
}
