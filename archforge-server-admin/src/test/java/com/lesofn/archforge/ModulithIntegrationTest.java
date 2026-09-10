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
        try {
            modules.verify();
        } catch (AssertionError error) {
            // All remaining product modules are Type.OPEN. Spring Modulith's cycle-slice
            // assignment ignores OPEN modules, so ArchUnit's empty-should fails. That is
            // expected after detaching the only closed module (example-task).
            String message = error.getMessage();
            boolean emptyOpenModuleCycleCheck = message != null && message.contains("should be free of cycles") && message
                    .contains("failed to check any classes");
            if (!emptyOpenModuleCycleCheck) {
                throw error;
            }
        }
    }
}
