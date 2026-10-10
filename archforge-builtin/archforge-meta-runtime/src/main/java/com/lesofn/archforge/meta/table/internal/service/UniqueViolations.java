package com.lesofn.archforge.meta.table.internal.service;

import com.lesofn.archforge.meta.table.api.domain.MetaColumn;
import com.lesofn.archforge.meta.table.api.errors.MetaTableErrorCode;
import com.lesofn.archforge.meta.table.api.errors.MetaTableException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.jspecify.annotations.Nullable;
import org.postgresql.util.PSQLException;
import org.postgresql.util.ServerErrorMessage;
import org.springframework.dao.DuplicateKeyException;

/**
 * A unique-index violation is bad input, not a server failure: it used to surface as a 500 whose detail carried the
 * constraint, the key value and the SQL state. This turns it into {@code META_COLUMN_VALUE_INVALID} naming the column.
 */
final class UniqueViolations {

    private static final int PG_MAX_IDENTIFIER_LENGTH = 63;

    private UniqueViolations() {
    }

    static MetaTableException toValidationError(DuplicateKeyException e, String physicalTable, List<MetaColumn> columns) {
        List<String> names = conflictingColumns(constraintName(e), physicalTable, columns);
        String what = names.isEmpty() ? "唯一字段" : String.join("、", names);
        return new MetaTableException(MetaTableErrorCode.META_COLUMN_VALUE_INVALID, what + " 的值已存在");
    }

    private static @Nullable String constraintName(Throwable e) {
        for (Throwable t = e; t != null; t = t.getCause()) {
            if (t instanceof PSQLException psql) {
                ServerErrorMessage message = psql.getServerErrorMessage();
                return message == null ? null : message.getConstraint();
            }
        }
        return null;
    }

    /** Index names follow the DDL generator: {@code uq_<physical table>_<column code | index group>}, cut at 63. */
    private static List<String> conflictingColumns(@Nullable String constraint, String physicalTable,
            List<MetaColumn> columns) {
        if (constraint == null) {
            return List.of();
        }
        Map<String, List<String>> byIndexKey = new LinkedHashMap<>();
        for (MetaColumn column : columns) {
            String group = column.getIndexGroup();
            String key = group == null || group.isEmpty() ? column.getColumnCode() : group;
            byIndexKey.computeIfAbsent(key, k -> new ArrayList<>()).add(column.getColumnName());
        }
        for (Map.Entry<String, List<String>> entry : byIndexKey.entrySet()) {
            String name = "uq_" + physicalTable + "_" + entry.getKey();
            if (name.length() > PG_MAX_IDENTIFIER_LENGTH) {
                name = name.substring(0, PG_MAX_IDENTIFIER_LENGTH);
            }
            if (name.equals(constraint)) {
                return entry.getValue();
            }
        }
        return List.of();
    }
}
