package com.lesofn.archforge.infrastructure.config;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class ProductionSecretsTest {

    @Test
    void rejectsBlankRsaPrivateKey() {
        assertThrows(IllegalStateException.class, () -> ProductionSecrets.requireRsaPrivateKey(null));
        assertThrows(IllegalStateException.class, () -> ProductionSecrets.requireRsaPrivateKey(" "));
    }

    @Test
    void acceptsConfiguredRsaPrivateKey() {
        assertDoesNotThrow(() -> ProductionSecrets.requireRsaPrivateKey("MIICeAIBADANBgkqhkiG"));
    }
}
