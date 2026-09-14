package com.lesofn.archforge.cli.command;

import com.lesofn.archforge.cli.config.ProjectPaths;
import com.lesofn.archforge.cli.proc.ProcessRunner;
import java.io.IOException;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;
import picocli.CommandLine.Command;

@Command(
        mixinStandardHelpOptions = true,
        name = "doctor",
        description = "Diagnose the local environment: JDK 25, docker/compose, pnpm, ports, .env")
public class DoctorCommand implements Callable<Integer> {

    private int failures;

    @Override
    public Integer call() {
        Path root = ProjectPaths.repoRoot();
        ProcessRunner runner = new ProcessRunner();

        checkJava(runner);
        checkTool(runner, List.of("docker", "version", "--format", "{{.Server.Version}}"), "docker daemon");
        checkTool(runner, List.of("docker", "compose", "version", "--short"), "docker compose plugin");
        checkToolVersion(runner, List.of("pnpm", "--version"), "pnpm", 9);
        checkToolVersion(runner, List.of("node", "--version"), "node", 22);
        checkFile(root.resolve(".env"), ".env (run `archforge init --write` to create)");
        checkFile(ProjectPaths.adminRepo(root), "ArchForgeAdmin repo (sibling checkout, admin-ui)");
        checkFile(ProjectPaths.webRepo(root), "ArchForgeWeb repo (sibling checkout, next.js)");
        checkPort(8080, "server-admin");
        checkPort(8081, "server-web");
        checkPort(8848, "admin-ui dev");
        checkPort(3000, "web-ui next.js dev");

        System.out.println();
        if (failures == 0) {
            System.out.println("doctor: all checks passed.");
            return 0;
        }
        System.out.println("doctor: " + failures + " problem(s) found — fix the ✗ items above.");
        return 1;
    }

    private void checkJava(ProcessRunner runner) {
        ProcessRunner.RunResult version = runner.runCapture(List.of("java", "-version"), Path.of("."));
        // `java -version` prints to stderr; runCapture merges nothing, so parse via the process directly.
        int major = javaMajor();
        if (major >= 25) {
            ok("java " + major + " (need >= 25)");
        } else {
            fail("java " + major + " — need JDK 25+ on PATH (or fix JAVA_HOME)");
        }
        if (version.exitCode() != 0) {
            fail("java not runnable: exit " + version.exitCode());
        }
    }

    private static int javaMajor() {
        String v = System.getProperty("java.version", "0");
        StringBuilder digits = new StringBuilder();
        for (int i = 0; i < v.length(); i++) {
            char c = v.charAt(i);
            if (!Character.isDigit(c)) {
                break;
            }
            digits.append(c);
        }
        try {
            return Integer.parseInt(digits.toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void checkTool(ProcessRunner runner, List<String> command, String label) {
        try {
            ProcessRunner.RunResult result = runner.runCapture(command, Path.of("."));
            if (result.exitCode() == 0) {
                ok(label + " " + result.stdout().trim());
            } else {
                fail(label + " not runnable (exit " + result.exitCode() + ")");
            }
        } catch (IllegalStateException e) {
            fail(label + " not found on PATH");
        }
    }

    /** Like {@link #checkTool} plus a minimum major-version check (node>=22, pnpm>=9). */
    private void checkToolVersion(ProcessRunner runner, List<String> command, String label, int minMajor) {
        try {
            ProcessRunner.RunResult result = runner.runCapture(command, Path.of("."));
            if (result.exitCode() != 0) {
                fail(label + " not runnable (exit " + result.exitCode() + ")");
                return;
            }
            int major = parseLeadingInt(result.stdout().trim());
            if (major >= minMajor) {
                ok(label + " " + result.stdout().trim());
            } else {
                fail(label + " " + result.stdout().trim() + " — need >= " + minMajor);
            }
        } catch (IllegalStateException e) {
            fail(label + " not found on PATH");
        }
    }

    private static int parseLeadingInt(String version) {
        StringBuilder digits = new StringBuilder();
        for (int i = 0; i < version.length(); i++) {
            char c = version.charAt(i);
            if (!Character.isDigit(c)) {
                if (digits.length() > 0) {
                    break;
                }
                continue;
            }
            digits.append(c);
        }
        try {
            return Integer.parseInt(digits.toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void checkFile(Path file, String label) {
        if (Files.exists(file)) {
            ok(label);
        } else {
            fail(label + " missing");
        }
    }

    private void checkPort(int port, String label) {
        try (ServerSocket socket = new ServerSocket(port)) {
            socket.setReuseAddress(true);
            ok("port " + port + " free (" + label + ")");
        } catch (IOException e) {
            fail("port " + port + " already bound (" + label + " cannot start)");
        }
    }

    private void ok(String item) {
        System.out.println("  ✓ " + item);
    }

    private void fail(String item) {
        failures++;
        System.out.println("  ✗ " + item);
    }
}
