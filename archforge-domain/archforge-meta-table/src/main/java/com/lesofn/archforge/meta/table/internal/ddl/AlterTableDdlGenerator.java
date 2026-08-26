package com.lesofn.archforge.meta.table.internal.ddl;

import com.lesofn.archforge.meta.table.api.domain.MetaColumn;
import com.lesofn.archforge.meta.table.api.domain.MetaTable;
import com.lesofn.archforge.meta.table.internal.schema.SchemaChange;
import com.lesofn.archforge.meta.table.internal.schema.SchemaChangeType;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 根据 Schema 变更生成 ALTER TABLE DDL。
 */
@Component
@RequiredArgsConstructor
public class AlterTableDdlGenerator {

    private static final int PG_MAX_IDENTIFIER_LENGTH = 63;

    private final ColumnTypeResolver columnTypeResolver;

    public List<SchemaDdl> generate(MetaTable table, List<SchemaChange> changes) {
        List<SchemaDdl> result = new ArrayList<>();
        for (SchemaChange change : changes) {
            result.addAll(generateForChange(table, change));
        }
        return result;
    }

    /** 执行该变更前需要先确认的数据预检查询（违规行计数）；空表示无需预检。 */
    public Optional<String> buildViolationCountSql(MetaTable table, SchemaChange change) {
        return switch (change.getType()) {
            case ALTER_TYPE -> Optional.of(buildTypeLossCountSql(table, change));
            case ALTER_NULL -> Boolean.TRUE.equals(change.getNewRequired())
                    ? Optional.of(buildNullCountSql(table, change))
                    : Optional.<String> empty();
            default -> Optional.<String> empty();
        };
    }

    /** SET NOT NULL 需要先执行的默认值回填 UPDATE；仅当目标列配置了默认值时非空。 */
    public Optional<String> buildBackfillUpdateSql(MetaTable table, SchemaChange change) {
        if (change.getType() != SchemaChangeType.ALTER_NULL || !Boolean.TRUE.equals(change.getNewRequired())) {
            return Optional.empty();
        }
        MetaColumn column = change.getNewColumn();
        if (column.getDefaultValue() == null || column.getDefaultValue().isEmpty()) {
            return Optional.empty();
        }
        String physicalName = SqlIdentifier.quote(table.physicalTableName());
        String quoted = SqlIdentifier.quote(column.getColumnCode());
        return Optional.of("UPDATE " + physicalName + " SET " + quoted + " = " + columnTypeResolver.formatDefaultValue(column) +
                " WHERE " + quoted + " IS NULL");
    }

    private String buildNullCountSql(MetaTable table, SchemaChange change) {
        String physicalName = SqlIdentifier.quote(table.physicalTableName());
        String quoted = SqlIdentifier.quote(change.getNewColumn().getColumnCode());
        return "SELECT COUNT(*) FROM " + physicalName + " WHERE " + quoted + " IS NULL";
    }

    private String buildTypeLossCountSql(MetaTable table, SchemaChange change) {
        String physicalName = SqlIdentifier.quote(table.physicalTableName());
        String columnCode = change.getNewColumn().getColumnCode();
        String quoted = SqlIdentifier.quote(columnCode);
        String forward = buildUsingExpression(columnCode, change.getOldType(), change.getNewType());
        String roundTrip = needsTextRelay(change.getOldType())
                ? "(" + forward + ")::text::" + change.getOldType()
                : "CAST(" + forward + " AS " + change.getOldType() + ")";
        return "SELECT COUNT(*) FROM " + physicalName + " WHERE " + quoted + " IS NOT NULL AND " + roundTrip +
                " IS DISTINCT FROM " + quoted;
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

        return List.of(new SchemaDdl(change, statements));
    }

    private List<SchemaDdl> generateDropColumn(MetaTable table, SchemaChange change) {
        List<String> statements = new ArrayList<>();
        MetaColumn column = change.getOldColumn();
        String physicalName = SqlIdentifier.quote(table.physicalTableName());
        statements.add("ALTER TABLE " + physicalName + " DROP COLUMN IF EXISTS " + SqlIdentifier.quote(column.getColumnCode()) +
                " CASCADE");
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
        String oldType = change.getOldType();
        String newType = change.getNewType();
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
        String physicalTableCode = table.physicalTableName();

        List<MetaColumn> oldMembers = change.getOldGroupColumns();
        List<MetaColumn> newMembers = change.getNewGroupColumns();

        if (oldMembers != null && !oldMembers.isEmpty()) {
            statements.add(buildDropIndex(physicalTableCode, oldMembers, change.getOldIndexGroup(), change.getOldIndexType()));
        }
        if (newMembers != null && !newMembers.isEmpty()) {
            statements.add(buildCreateIndex(physicalTableCode, newMembers, change.getNewIndexGroup(),
                    change.getNewIndexType(), change.getNewUnique()));
        }

        return List.of(new SchemaDdl(change, statements));
    }

    private boolean isIndexEnabled(MetaColumn column) {
        return Boolean.TRUE.equals(column.getUnique()) || Boolean.TRUE.equals(column.getIndex());
    }

    private String buildDropIndex(String physicalTableCode, List<MetaColumn> members, String indexNameOrGroup,
            String indexType) {
        String indexName = buildIndexName(physicalTableCode, indexNameOrGroup, members);
        return "DROP INDEX IF EXISTS " + SqlIdentifier.quote(indexName);
    }

    private String buildCreateIndex(String physicalTableCode, List<MetaColumn> members, String indexNameOrGroup,
            String indexType, Boolean unique) {
        String physicalName = SqlIdentifier.quote(physicalTableCode);
        String indexName = buildIndexName(physicalTableCode, indexNameOrGroup, members);

        List<String> expressions = members.stream()
                .map(c -> indexExpression(c, indexType))
                .toList();
        String columnsPart = String.join(", ", expressions);

        StringBuilder sb = new StringBuilder();
        sb.append("CREATE ");
        if (Boolean.TRUE.equals(unique)) {
            sb.append("UNIQUE ");
        }
        sb.append("INDEX IF NOT EXISTS ").append(SqlIdentifier.quote(indexName))
                .append(" ON ").append(physicalName);
        if ("GIN".equalsIgnoreCase(indexType) || "GIST".equalsIgnoreCase(indexType) || "FULLTEXT".equalsIgnoreCase(indexType)) {
            String pgType = "FULLTEXT".equalsIgnoreCase(indexType) ? "GIN" : indexType.toUpperCase();
            sb.append(" USING ").append(pgType);
        }
        sb.append(" (").append(columnsPart).append(")");
        if (Boolean.TRUE.equals(unique)) {
            sb.append(" WHERE deleted = 0");
        }
        return sb.toString();
    }

    private String buildIndexName(String physicalTableCode, String indexNameOrGroup, List<MetaColumn> members) {
        boolean unique = members.stream().anyMatch(c -> Boolean.TRUE.equals(c.getUnique()));
        String raw = (unique ? "uq_" : "idx_") + physicalTableCode + "_" + indexNameOrGroup;
        return raw.length() > PG_MAX_IDENTIFIER_LENGTH ? raw.substring(0, PG_MAX_IDENTIFIER_LENGTH) : raw;
    }

    private String indexExpression(MetaColumn column, String indexType) {
        String quoted = SqlIdentifier.quote(column.getColumnCode());
        if ("FULLTEXT".equalsIgnoreCase(indexType)) {
            return "to_tsvector('chinese', " + quoted + ")";
        }
        return quoted;
    }

    private String buildUsingExpression(String columnCode, String oldType, String newType) {
        String quoted = SqlIdentifier.quote(columnCode);
        if (needsTextRelay(oldType)) {
            return quoted + "::text::" + newType;
        }
        return quoted + "::" + newType;
    }

    private static boolean needsTextRelay(String type) {
        return type != null && (type.toUpperCase().startsWith("JSONB") || type.toUpperCase().endsWith("[]"));
    }
}
