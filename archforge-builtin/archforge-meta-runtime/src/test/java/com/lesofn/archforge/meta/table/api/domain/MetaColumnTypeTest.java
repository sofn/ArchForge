package com.lesofn.archforge.meta.table.api.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.lesofn.archforge.meta.table.api.errors.MetaTableException;
import org.junit.jupiter.api.Test;

/** Invalid dataType strings must surface as business errors, not raw IllegalArgumentException (HTTP 500). */
class MetaColumnTypeTest {

    @Test
    void parsesValidType() {
        assertEquals(MetaColumnType.TEXT, MetaColumnType.of("TEXT"));
        assertEquals(MetaColumnType.REFERENCE, MetaColumnType.of("REFERENCE"));
    }

    @Test
    void invalidTypeThrowsBusinessException() {
        assertThrows(MetaTableException.class, () -> MetaColumnType.of("BOGUS"));
    }

    @Test
    void nullTypeThrowsBusinessException() {
        assertThrows(MetaTableException.class, () -> MetaColumnType.of(null));
    }
}
