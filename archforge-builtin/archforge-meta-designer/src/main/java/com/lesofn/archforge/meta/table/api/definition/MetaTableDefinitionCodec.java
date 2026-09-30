package com.lesofn.archforge.meta.table.api.definition;

import com.lesofn.archforge.meta.table.api.domain.MetaColumn;
import com.lesofn.archforge.meta.table.api.domain.MetaColumnType;
import com.lesofn.archforge.meta.table.api.domain.MetaTable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import org.jspecify.annotations.Nullable;
import tools.jackson.core.type.TypeReference;
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

    private static final TypeReference<Map<String, Object>> KEY_MAP = new TypeReference<>() {
    };

    /** Table-level keys holding children — compared per column, not as a table field. */
    private static final Set<String> TABLE_CHILD_KEYS = Set.of("columns", "removedColumns");

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

    public static ColumnDefinition toDefinition(MetaColumn column) {
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

    /** Definition → new entity (id/audit fields unset — assigned by the caller). */
    public static MetaTable toTableEntity(TableDefinition definition) {
        MetaTable table = new MetaTable();
        applyTo(definition, table);
        return table;
    }

    /** Definition → column entities (tableId unset — assigned by the caller). */
    public static List<MetaColumn> toColumnEntities(TableDefinition definition) {
        List<MetaColumn> columns = new ArrayList<>(definition.getColumns().size());
        for (ColumnDefinition def : definition.getColumns()) {
            MetaColumn column = new MetaColumn();
            applyTo(def, column);
            columns.add(column);
        }
        return columns;
    }

    /**
     * Assertion-style comparison: every table-level key the file carries must
     * equal the DB value; keys absent from the file are unmanaged (the DB keeps
     * its column default / previous value). Columns are compared separately.
     */
    public static boolean matches(TableDefinition file, TableDefinition db) {
        return assertedKeysMatch(file, db, TABLE_CHILD_KEYS);
    }

    /** Column counterpart of {@link #matches(TableDefinition, TableDefinition)}. */
    public static boolean matches(ColumnDefinition file, ColumnDefinition db) {
        return assertedKeysMatch(file, db, Set.of());
    }

    /**
     * Copy the keys the file carries onto an existing (possibly JPA-managed)
     * entity. Absent keys, id, version and audit fields stay untouched — so
     * re-applying an unchanged file never dirties the entity.
     */
    public static void applyTo(TableDefinition definition, MetaTable target) {
        target.setTableCode(definition.getTableCode());
        target.setTableName(definition.getTableName());
        setIfPresent(definition.getDescription(), target::setDescription);
        setIfPresent(definition.getTablePrefix(), target::setTablePrefix);
        setIfPresent(definition.getStatus(), target::setStatus);
        setIfPresent(definition.getSchemaVersion(), target::setSchemaVersion);
    }

    /** Column counterpart of {@link #applyTo(TableDefinition, MetaTable)}. */
    public static void applyTo(ColumnDefinition def, MetaColumn column) {
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
    }

    /** Null-omitting (NON_NULL) maps of both sides: the file's keys ⊆ the DB's keys with equal values. */
    private static boolean assertedKeysMatch(Object file, Object db, Set<String> ignoredKeys) {
        Map<String, Object> dbKeys = MAPPER.convertValue(db, KEY_MAP);
        return MAPPER.convertValue(file, KEY_MAP).entrySet().stream()
                .filter(e -> !ignoredKeys.contains(e.getKey()))
                .allMatch(e -> Objects.equals(e.getValue(), dbKeys.get(e.getKey())));
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
}
