package com.lesofn.archforge.cli.secret;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SecretGeneratorTest {

    @TempDir
    Path tempDir;

    @Test
    void generatesRequiredSecrets() {
        Map<String, String> secrets = SecretGenerator.generate();
        assertTrue(secrets.containsKey("JWT_SECRET"));
        assertTrue(secrets.containsKey("DB_PASSWORD"));
        assertTrue(secrets.containsKey("DRUID_PASSWORD"));
        assertTrue(secrets.containsKey("RSA_PUBLIC_KEY"));
        assertTrue(secrets.containsKey("RSA_PRIVATE_KEY"));
        assertTrue(secrets.containsKey("ARCH_FORGE_RSA_PRIVATE_KEY"));
        assertEquals(secrets.get("RSA_PRIVATE_KEY"), secrets.get("ARCH_FORGE_RSA_PRIVATE_KEY"));
        assertTrue(secrets.containsKey("AES_KEY"));
        assertTrue(secrets.get("JWT_SECRET").length() >= 32);
        assertTrue(secrets.get("DB_PASSWORD").length() >= 16);
        assertNotEquals(secrets.get("DB_PASSWORD"), secrets.get("DRUID_PASSWORD"));
    }

    @Test
    void writeSkipsExistingKeys() throws Exception {
        Path envFile = tempDir.resolve(".env");
        Files.writeString(envFile, "JWT_SECRET=existing-secret\n");

        Map<String, String> written = SecretGenerator.writeIdempotent(envFile);
        String content = Files.readString(envFile);

        assertEquals("existing-secret", readEnv(content, "JWT_SECRET"));
        assertTrue(content.contains("DB_PASSWORD="));
        assertFalse(written.containsKey("JWT_SECRET"));
        assertTrue(written.containsKey("DB_PASSWORD"));
    }

    private static String readEnv(String content, String key) {
        for (String line : content.split("\n")) {
            if (line.startsWith(key + "=")) {
                return line.substring(key.length() + 1);
            }
        }
        return null;
    }
}
