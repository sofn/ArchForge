package com.lesofn.archforge.cli.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.lesofn.archforge.cli.config.DbPasswordResolver;
import com.lesofn.archforge.cli.proc.RecordingProcessRunner;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import picocli.CommandLine;

/** `archforge init` must hand Gradle the credentials and must not report success when the migration failed. */
class InitCommandTest {

    @TempDir
    Path root;

    private InitCommand init(RecordingProcessRunner runner) {
        InitCommand command = new InitCommand(root, runner);
        new CommandLine(command).parseArgs(); // applies the --profile default (dev)
        return command;
    }

    @Test
    void migrationFailureIsReportedNotSwallowed() {
        RecordingProcessRunner runner = new RecordingProcessRunner(cmd -> String.join(" ", cmd).contains("flywayMigrate"), 1);

        assertEquals(1, init(runner).call(), "init used to print 'ok if plugin not wired' and exit 0");
    }

    @Test
    void migratesWithTheModuleAwareTaskAndTheGeneratedDbCredentials() {
        RecordingProcessRunner runner = RecordingProcessRunner.succeeding();

        assertEquals(0, init(runner).call());

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
