package com.lesofn.archforge;

import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

class ModulithIntegrationTest {

    @Test
    void modulesAreValid() {
        ApplicationModules modules = ApplicationModules.of(ModulithRoot.class);
        boolean exampleTaskPresent = modules.stream()
                .anyMatch(module -> "example-task".equals(module.getIdentifier().toString()));
        assertFalse(exampleTaskPresent, "example-task must not be assembled into server-admin");
        modules.verify();
    }
}
