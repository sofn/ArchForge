package com.lesofn.archforge.cli.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Applies known security/config fixes to application yaml files.
 */
public final class YamlConfigPatcher {

    private YamlConfigPatcher() {
    }

    public static Map<String, Boolean> patchDevAndTest(Path repoRoot) {
        Map<String, Boolean> result = new LinkedHashMap<>();
        result.put(
                "application-dev.yaml",
                patchFile(repoRoot.resolve("archforge-server-admin/src/main/resources/application-dev.yaml"), true));
        result.put(
                "application-test.yaml",
                patchFile(repoRoot.resolve("archforge-server-admin/src/test/resources/application-test.yaml"), false));
        return result;
    }

    static boolean patchFile(Path file, boolean includeDevFixes) {
        if (!Files.exists(file)) {
            return false;
        }
        try {
            String original = Files.readString(file, StandardCharsets.UTF_8);
            String updated = original;
            updated = replaceJwtSecret(updated);
            updated = replaceEnvDefault(updated, "DB_PASSWORD", "archforge");
            updated = replaceDruidPassword(updated);
            if (includeDevFixes) {
                if (!updated.contains("open-in-view:")) {
                    updated = updated.replace("  jpa:\n", "  jpa:\n    open-in-view: false\n");
                }
                updated = updated.replace("ddl-auto: update", "ddl-auto: validate");
                if (!updated.contains("compression:")) {
                    updated = updated.replace("server:\n  port:", """
                            server:
                              compression:
                                enabled: true
                                mime-types: application/json,application/xml,text/html,text/xml,text/plain
                              port:""");
                }
            }
            if (!updated.equals(original)) {
                Files.writeString(file, updated, StandardCharsets.UTF_8);
                return true;
            }
            return false;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to patch " + file, e);
        }
    }

    static String replaceJwtSecret(String yaml) {
        String marker = "  jwt:\n    secret:";
        int jwt = yaml.indexOf(marker);
        if (jwt < 0) {
            jwt = yaml.indexOf("jwt:\n    secret:");
        }
        if (jwt < 0) {
            return yaml;
        }
        int secretLine = yaml.indexOf("secret:", jwt);
        int lineEnd = yaml.indexOf('\n', secretLine);
        if (lineEnd < 0) {
            lineEnd = yaml.length();
        }
        return yaml.substring(0, secretLine) + "secret: ${JWT_SECRET:}" + yaml.substring(lineEnd);
    }

    static String replaceDruidPassword(String yaml) {
        return yaml.replace("login-password: admin", "login-password: ${DRUID_PASSWORD:}");
    }

    static String replaceEnvDefault(String yaml, String envKey, String defaultValue) {
        return yaml.replace("${" + envKey + ":" + defaultValue + "}", "${" + envKey + ":}");
    }
}
