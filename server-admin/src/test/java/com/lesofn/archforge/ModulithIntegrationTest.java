package com.lesofn.archforge;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

class ModulithIntegrationTest {

    @Test
    void modulesAreValid() {
        ApplicationModules.of(ModulithRoot.class).verify();
    }
}
