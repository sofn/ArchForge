package com.lesofn.archforge.cli.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.lesofn.archforge.cli.config.DbPasswordResolver;
import com.lesofn.archforge.cli.proc.RecordingProcessRunner;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import picocli.CommandLine;

/**
 * `archforge init` must hand Gradle the credentials, must not report success when the migration failed — and without
 * {@code --write} it must not touch anything (it used to patch yaml, start containers, ALTER ROLE and migrate).
 */
class InitCommandTest {

    @TempDir
    Path root;

    private InitCommand init(RecordingProcessRunner runner, String... args) {
        InitCommand command = new InitCommand(root, runner);
        new CommandLine(command).parseArgs(args); // applies the --profile default (dev)
        return command;
    }

    @Test
    void dryRunChangesNothing() throws IOException {
        Path resources = Files.createDirectories(root.resolve("archforge-server-admin/src/main/resources"));
        Path devYaml = resources.resolve("application-dev.yaml");
        Files.writeString(devYaml, "spring:\n  jpa:\n    hibernate:\n      ddl-auto: update\n");
        RecordingProcessRunner runner = RecordingProcessRunner.succeeding();

        assertEquals(0, init(runner).call());

        assertEquals(List.of(), runner.calls, "no docker, no ALTER ROLE, no Flyway without --write");
        assertEquals("spring:\n  jpa:\n    hibernate:\n      ddl-auto: update\n", Files.readString(devYaml));
        assertFalse(Files.exists(root.resolve(".env")));
    }

    @Test
    void migrationFailureIsReportedNotSwallowed() {
        RecordingProcessRunner runner = new RecordingProcessRunner(cmd -> String.join(" ", cmd).contains("flywayMigrate"), 1);

        assertEquals(1, init(runner, "--write").call(), "init used to print 'ok if plugin not wired' and exit 0");
    }

    @Test
    void migratesWithTheModuleAwareTaskAndTheGeneratedDbCredentials() {
        RecordingProcessRunner runner = RecordingProcessRunner.succeeding();

        assertEquals(0, init(runner, "--write").call());

        RecordingProcessRunner.Call migrate = runner.calls.stream()
                .filter(call -> String.join(" ", call.command()).contains("flywayMigrate"))
                .findFirst()
                .orElseThrow();
        assertTrue(migrate.command().contains(":archforge-server-admin:flywayMigrateAll"), migrate.command().toString());
        assertFalse(migrate.command().contains(":archforge-server-admin:flywayMigrate"),
                "the flat flywayMigrate collides __root/V1 with cms/V1");
        String expected = DbPasswordResolver.resolve(root, null).value();
        assertEquals(expected, migrate.env().get("DB_PASSWORD"), "gradle falls back to the literal 'archforge' otherwise");
        assertNotNull(migrate.env().get("DB_USERNAME"));
    }
}
