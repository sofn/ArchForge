package com.lesofn.archforge.cli.secret;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.base.Splitter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SecretGeneratorTest {

    @TempDir
    Path tempDir;

    @Test
    void generatesRequiredSecrets() {
        Map<String, String> secrets = SecretGenerator.generate();
        // auth is sa-token (opaque tokens in Redis) — there is no JWT signing key to generate
        assertFalse(secrets.containsKey("JWT_SECRET"));
        assertTrue(secrets.containsKey("DB_PASSWORD"));
        assertTrue(secrets.containsKey("RSA_PUBLIC_KEY"));
        assertTrue(secrets.containsKey("RSA_PRIVATE_KEY"));
        assertTrue(secrets.containsKey("ARCH_FORGE_RSA_PRIVATE_KEY"));
        assertEquals(secrets.get("RSA_PRIVATE_KEY"), secrets.get("ARCH_FORGE_RSA_PRIVATE_KEY"));
        assertTrue(secrets.containsKey("AES_KEY"));
        assertTrue(secrets.get("DB_PASSWORD").length() >= 16);
    }

    @Test
    void writeSkipsExistingKeys() throws Exception {
        Path envFile = tempDir.resolve(".env");
        Files.writeString(envFile, "DB_PASSWORD=existing-secret\n");

        Map<String, String> written = SecretGenerator.writeIdempotent(envFile);
        String content = Files.readString(envFile);

        assertEquals("existing-secret", readEnv(content, "DB_PASSWORD"));
        assertTrue(content.contains("AES_KEY="));
        assertFalse(written.containsKey("DB_PASSWORD"));
        assertTrue(written.containsKey("AES_KEY"));
    }

    private static @Nullable String readEnv(String content, String key) {
        for (String line : Splitter.on('\n').split(content)) {
            if (line.startsWith(key + "=")) {
                return line.substring(key.length() + 1);
            }
        }
        return null;
    }
}
