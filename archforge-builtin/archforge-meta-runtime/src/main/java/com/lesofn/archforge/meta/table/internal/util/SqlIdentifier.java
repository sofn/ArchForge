package com.lesofn.archforge.meta.table.internal.util;

import com.lesofn.archforge.meta.table.api.errors.MetaTableErrorCode;
import com.lesofn.archforge.meta.table.api.errors.MetaTableException;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jspecify.annotations.Nullable;

/**
 * SQL 标识符安全处理工具。
 *
 * <p>
 * 所有用户输入的表名、列名、索引名都必须经过白名单校验与双引号转义后才能拼接到 SQL 中。
 */
public final class SqlIdentifier {

    private static final String IDENTIFIER_PATTERN = "^[a-z][a-z0-9_]{0,62}$";

    private static final Set<String> RESERVED_TABLE_PREFIXES = Set.of("sys_", "qrtz_", "pg_", "sql_", "information_schema_");

    /** 物理表名（前缀 + 编码）不得以这些开头：编码保留前缀 + Flyway 历史表。 */
    private static final Set<String> RESERVED_PHYSICAL_PREFIXES = Stream.concat(RESERVED_TABLE_PREFIXES.stream(), Stream.of(
            "flyway_")).collect(Collectors.toUnmodifiableSet());

    /** 设计器自建表的默认前缀（与 {@code MetaTable#physicalTableName} 一致）。 */
    private static final String DEFAULT_TABLE_PREFIX = "meta_";

    private static final Pattern TABLE_PREFIX_PATTERN = Pattern.compile("^[a-z][a-z0-9_]{0,31}$");

    private static final Set<String> RESERVED_COLUMN_CODES = Set.of(
            "id", "creator_id", "create_time", "updater_id", "update_time", "deleted");

    private static final Set<String> RESERVED_WORDS = Set.of(
            "all", "analyse", "analyze", "and", "any", "array", "as", "asc", "asymmetric", "both",
            "case", "cast", "check", "collate", "column", "constraint", "create", "current_catalog",
            "current_date", "current_role", "current_time", "current_timestamp", "current_user",
            "default", "deferrable", "desc", "distinct", "do", "else", "end", "except", "false",
            "fetch", "for", "foreign", "from", "grant", "group", "having", "in", "initially",
            "intersect", "into", "lateral", "leading", "limit", "localtime", "localtimestamp", "not",
            "null", "offset", "on", "only", "or", "order", "placing", "primary", "references",
            "returning", "select", "session_user", "some", "symmetric", "table", "then", "to",
            "trailing", "true", "union", "unique", "user", "using", "variadic", "when", "where",
            "window", "with");

    private SqlIdentifier() {
    }

    /** 校验并转义 SQL 标识符。 */
    public static String quote(String identifier) {
        validateCode(identifier);
        return '"' + identifier.replace("\"", "\"\"") + '"';
    }

    /** 校验业务表编码（不含前缀）。 */
    public static void validateTableCode(String code) {
        validateCode(code);
        for (String prefix : RESERVED_TABLE_PREFIXES) {
            if (code.startsWith(prefix)) {
                throw new MetaTableException(MetaTableErrorCode.META_TABLE_CODE_INVALID, "不能使用保留前缀: " + prefix);
            }
        }
    }

    /**
     * 校验物理表名 = 前缀 + 编码 整体。
     *
     * <p>
     * 只校验编码不够：前缀可以把无害的编码拼成平台表名（{@code sys_} + {@code menu}），而建表用
     * {@code CREATE TABLE IF NOT EXISTS}（会"收编"已存在的系统表）、删表用 {@code DROP TABLE ... CASCADE}。
     * 空前缀合法——它表示纳管的既有物理表，此时 {@code meta_} 也算保留（那是设计器自建表的命名空间）。
     */
    public static void validatePhysicalTableName(@Nullable String prefix, String code) {
        String effectivePrefix = prefix == null ? DEFAULT_TABLE_PREFIX : prefix;
        if (!effectivePrefix.isEmpty() && !TABLE_PREFIX_PATTERN.matcher(effectivePrefix).matches()) {
            throw new MetaTableException(MetaTableErrorCode.META_TABLE_PREFIX_INVALID, "只能是小写字母、数字、下划线，以字母开头，最长 32 位");
        }
        String physicalName = effectivePrefix + code;
        validateCode(physicalName);
        for (String reserved : RESERVED_PHYSICAL_PREFIXES) {
            if (physicalName.startsWith(reserved)) {
                throw new MetaTableException(MetaTableErrorCode.META_TABLE_PREFIX_INVALID, "不能使用保留前缀: " + reserved);
            }
        }
        if (effectivePrefix.isEmpty() && physicalName.startsWith(DEFAULT_TABLE_PREFIX)) {
            throw new MetaTableException(MetaTableErrorCode.META_TABLE_PREFIX_INVALID, "不能纳管 " + DEFAULT_TABLE_PREFIX +
                    " 前缀的表（设计器自建表的命名空间）");
        }
    }

    /** 校验字段编码。 */
    public static void validateColumnCode(String code) {
        validateCode(code);
        if (RESERVED_COLUMN_CODES.contains(code)) {
            throw new MetaTableException(MetaTableErrorCode.META_COLUMN_CODE_INVALID, "不能使用保留字段名: " + code);
        }
    }

    private static void validateCode(String code) {
        if (code == null || !code.matches(IDENTIFIER_PATTERN)) {
            throw new MetaTableException(MetaTableErrorCode.META_TABLE_CODE_INVALID, "标识符只能是小写字母、数字、下划线，且不能以数字开头");
        }
        if (RESERVED_WORDS.contains(code)) {
            throw new MetaTableException(MetaTableErrorCode.META_TABLE_CODE_INVALID, "不能使用 PostgreSQL 保留字: " + code);
        }
    }
}
