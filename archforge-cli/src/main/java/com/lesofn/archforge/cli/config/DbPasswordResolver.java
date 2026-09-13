package com.lesofn.archforge.cli.config;

import com.lesofn.archforge.cli.secret.SecretGenerator;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import org.jspecify.annotations.Nullable;

/**
 * Resolves the dev PostgreSQL password for {@code infra up}: CLI flag →
 * {@code DB_PASSWORD} env → {@code .env} file → generated 16-char password
 * (persisted to {@code .env} so later runs reuse it).
 */
public final class DbPasswordResolver {

    private DbPasswordResolver() {
    }

    public record Result(String value, String source, boolean generated) {
    }

    public static Result resolve(Path repoRoot, @Nullable String cliPassword) {
        Path envFile = ProjectPaths.envFile(repoRoot);
        if (cliPassword != null && !cliPassword.isBlank()) {
            persistIfMissing(envFile, cliPassword, "--db-password");
            return new Result(cliPassword, "--db-password", false);
        }
        String env = System.getenv("DB_PASSWORD");
        if (env != null && !env.isBlank()) {
            return new Result(env, "env DB_PASSWORD", false);
        }
        String fromFile = readEnvValue(envFile);
        if (fromFile != null && !fromFile.isBlank()) {
            return new Result(fromFile, ".env", false);
        }
        String generated = SecretGenerator.generateDbPassword();
        persistIfMissing(envFile, generated, "generated");
        return new Result(generated, "generated", true);
    }

    private static @Nullable String readEnvValue(Path envFile) {
        try {
            Map<String, String> values = SecretGenerator.readEnv(envFile);
            return values.get("DB_PASSWORD");
        } catch (IOException e) {
            return null;
        }
    }

    private static void persistIfMissing(Path envFile, String value, String source) {
        try {
            String current = readEnvValue(envFile);
            if (current != null && !current.isBlank()) {
                if (!current.equals(value)) {
                    System.out.println(
                            "note: .env already defines a different DB_PASSWORD — kept; " + source +
                                    " value applies to this run only.");
                }
                return;
            }
            String existing = Files.exists(envFile)
                    ? Files.readString(envFile, StandardCharsets.UTF_8)
                    : "";
            if (!existing.isEmpty() && !existing.endsWith("\n")) {
                existing += System.lineSeparator();
            }
            Files.writeString(
                    envFile, existing + "DB_PASSWORD=" + value + System.lineSeparator(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.out.println("note: could not persist DB_PASSWORD to .env: " + e.getMessage());
        }
    }

    /** Prints the app-startup env vars, Windows and Linux syntax. */
    public static void printEnvHint(Result result) {
        System.out.println();
        System.out.println("DB credentials — source: " + result.source());
        System.out.println("Set before starting the app (java -jar / bootRun):");
        System.out.println("  Linux/macOS : export DB_PASSWORD=" + result.value());
        System.out.println("  Windows cmd : set DB_PASSWORD=" + result.value());
        System.out.println("  PowerShell  : $env:DB_PASSWORD=\"" + result.value() + "\"");
        if (result.generated()) {
            System.out.println();
            System.out.println("WARN: no DB password was specified — generated a 16-char one" +
                    " and wrote it to .env. To choose your own, set the DB_PASSWORD" +
                    " environment variable (or pass --db-password) before `infra up`.");
        }
    }
}
