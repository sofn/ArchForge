package com.lesofn.archforge.meta.table.internal.util;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.lesofn.archforge.meta.table.api.errors.MetaTableErrorCode;
import com.lesofn.archforge.meta.table.api.errors.MetaTableException;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * The physical table name is {@code prefix + code}: validating only the code lets a prefix such as
 * {@code sys_} turn a harmless code into a platform table (CREATE ... IF NOT EXISTS adopts it,
 * DROP ... CASCADE destroys it).
 */
class SqlIdentifierTest {

    @Test
    void platformPrefixesAreRejectedOnThePhysicalName() {
        for (String prefix : List.of("sys_", "qrtz_", "pg_", "sql_", "information_schema_", "flyway_")) {
            MetaTableException e = assertThrows(MetaTableException.class,
                    () -> SqlIdentifier.validatePhysicalTableName(prefix, "user"), prefix);
            assertEquals(MetaTableErrorCode.META_TABLE_PREFIX_INVALID.getCode(), e.getErrorInfo().getCode(), prefix);
        }
    }

    @Test
    void platformNamesBuiltFromAnEmptyPrefixAreRejectedToo() {
        assertThrows(MetaTableException.class, () -> SqlIdentifier.validatePhysicalTableName("", "sys_user"));
        assertThrows(MetaTableException.class, () -> SqlIdentifier.validatePhysicalTableName("", "meta_orders"));
        // a split such as "sy" + "s_user" must not slip through either
        assertThrows(MetaTableException.class, () -> SqlIdentifier.validatePhysicalTableName("sy", "s_user"));
    }

    @Test
    void malformedPrefixesAreRejected() {
        for (String prefix : List.of("Meta_", "meta-", "1abc_", "_x", "meta_;", "a".repeat(33), "meta_\"", " meta_")) {
            MetaTableException e = assertThrows(MetaTableException.class,
                    () -> SqlIdentifier.validatePhysicalTableName(prefix, "orders"), prefix);
            assertEquals(MetaTableErrorCode.META_TABLE_PREFIX_INVALID.getCode(), e.getErrorInfo().getCode(), prefix);
        }
    }

    @Test
    void ordinaryNamesPass() {
        assertDoesNotThrow(() -> SqlIdentifier.validatePhysicalTableName(null, "orders"));
        assertDoesNotThrow(() -> SqlIdentifier.validatePhysicalTableName("meta_", "orders"));
        assertDoesNotThrow(() -> SqlIdentifier.validatePhysicalTableName("crm_", "customer"));
        // adopted (imported) physical tables carry an empty prefix
        assertDoesNotThrow(() -> SqlIdentifier.validatePhysicalTableName("", "legacy_goods"));
    }

    @Test
    void physicalNameLongerThanPostgresLimitIsRejected() {
        assertThrows(MetaTableException.class, () -> SqlIdentifier.validatePhysicalTableName("meta_", "a".repeat(60)));
    }
}
