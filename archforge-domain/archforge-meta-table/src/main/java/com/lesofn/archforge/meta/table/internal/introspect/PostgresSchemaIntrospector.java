package com.lesofn.archforge.meta.table.internal.introspect;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * PostgreSQL 物理表结构内省器。
 *
 * <p>
 * 通过 information_schema 与 pg_catalog 读取当前 schema 下的表、列、主键、索引与注释，
 * 供「导入已有表」做兼容判定与列映射。所有查询走 meta-table 自身的 JdbcTemplate，
 * 与物理表同数据源。
 */
@Component
public class PostgresSchemaIntrospector {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public PostgresSchemaIntrospector(
            @Qualifier("metaTableJdbcTemplate") NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /** 表概览。 */
    public record TableInfo(String name, @Nullable String comment, long estimatedRows) {
    }

    /** 列定义（information_schema.columns + 注释）。 */
    public record ColumnInfo(
            String name,
            String udtName,
            boolean nullable,
            boolean identity,
            @Nullable String columnDefault,
            @Nullable Integer charLen,
            @Nullable Integer precision,
            @Nullable Integer scale,
            int position,
            @Nullable String comment) {
    }

    /** 单列索引信息（复合索引不入此列）。 */
    public record IndexInfo(boolean unique, String columnName) {
    }

    private static final String LIST_TABLES_SQL = """
            SELECT t.table_name AS name,
                   obj_description(('"' || t.table_schema || '"."' || t.table_name || '"')::regclass, 'pg_class') AS comment,
                   GREATEST(c.reltuples, 0)::bigint AS estimated_rows
            FROM information_schema.tables t
            JOIN pg_class c ON c.relname = t.table_name
            JOIN pg_namespace n ON n.oid = c.relnamespace AND n.nspname = t.table_schema
            WHERE t.table_schema = current_schema() AND t.table_type = 'BASE TABLE'
            ORDER BY t.table_name
            """;

    private static final String LIST_COLUMNS_SQL = """
            SELECT col.column_name AS name,
                   col.udt_name,
                   col.is_nullable = 'YES' AS nullable,
                   col.is_identity = 'YES' AS identity,
                   col.column_default,
                   col.character_maximum_length AS char_len,
                   col.numeric_precision AS precision,
                   col.numeric_scale AS scale,
                   col.ordinal_position AS position,
                   col_description(('"' || col.table_schema || '"."' || col.table_name || '"')::regclass,
                                   col.ordinal_position) AS comment
            FROM information_schema.columns col
            WHERE col.table_schema = current_schema() AND col.table_name = :tableName
            ORDER BY col.ordinal_position
            """;

    private static final String PRIMARY_KEYS_SQL = """
            SELECT kcu.column_name AS name
            FROM information_schema.table_constraints tc
            JOIN information_schema.key_column_usage kcu
              ON tc.constraint_name = kcu.constraint_name
             AND tc.table_schema = kcu.table_schema
             AND tc.table_name = kcu.table_name
            WHERE tc.constraint_type = 'PRIMARY KEY'
              AND tc.table_schema = current_schema() AND tc.table_name = :tableName
            ORDER BY kcu.ordinal_position
            """;

    private static final String INDEXES_SQL = """
            SELECT ix.indisunique AS is_unique,
                   a.attname AS column_name,
                   ix.indnatts AS att_count,
                   i.relname AS index_name
            FROM pg_index ix
            JOIN pg_class t ON t.oid = ix.indrelid
            JOIN pg_class i ON i.oid = ix.indexrelid
            JOIN pg_namespace n ON n.oid = t.relnamespace
            JOIN pg_attribute a ON a.attrelid = t.oid AND a.attnum = ANY (ix.indkey)
            WHERE n.nspname = current_schema() AND t.relname = :tableName
              AND NOT ix.indisprimary
            """;

    private static final String TABLE_EXISTS_SQL = "SELECT COUNT(*) FROM information_schema.tables" +
            " WHERE table_schema = current_schema() AND table_type = 'BASE TABLE' AND table_name = :tableName";

    /** 当前 schema 下的全部物理表。 */
    public List<TableInfo> listTables() {
        return jdbcTemplate.query(LIST_TABLES_SQL, Map.of(), (rs, rowNum) -> new TableInfo(rs.getString("name"), rs.getString(
                "comment"), rs.getLong("estimated_rows")));
    }

    public boolean tableExists(String tableName) {
        Integer count = jdbcTemplate.queryForObject(TABLE_EXISTS_SQL, Map.of("tableName", tableName), Integer.class);
        return count != null && count > 0;
    }

    /** 指定表的全部列（按 ordinal_position 排序）。 */
    public List<ColumnInfo> listColumns(String tableName) {
        return jdbcTemplate.query(LIST_COLUMNS_SQL, Map.of("tableName", tableName), (rs, rowNum) -> new ColumnInfo(rs.getString(
                "name"), rs.getString("udt_name"), rs.getBoolean("nullable"), rs.getBoolean("identity"), rs.getString(
                        "column_default"), (Integer) rs.getObject("char_len"), (Integer) rs.getObject("precision"), (Integer) rs
                                .getObject("scale"), rs.getInt("position"), rs.getString("comment")));
    }

    /** 主键列名（有序，单列主键返回单元素）。 */
    public List<String> primaryKeyColumns(String tableName) {
        return jdbcTemplate.query(
                PRIMARY_KEYS_SQL, Map.of("tableName", tableName), (rs, rowNum) -> rs.getString("name"));
    }

    /** 单列索引 → 列名；复合索引名列表另存。 */
    public IndexScan scanIndexes(String tableName) {
        List<IndexInfo> singleColumn = new ArrayList<>();
        List<String> compositeNames = new ArrayList<>();
        // indkey 按 ANY 展开后 indnatts>1 的索引其每列都会出现 —— 按索引名归并一次即可
        Map<String, Integer> indexAttCount = new LinkedHashMap<>();
        jdbcTemplate.query(INDEXES_SQL, Map.of("tableName", tableName), rs -> {
            String indexName = rs.getString("index_name");
            int attCount = rs.getInt("att_count");
            indexAttCount.put(indexName, attCount);
            if (attCount == 1) {
                singleColumn.add(new IndexInfo(rs.getBoolean("is_unique"), rs.getString("column_name")));
            }
        });
        indexAttCount.forEach((name, count) -> {
            if (count > 1) {
                compositeNames.add(name);
            }
        });
        return new IndexScan(singleColumn, compositeNames);
    }

    /** 索引扫描结果。 */
    public record IndexScan(List<IndexInfo> singleColumnIndexes, List<String> compositeIndexNames) {
    }
}
