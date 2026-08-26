package com.lesofn.archforge.server.admin.metatable;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.lesofn.archforge.infrastructure.config.ArchForgeProperties;
import org.junit.jupiter.api.Test;

/** Guards the documented default of arch-forge.meta-table.max-page-size. */
class ArchForgePropertiesMetaTableTest {

    @Test
    void maxPageSizeDefaultsTo200() {
        assertEquals(200, new ArchForgeProperties().getMetaTable().getMaxPageSize());
    }
}
