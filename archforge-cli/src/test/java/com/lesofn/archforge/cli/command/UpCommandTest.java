package com.lesofn.archforge.cli.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.lesofn.archforge.cli.proc.RecordingProcessRunner;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import picocli.CommandLine;

class UpCommandTest {

    @TempDir
    Path root;

    private UpCommand up(RecordingProcessRunner runner) {
        UpCommand command = new UpCommand(root, runner);
        new CommandLine(command).parseArgs(); // dev profile default
        return command;
    }

    @Test
    void failedMigrationStopsBeforeTheAppContainersStart() {
        RecordingProcessRunner runner = new RecordingProcessRunner(cmd -> String.join(" ", cmd).contains("flywayMigrate"), 1);

        assertEquals(1, up(runner).call());

        assertFalse(runner.ran("docker-compose.yml"), "app containers must not start on a stale schema");
    }

    @Test
    void successfulMigrationIsFollowedByTheStack() {
        RecordingProcessRunner runner = RecordingProcessRunner.succeeding();

        assertEquals(0, up(runner).call());

        assertTrue(runner.ran("flywayMigrateAll"));
        assertTrue(runner.ran("docker-compose.yml"));
    }
}
