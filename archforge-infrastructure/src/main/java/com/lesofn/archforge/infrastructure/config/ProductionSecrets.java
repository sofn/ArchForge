package com.lesofn.archforge.infrastructure.config;

public final class ProductionSecrets {

    private ProductionSecrets() {
    }

    public static String requireRsaPrivateKey(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Production RSA private key is missing. Set ARCH_FORGE_RSA_PRIVATE_KEY.");
        }
        return value;
    }
}
