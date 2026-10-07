package com.lesofn.archforge.meta.table.internal.service;

import org.springframework.jdbc.core.JdbcOperations;

/** Existence probe for physical tables, shared by the designer (create) and the definition sync (missing-table report). */
final class PhysicalTables {

    private PhysicalTables() {
    }

    /**
     * Whether a relation with exactly this name is visible on the search path. The name is double-quoted so it is
     * resolved verbatim — never case-folded, never split on a dot into schema + table.
     */
    static boolean exists(JdbcOperations jdbc, String physicalName) {
        String quoted = "\"" + physicalName.replace("\"", "\"\"") + "\"";
        return Boolean.TRUE.equals(jdbc.queryForObject("SELECT to_regclass(?) IS NOT NULL", Boolean.class, quoted));
    }
}
