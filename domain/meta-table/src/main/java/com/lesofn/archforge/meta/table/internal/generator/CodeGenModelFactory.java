package com.lesofn.archforge.meta.table.internal.generator;

import com.lesofn.archforge.meta.table.api.domain.MetaColumn;
import com.lesofn.archforge.meta.table.api.domain.MetaColumnType;
import com.lesofn.archforge.meta.table.api.domain.MetaTable;
import com.lesofn.archforge.meta.table.api.domain.OptionItem;
import com.lesofn.archforge.meta.table.api.service.DictionaryProvider;
import com.lesofn.archforge.meta.table.internal.generator.extension.CodeGenExtensionRegistry;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public final class CodeGenModelFactory {

    private static final Pattern NON_ALPHANUMERIC = Pattern.compile("[^A-Za-z0-9]+");

    private CodeGenModelFactory() {
    }

    public static Map<String, Object> buildModel(MetaTable table, List<MetaColumn> columns, CodeGenOptions options,
            CodeGenExtensionRegistry extensionRegistry, DictionaryProvider dictionaryProvider) {
        if (extensionRegistry == null) {
            extensionRegistry = new CodeGenExtensionRegistry();
        }

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
        model.put("tableId", table.getId());
        model.put("javaPackage", javaPackage);
        model.put("className", className);
        model.put("entityName", className);
        model.put("packageBase", packageBase);
        model.put("basePath", basePath);
        model.put("moduleCode", generateModuleCode(tableCode));
        model.put("physicalTableName", table.physicalTableName());

        extensionRegistry.beforeBuildModel(table, columns, options, model);

        CodeGenTypeRegistry typeRegistry = new CodeGenTypeRegistry();
        List<CodeGenColumn> codeGenColumns = new ArrayList<>();
        for (MetaColumn column : columns) {
            CodeGenColumn col = toCodeGenColumn(column, typeRegistry, dictionaryProvider);
            codeGenColumns.add(col);
        }

        List<Map<String, Object>> columnModels = new ArrayList<>();
        for (CodeGenColumn col : codeGenColumns) {
            columnModels.add(toMap(col));
        }
        model.put("columns", columnModels);

        extensionRegistry.afterBuildModel(table, columns, options, model);

        List<Map<String, Object>> searchableColumns = new ArrayList<>();
        List<Map<String, Object>> listVisibleColumns = new ArrayList<>();
        List<Map<String, Object>> requiredColumns = new ArrayList<>();
        List<Map<String, Object>> keywordColumns = new ArrayList<>();
        for (Map<String, Object> col : columnModels) {
            if (Boolean.TRUE.equals(col.get("searchable"))) {
                searchableColumns.add(col);
            }
            if (Boolean.TRUE.equals(col.get("listVisible"))) {
                listVisibleColumns.add(col);
            }
            if (Boolean.TRUE.equals(col.get("required"))) {
                requiredColumns.add(col);
            }
            if (Boolean.TRUE.equals(col.get("keywordSearchable"))) {
                keywordColumns.add(col);
            }
        }
        model.put("searchableColumns", searchableColumns);
        model.put("listVisibleColumns", listVisibleColumns);
        model.put("requiredColumns", requiredColumns);
        model.put("keywordColumns", keywordColumns);

        Set<String> imports = new LinkedHashSet<>();
        Set<String> jpaImports = new LinkedHashSet<>();
        Set<String> validatorImports = new LinkedHashSet<>();
        for (CodeGenColumn col : codeGenColumns) {
            imports.addAll(col.getImports());
            jpaImports.addAll(col.getJpaImports());
            validatorImports.addAll(col.getValidatorImports());
        }

        Set<String> entityImports = new LinkedHashSet<>(imports);
        entityImports.addAll(jpaImports);
        entityImports.removeIf(s -> s.startsWith("jakarta.persistence."));

        model.put("imports", new ArrayList<>(imports));
        model.put("entityImports", new ArrayList<>(entityImports));
        model.put("validatorImports", new ArrayList<>(validatorImports));

        return model;
    }

    private static CodeGenColumn toCodeGenColumn(MetaColumn column, CodeGenTypeRegistry typeRegistry,
            DictionaryProvider dictionaryProvider) {
        CodeGenColumn col = new CodeGenColumn();
        col.setColumnCode(column.getColumnCode());
        col.setFieldName(toCamelCase(column.getColumnCode()));
        col.setColumnName(column.getColumnName() == null ? column.getColumnCode() : column.getColumnName());
        col.setDataType(column.getDataType());
        col.setSearchable(Boolean.TRUE.equals(column.getSearchable()));
        col.setListVisible(Boolean.TRUE.equals(column.getListVisible()));
        col.setRequired(Boolean.TRUE.equals(column.getRequired()));
        col.setUnique(Boolean.TRUE.equals(column.getUnique()));
        col.setNullable(!Boolean.TRUE.equals(column.getRequired()));
        col.setLength(column.getLength());
        col.setPrecision(column.getPrecision());
        col.setScale(column.getScale());
        col.setDictCode(column.getDictCode());
        col.setHasDict(column.getDataType() == MetaColumnType.ENUM && column.getDictCode() != null && !column.getDictCode()
                .isEmpty());
        col.setOptions(buildOptions(column.getOptions()));
        col.setHasOptions(column.getDataType() == MetaColumnType.ENUM && !col.isHasDict() && column.getOptions() != null &&
                !column
                        .getOptions().isEmpty());
        col.setArrayElementType(column.getArrayElementType());

        typeRegistry.resolve(column.getDataType()).enrich(col, column);

        if (col.isHasDict() && dictionaryProvider != null) {
            List<OptionItem> dictItems = dictionaryProvider.findItems(column.getDictCode());
            col.setOptions(buildOptions(dictItems));
            col.setHasOptions(!dictItems.isEmpty());
        }

        return col;
    }

    private static Map<String, Object> toMap(CodeGenColumn col) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("columnCode", col.getColumnCode());
        map.put("fieldName", col.getFieldName());
        map.put("columnName", col.getColumnName());
        map.put("dataType", col.getDataType().name());
        map.put("javaType", col.getJavaType());
        map.put("tsType", col.getTsType());
        map.put("componentType", col.getComponentType());
        map.put("inputType", col.getInputType());
        map.put("dateType", col.getDateType());
        map.put("dateValueFormat", col.getDateValueFormat());
        map.put("javaDefaultValue", col.getJavaDefaultValue());
        map.put("tsDefaultValue", col.getTsDefaultValue());
        map.put("searchCondition", col.getSearchCondition());
        map.put("searchPredicate", col.getSearchPredicate());
        map.put("keywordPredicate", col.getKeywordPredicate());
        map.put("jpaAnnotations", col.getJpaAnnotations());
        map.put("validatorAnnotations", col.getValidatorAnnotations());
        map.put("options", col.getOptions());
        map.put("hasOptions", col.isHasOptions());
        map.put("dictCode", col.getDictCode());
        map.put("hasDict", col.isHasDict());
        map.put("searchable", col.isSearchable());
        map.put("listVisible", col.isListVisible());
        map.put("required", col.isRequired());
        map.put("unique", col.isUnique());
        map.put("nullable", col.isNullable());
        map.put("length", col.getLength());
        map.put("precision", col.getPrecision());
        map.put("scale", col.getScale());
        map.put("arrayElementType", col.getArrayElementType());
        map.put("searchType", col.getSearchType());
        map.put("rangeSearch", col.isRangeSearch());
        map.put("fileSizeLimit", col.getFileSizeLimit());
        map.put("isString", col.isString());
        map.put("isText", col.isText());
        map.put("isJson", col.isJson());
        map.put("isFile", col.isFile());
        map.put("isImage", col.isImage());
        map.put("isMultiImage", col.isMultiImage());
        map.put("isEnum", col.isEnum());
        map.put("isInteger", col.isInteger());
        map.put("isDecimal", col.isDecimal());
        map.put("isBoolean", col.isBoolean());
        map.put("isDate", col.isDate());
        map.put("isDateTime", col.isDateTime());
        map.put("isUuid", col.isUuid());
        map.put("isTimestampTz", col.isTimestampTz());
        map.put("isArray", col.isArray());
        map.put("isGeo", col.isGeo());
        map.put("likeSearch", col.isLikeSearch());
        map.put("keywordSearchable", col.isKeywordSearchable());
        return map;
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

    private static int generateModuleCode(String tableCode) {
        int hash = tableCode.hashCode();
        return Math.floorMod(hash, 9000) + 100;
    }
}
