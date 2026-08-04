package com.lesofn.archforge.meta.table.internal.ddl;

import com.lesofn.archforge.meta.table.api.domain.MetaColumn;
import com.lesofn.archforge.meta.table.api.domain.MetaTable;
import com.lesofn.archforge.meta.table.internal.schema.SchemaChange;
import com.lesofn.archforge.meta.table.internal.schema.SchemaChangeType;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 根据 Schema 变更生成 ALTER TABLE DDL。
 */
@Component
@RequiredArgsConstructor
public class AlterTableDdlGenerator {

    private final ColumnTypeResolver columnTypeResolver;

    public List<SchemaDdl> generate(MetaTable table, List<SchemaChange> changes) {
        List<SchemaDdl> result = new ArrayList<>();
        for (SchemaChange change : changes) {
            result.addAll(generateForChange(table, change));
        }
        return result;
    }

    private List<SchemaDdl> generateForChange(MetaTable table, SchemaChange change) {
        return switch (change.getType()) {
            case ADD_COLUMN -> generateAddColumn(table, change);
            case DROP_COLUMN -> generateDropColumn(table, change);
            case RENAME_COLUMN -> generateRenameColumn(table, change);
            case ALTER_TYPE -> generateAlterType(table, change);
            case ALTER_DEFAULT -> generateAlterDefault(table, change);
            case ALTER_NULL -> generateAlterNull(table, change);
            case ALTER_INDEX -> generateAlterIndex(table, change);
        };
    }

    private List<SchemaDdl> generateAddColumn(MetaTable table, SchemaChange change) {
        List<String> statements = new ArrayList<>();
        MetaColumn column = change.getNewColumn();
        String physicalName = SqlIdentifier.quote(table.physicalTableName());
        StringBuilder sb = new StringBuilder();
        sb.append("ALTER TABLE ").append(physicalName)
                .append(" ADD COLUMN IF NOT EXISTS ")
                .append(SqlIdentifier.quote(column.getColumnCode()))
                .append(' ')
                .append(columnTypeResolver.resolve(column));
        if (Boolean.TRUE.equals(column.getRequired())) {
            sb.append(" NOT NULL");
        }
        if (column.getDefaultValue() != null && !column.getDefaultValue().isEmpty()) {
            sb.append(" DEFAULT ").append(columnTypeResolver.formatDefaultValue(column));
        }
        statements.add(sb.toString());

        String index = indexStatement(table, column, column.getColumnCode());
        if (index != null) {
            statements.add(index);
        }
        return List.of(new SchemaDdl(change, statements));
    }

    private List<SchemaDdl> generateDropColumn(MetaTable table, SchemaChange change) {
        List<String> statements = new ArrayList<>();
        MetaColumn column = change.getOldColumn();
        String oldCode = column.getColumnCode();
        String physicalName = SqlIdentifier.quote(table.physicalTableName());
        dropIndexStatement(statements, table, oldCode, true);
        dropIndexStatement(statements, table, oldCode, false);
        statements.add("ALTER TABLE " + physicalName + " DROP COLUMN IF EXISTS " + SqlIdentifier.quote(oldCode));
        return List.of(new SchemaDdl(change, statements));
    }

    private List<SchemaDdl> generateRenameColumn(MetaTable table, SchemaChange change) {
        String physicalName = SqlIdentifier.quote(table.physicalTableName());
        return List.of(new SchemaDdl(change, List.of("ALTER TABLE " + physicalName + " RENAME COLUMN " + SqlIdentifier.quote(
                change.getOldColumn().getColumnCode()) + " TO " + SqlIdentifier.quote(change.getNewColumn().getColumnCode()))));
    }

    private List<SchemaDdl> generateAlterType(MetaTable table, SchemaChange change) {
        String physicalName = SqlIdentifier.quote(table.physicalTableName());
        MetaColumn oldColumn = change.getOldColumn();
        MetaColumn newColumn = change.getNewColumn();
        String oldType = columnTypeResolver.resolve(oldColumn);
        String newType = columnTypeResolver.resolve(newColumn);
        String using = buildUsingExpression(newColumn.getColumnCode(), oldType, newType);
        return List.of(new SchemaDdl(change, List.of("ALTER TABLE " + physicalName + " ALTER COLUMN " + SqlIdentifier.quote(
                newColumn.getColumnCode()) + " TYPE " + newType + " USING " + using)));
    }

    private List<SchemaDdl> generateAlterDefault(MetaTable table, SchemaChange change) {
        String physicalName = SqlIdentifier.quote(table.physicalTableName());
        MetaColumn newColumn = change.getNewColumn();
        String newDefault = newColumn.getDefaultValue();
        if (newDefault == null || newDefault.isEmpty()) {
            return List.of(new SchemaDdl(change, List.of("ALTER TABLE " + physicalName + " ALTER COLUMN " + SqlIdentifier.quote(
                    newColumn.getColumnCode()) + " DROP DEFAULT")));
        }
        return List.of(new SchemaDdl(change, List.of("ALTER TABLE " + physicalName + " ALTER COLUMN " + SqlIdentifier.quote(
                newColumn.getColumnCode()) + " SET DEFAULT " + columnTypeResolver.formatDefaultValue(newColumn))));
    }

    private List<SchemaDdl> generateAlterNull(MetaTable table, SchemaChange change) {
        String physicalName = SqlIdentifier.quote(table.physicalTableName());
        MetaColumn newColumn = change.getNewColumn();
        String action = Boolean.TRUE.equals(newColumn.getRequired()) ? "SET NOT NULL" : "DROP NOT NULL";
        return List.of(new SchemaDdl(change, List.of("ALTER TABLE " + physicalName + " ALTER COLUMN " + SqlIdentifier.quote(
                newColumn.getColumnCode()) + " " + action)));
    }

    private List<SchemaDdl> generateAlterIndex(MetaTable table, SchemaChange change) {
        List<String> statements = new ArrayList<>();
        MetaColumn oldColumn = change.getOldColumn();
        MetaColumn newColumn = change.getNewColumn();
        String oldCode = oldColumn.getColumnCode();
        String newCode = newColumn.getColumnCode();

        String oldEffective = effectiveIndexType(oldColumn.getUnique(), oldColumn.getIndex());
        String newEffective = effectiveIndexType(newColumn.getUnique(), newColumn.getIndex());

        if (oldEffective != null && !oldEffective.equals(newEffective)) {
            dropIndexStatement(statements, table, oldCode, "UNIQUE".equals(oldEffective));
        }
        if (newEffective != null && !newEffective.equals(oldEffective)) {
            statements.add(buildCreateIndex(table, newCode, "UNIQUE".equals(newEffective)));
        }
        return List.of(new SchemaDdl(change, statements));
    }

    private String buildUsingExpression(String columnCode, String oldType, String newType) {
        String quoted = SqlIdentifier.quote(columnCode);
        if ("JSONB".equals(oldType)) {
            return quoted + "::text::" + newType;
        }
        return quoted + "::" + newType;
    }

    private String effectiveIndexType(Boolean unique, Boolean index) {
        if (Boolean.TRUE.equals(unique)) {
            return "UNIQUE";
        }
        if (Boolean.TRUE.equals(index)) {
            return "INDEX";
        }
        return null;
    }

    private String indexStatement(MetaTable table, MetaColumn column, String code) {
        String effective = effectiveIndexType(column.getUnique(), column.getIndex());
        if (effective == null) {
            return null;
        }
        return buildCreateIndex(table, code, "UNIQUE".equals(effective));
    }

    private void dropIndexStatement(List<String> out, MetaTable table, String code, boolean unique) {
        out.add("DROP INDEX IF EXISTS " + SqlIdentifier.quote(buildIndexName(table, code, unique)));
    }

    private String buildCreateIndex(MetaTable table, String code, boolean unique) {
        String physicalName = SqlIdentifier.quote(table.physicalTableName());
        return "CREATE " + (unique ? "UNIQUE " : "") + "INDEX IF NOT EXISTS " + SqlIdentifier.quote(buildIndexName(table, code,
                unique)) + " ON " + physicalName + " (" + SqlIdentifier.quote(code) + ")";
    }

    private String buildIndexName(MetaTable table, String code, boolean unique) {
        String raw = table.physicalTableName() + "_" + code;
        String name = (unique ? "uq_" : "idx_") + raw;
        return name.length() > 63 ? name.substring(0, 63) : name;
    }
}
