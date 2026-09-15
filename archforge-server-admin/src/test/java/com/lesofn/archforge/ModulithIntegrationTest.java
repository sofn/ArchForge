package com.lesofn.archforge;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

class ModulithIntegrationTest {

    @Test
    void modulesAreValid() {
        ApplicationModules modules = ApplicationModules.of(ModulithRoot.class);
        Set<String> ids = modules.stream()
                .map(module -> module.getIdentifier().toString())
                .collect(Collectors.toSet());
        assertTrue(
                ids.containsAll(Set.of("admin-user", "meta-table", "cms", "task")),
                "L3/L4 modules must be assembled into server-admin by default, actual: " + ids);
        modules.verify();
    }
}
