package com.lesofn.archforge.cli.proc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;

class ProcessRunnerTest {

    private static final Path CWD = Path.of(".");

    @Test
    void runCaptureCapturesStdout() {
        ProcessRunner.RunResult result = new ProcessRunner().runCapture(List.of("java", "-version"), CWD);
        // java -version prints to stderr, so stdout stays empty but the call must not hang
        assertEquals(0, result.exitCode());
    }

    @Test
    void runCaptureEchoesStdout() {
        ProcessRunner.RunResult result = new ProcessRunner()
                .runCapture(List.of("sh", "-c", "printf hello && printf err >&2"), CWD);
        assertEquals(0, result.exitCode());
        assertEquals("hello", result.stdout());
    }

    @Test
    void runCaptureReportsFailure() {
        ProcessRunner.RunResult result = new ProcessRunner().runCapture(List.of("sh", "-c", "exit 7"), CWD);
        assertEquals(7, result.exitCode());
        assertTrue(result.stdout().isEmpty());
    }
}
