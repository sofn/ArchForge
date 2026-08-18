package com.lesofn.archforge.cli.proc;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Thin process wrapper used by CLI commands. Does not start a Spring context.
 */
public class ProcessRunner {

    public int run(List<String> command, Path workingDir) {
        return run(command, workingDir, Map.of(), false);
    }

    public int run(List<String> command, Path workingDir, Map<String, String> extraEnv, boolean inheritIo) {
        return run(command, workingDir, extraEnv, inheritIo, null);
    }

    public int run(List<String> command, Path workingDir, Map<String, String> extraEnv, boolean inheritIo, Path stdoutFile) {
        try {
            ProcessBuilder builder = new ProcessBuilder(new ArrayList<>(command));
            if (workingDir != null) {
                builder.directory(workingDir.toFile());
            }
            builder.environment().putAll(extraEnv);
            if (inheritIo) {
                builder.inheritIO();
            } else if (stdoutFile != null) {
                stdoutFile.toFile().getParentFile().mkdirs();
                builder.redirectOutput(stdoutFile.toFile());
                builder.redirectError(ProcessBuilder.Redirect.INHERIT);
            } else {
                builder.redirectErrorStream(true);
            }
            Process process = builder.start();
            if (!inheritIo && stdoutFile == null) {
                String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
                if (!output.isBlank()) {
                    System.out.print(output);
                    if (!output.endsWith(System.lineSeparator())) {
                        System.out.println();
                    }
                }
            }
            return process.waitFor();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while running: " + command, e);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to run: " + command, e);
        }
    }

    public Process startDetached(List<String> command, Path workingDir, File logFile) {
        try {
            ProcessBuilder builder = new ProcessBuilder(new ArrayList<>(command));
            if (workingDir != null) {
                builder.directory(workingDir.toFile());
            }
            builder.redirectErrorStream(true);
            if (logFile != null) {
                logFile.getParentFile().mkdirs();
                builder.redirectOutput(ProcessBuilder.Redirect.appendTo(logFile));
            }
            return builder.start();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to start: " + command, e);
        }
    }
}
