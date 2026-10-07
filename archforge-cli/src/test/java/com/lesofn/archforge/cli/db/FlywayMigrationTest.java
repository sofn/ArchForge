package com.lesofn.archforge.cli.db;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.lesofn.archforge.cli.config.DbPasswordResolver;
import com.lesofn.archforge.cli.proc.RecordingProcessRunner;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FlywayMigrationTest {

    @TempDir
    Path root;

    @Test
    void runsTheAggregateTaskWithResolvedCredentials() throws Exception {
        Files.writeString(root.resolve(".env"), "DB_PASSWORD=from-dot-env\nDB_USERNAME=app_user\n");
        RecordingProcessRunner runner = RecordingProcessRunner.succeeding();

        int code = FlywayMigration.run(runner, root);

        assertEquals(0, code);
        RecordingProcessRunner.Call call = runner.calls.get(0);
        assertTrue(call.command().contains(FlywayMigration.GRADLE_TASK), call.command().toString());
        // the process environment wins over .env in the resolver — assert against what it resolves, not a literal
        assertEquals(DbPasswordResolver.resolve(root, null).value(), call.env().get("DB_PASSWORD"));
        assertEquals(DbPasswordResolver.resolveDbUsername(root), call.env().get("DB_USERNAME"));
    }

    @Test
    void gradleFailureIsPropagated() {
        RecordingProcessRunner runner = new RecordingProcessRunner(cmd -> true, 7);

        assertEquals(7, FlywayMigration.run(runner, root));
    }
}
