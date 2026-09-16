package com.lesofn.archforge.meta.table.api.definition;

import com.lesofn.archforge.meta.table.api.domain.MetaColumn;
import com.lesofn.archforge.meta.table.api.domain.MetaColumnType;
import com.lesofn.archforge.meta.table.api.domain.MetaTable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import org.jspecify.annotations.Nullable;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.dataformat.yaml.YAMLMapper;

/**
 * Pure codec between {@code MetaTable}/{@code MetaColumn} entities and their
 * YAML definition form. Stateless and Spring-free — all persistence decisions
 * (diff, upsert, transactions) live in the caller.
 */
public final class MetaTableDefinitionCodec {

    private static final YAMLMapper MAPPER = YAMLMapper.builder()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true)
            .build();

    private MetaTableDefinitionCodec() {
    }

    public static TableDefinition toDefinition(MetaTable table, List<MetaColumn> columns) {
        TableDefinition def = new TableDefinition();
        def.setTableCode(table.getTableCode());
        def.setTableName(table.getTableName());
        def.setDescription(table.getDescription());
        def.setTablePrefix(table.getTablePrefix());
        def.setStatus(table.getStatus());
        def.setSchemaVersion(table.getSchemaVersion());
        List<ColumnDefinition> defs = new ArrayList<>(columns.size());
        for (MetaColumn column : columns) {
            defs.add(toDefinition(column));
        }
        def.setColumns(defs);
        return def;
    }

    private static ColumnDefinition toDefinition(MetaColumn column) {
        ColumnDefinition def = new ColumnDefinition();
        def.setColumnCode(column.getColumnCode());
        def.setColumnName(column.getColumnName());
        def.setDataType(column.getDataType().name());
        def.setLength(column.getLength());
        def.setPrecision(column.getPrecision());
        def.setScale(column.getScale());
        def.setNullable(column.getNullable());
        def.setDefaultValue(column.getDefaultValue());
        def.setUnique(column.getUnique());
        def.setRequired(column.getRequired());
        def.setSearchable(column.getSearchable());
        def.setListVisible(column.getListVisible());
        def.setIndex(column.getIndex());
        def.setSort(column.getSort());
        def.setOptions(column.getOptions());
        def.setReferenceTable(column.getReferenceTable());
        def.setReferenceColumn(column.getReferenceColumn());
        def.setDisplayExpression(column.getDisplayExpression());
        def.setTenantColumn(column.getTenantColumn());
        def.setOwnerColumn(column.getOwnerColumn());
        def.setIndexType(column.getIndexType());
        def.setIndexGroup(column.getIndexGroup());
        def.setArrayElementType(column.getArrayElementType());
        def.setSearchType(column.getSearchType());
        def.setDictCode(column.getDictCode());
        return def;
    }

    public static String toYaml(TableDefinition definition) {
        return MAPPER.writeValueAsString(definition);
    }

    /** Strict parse — unknown properties fail fast (the file is the contract). */
    public static TableDefinition fromYaml(String yaml) {
        return MAPPER.readValue(yaml, TableDefinition.class);
    }

    /** Definition → entity shape (id/audit fields untouched — set by the caller). */
    public static MetaTable toTableEntity(TableDefinition definition) {
        MetaTable table = new MetaTable();
        table.setTableCode(definition.getTableCode());
        table.setTableName(definition.getTableName());
        setIfPresent(definition.getDescription(), table::setDescription);
        setIfPresent(definition.getTablePrefix(), table::setTablePrefix);
        setIfPresent(definition.getStatus(), table::setStatus);
        setIfPresent(definition.getSchemaVersion(), table::setSchemaVersion);
        return table;
    }

    /** Definition → column entities (tableId unset — assigned by the caller). */
    public static List<MetaColumn> toColumnEntities(TableDefinition definition) {
        List<MetaColumn> columns = new ArrayList<>(definition.getColumns().size());
        for (ColumnDefinition def : definition.getColumns()) {
            columns.add(toColumnEntity(def));
        }
        return columns;
    }

    /**
     * Definition fields are all nullable (absent key = untouched); entity setters
     * are non-null — only apply what the file actually carries.
     */
    private static <T> void setIfPresent(@Nullable T value, Consumer<T> setter) {
        if (value != null) {
            setter.accept(value);
        }
    }

    private static MetaColumn toColumnEntity(ColumnDefinition def) {
        MetaColumn column = new MetaColumn();
        column.setColumnCode(def.getColumnCode());
        column.setColumnName(def.getColumnName());
        column.setDataType(MetaColumnType.valueOf(def.getDataType()));
        setIfPresent(def.getLength(), column::setLength);
        setIfPresent(def.getPrecision(), column::setPrecision);
        setIfPresent(def.getScale(), column::setScale);
        setIfPresent(def.getNullable(), column::setNullable);
        setIfPresent(def.getDefaultValue(), column::setDefaultValue);
        setIfPresent(def.getUnique(), column::setUnique);
        setIfPresent(def.getRequired(), column::setRequired);
        setIfPresent(def.getSearchable(), column::setSearchable);
        setIfPresent(def.getListVisible(), column::setListVisible);
        setIfPresent(def.getIndex(), column::setIndex);
        setIfPresent(def.getSort(), column::setSort);
        setIfPresent(def.getOptions(), column::setOptions);
        setIfPresent(def.getReferenceTable(), column::setReferenceTable);
        setIfPresent(def.getReferenceColumn(), column::setReferenceColumn);
        setIfPresent(def.getDisplayExpression(), column::setDisplayExpression);
        setIfPresent(def.getTenantColumn(), column::setTenantColumn);
        setIfPresent(def.getOwnerColumn(), column::setOwnerColumn);
        setIfPresent(def.getIndexType(), column::setIndexType);
        setIfPresent(def.getIndexGroup(), column::setIndexGroup);
        setIfPresent(def.getArrayElementType(), column::setArrayElementType);
        setIfPresent(def.getSearchType(), column::setSearchType);
        setIfPresent(def.getDictCode(), column::setDictCode);
        return column;
    }
}
