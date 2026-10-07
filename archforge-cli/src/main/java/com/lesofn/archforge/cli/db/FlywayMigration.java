package com.lesofn.archforge.cli.db;

import com.lesofn.archforge.cli.config.DbPasswordResolver;
import com.lesofn.archforge.cli.proc.ProcessRunner;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * The single place where the CLI runs the dev-time Flyway migration through Gradle.
 *
 * <p>
 * Two things every caller used to get wrong: the aggregate task has to be module-aware (the flat
 * {@code flywayMigrate} collides {@code __root/V1} with {@code cms/V1}), and Gradle needs the database
 * credentials (it falls back to the literal password {@code archforge} otherwise, which fails against a
 * database whose password {@code init} just generated).
 */
public final class FlywayMigration {

    /** {@code __root} first, then every module with its own history table — what the app does at startup. */
    public static final String GRADLE_TASK = ":archforge-server-admin:flywayMigrateAll";

    private FlywayMigration() {
    }

    /** Runs the migration; returns the Gradle exit code (non-zero means the schema is NOT up to date). */
    public static int run(ProcessRunner runner, Path repoRoot) {
        DbPasswordResolver.Result password = DbPasswordResolver.resolve(repoRoot, null);
        Map<String, String> env = Map.of(
                "DB_PASSWORD", password.value(),
                "DB_USERNAME", DbPasswordResolver.resolveDbUsername(repoRoot));
        int code = runner.run(List.of("./gradlew", GRADLE_TASK, "-x", "test"), repoRoot, env, true);
        if (code != 0) {
            System.err.println("Flyway migration failed (" + GRADLE_TASK + " exit " + code +
                    "): the database schema is not up to date.");
        }
        return code;
    }
}
