package com.lesofn.archforge.cli.secret;

import com.lesofn.archforge.common.encrypt.RsaEncrypter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Generates and persists ArchForge secrets without printing values.
 */
public final class SecretGenerator {

    static final String[] KEYS = {
            "JWT_SECRET",
            "DB_PASSWORD",
            "DRUID_PASSWORD",
            "RSA_PUBLIC_KEY",
            "RSA_PRIVATE_KEY",
            "AES_KEY"
    };

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final Pattern ENV_LINE = Pattern.compile("^([A-Z0-9_]+)=(.*)$");

    private SecretGenerator() {
    }

    public static Map<String, String> generate() {
        Map<String, String> secrets = new LinkedHashMap<>();
        secrets.put("JWT_SECRET", randomBase64(48));
        secrets.put("DB_PASSWORD", randomPassword(20));
        secrets.put("DRUID_PASSWORD", randomPassword(20));
        try {
            Map<String, String> rsa = RsaEncrypter.generateKeyPair();
            secrets.put("RSA_PUBLIC_KEY", rsa.get("publicKey"));
            secrets.put("RSA_PRIVATE_KEY", rsa.get("privateKey"));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to generate RSA key pair", e);
        }
        secrets.put("AES_KEY", randomBase64(32));
        return secrets;
    }

    /**
     * Writes missing keys into {@code envFile}. Existing keys are left untouched.
     *
     * @return newly written key/value pairs
     */
    public static Map<String, String> writeIdempotent(Path envFile) {
        try {
            Map<String, String> existing = readEnv(envFile);
            Map<String, String> generated = generate();
            Map<String, String> written = new LinkedHashMap<>();
            List<String> lines = Files.exists(envFile)
                    ? new ArrayList<>(Files.readAllLines(envFile, StandardCharsets.UTF_8))
                    : new ArrayList<>();
            for (String key : KEYS) {
                if (existing.containsKey(key) && !existing.get(key).isBlank()) {
                    continue;
                }
                String value = generated.get(key);
                lines.add(key + "=" + value);
                written.put(key, value);
            }
            Files.write(envFile, lines, StandardCharsets.UTF_8);
            return written;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to write env file: " + envFile, e);
        }
    }

    public static Map<String, String> readEnv(Path envFile) throws IOException {
        Map<String, String> values = new LinkedHashMap<>();
        if (!Files.exists(envFile)) {
            return values;
        }
        for (String line : Files.readAllLines(envFile, StandardCharsets.UTF_8)) {
            Matcher matcher = ENV_LINE.matcher(line.trim());
            if (matcher.matches()) {
                values.put(matcher.group(1), matcher.group(2));
            }
        }
        return values;
    }

    static String randomBase64(int byteCount) {
        byte[] bytes = new byte[byteCount];
        RANDOM.nextBytes(bytes);
        return Base64.getEncoder().encodeToString(bytes);
    }

    static String randomPassword(int length) {
        final String alphabet = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789";
        StringBuilder builder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            builder.append(alphabet.charAt(RANDOM.nextInt(alphabet.length())));
        }
        return builder.toString();
    }
}
