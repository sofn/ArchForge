package com.lesofn.archforge.cli.proc;

import com.lesofn.archforge.cli.config.DbPasswordResolver;
import com.lesofn.archforge.cli.config.ProjectPaths;
import com.lesofn.archforge.cli.docker.ComposeSupport;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * The local dev stack: postgres + redis (infra compose) plus four detached
 * OS processes — server-admin / server-web bootRun and the two pnpm dev
 * servers. Each detached process records its PID under {@code run/<name>.pid}
 * so {@code down} / {@code status} / {@code restart} can manage it.
 */
public final class DevStack {

    private DevStack() {
    }

    public record Service(String name, List<String> command, Path workingDir, String logFile) {
    }

    public static List<Service> services(Path root) {
        Path logs = logsDir(root);
        List<Service> list = new java.util.ArrayList<>();
        list.add(new Service("server-admin", List.of("./gradlew", ":archforge-server-admin:bootRun"), root, logs.resolve(
                "server-admin.log").toString()));
        list.add(new Service("server-web", List.of("./gradlew", ":archforge-server-web:bootRun"), root, logs.resolve(
                "server-web.log").toString()));
        Path adminRepo = ProjectPaths.adminRepo(root);
        if (Files.exists(adminRepo)) {
            list.add(new Service("admin-ui", List.of("pnpm", "dev"), adminRepo, logs.resolve("admin-ui.log").toString()));
        }
        Path webRepo = ProjectPaths.webRepo(root);
        if (Files.exists(webRepo)) {
            list.add(new Service("web-ui", List.of("pnpm", "dev"), webRepo, logs.resolve("web-ui.log").toString()));
        }
        return list;
    }

    /** Starts infra deps, then all dev services detached; writes PID files. */
    public static int start(Path root) {
        ProcessRunner runner = new ProcessRunner();
        ComposeSupport compose = new ComposeSupport(runner, root);
        DbPasswordResolver.Result password = DbPasswordResolver.resolve(root, null);
        int infra = compose.upInfra(List.of("postgres", "redis"), Map.of("DB_PASSWORD", password.value()));
        if (infra != 0) {
            return infra;
        }
        compose.syncDbPassword(DbPasswordResolver.resolveDbUsername(root), password.value());
        try {
            Files.createDirectories(logsDir(root));
            Files.createDirectories(runDir(root));
        } catch (IOException e) {
            throw new IllegalStateException("Cannot create logs/ or run/ directories", e);
        }
        Map<String, String> env = loadDotEnv(root);
        for (Service service : services(root)) {
            ensureDependencies(runner, service);
            Process process = runner.startDetached(
                    service.command(), service.workingDir(), new java.io.File(service.logFile()), env);
            writePid(root, service.name(), process.pid());
            System.out.println("started " + service.name() + " (pid " + process.pid() + ") → " + service.logFile());
        }
        System.out.println("Dev stack running. Logs under " + logsDir(root) + " — `archforge logs -f` to tail.");
        return 0;
    }

    /** Stops all dev services via their PID files. Returns services stopped. */
    public static int stop(Path root) {
        int stopped = 0;
        for (Service service : services(root)) {
            Optional<ProcessHandle> handle = liveProcess(root, service.name());
            if (handle.isEmpty()) {
                continue;
            }
            // Kill the whole tree: `gradlew bootRun` forks a worker JVM and
            // pnpm spawns vite/next grandchildren — the wrapper's death alone
            // would orphan them and leave ports bound. Node wrappers may
            // swallow SIGTERM, so escalate to forcible kill after a grace wait.
            ProcessHandle process = handle.get();
            List<ProcessHandle> tree = new java.util.ArrayList<>();
            process.descendants().forEach(tree::add);
            tree.add(process);
            tree.forEach(ProcessHandle::destroy);
            try {
                process.onExit().get(5, java.util.concurrent.TimeUnit.SECONDS);
            } catch (Exception ignored) {
                // still alive or interrupted — escalate below
            }
            tree.forEach(ProcessHandle::destroyForcibly);
            stopped++;
            System.out.println("stopped " + service.name() + " (pid " + process.pid() + ")");
        }
        clearPids(root);
        if (stopped == 0) {
            System.out.println("No dev processes found (no live run/*.pid).");
        }
        return stopped;
    }

    /** Prints a liveness table for dev services (pid file + process alive). */
    public static int status(Path root) {
        System.out.println("Dev processes:");
        boolean any = false;
        for (Service service : services(root)) {
            Optional<ProcessHandle> handle = liveProcess(root, service.name());
            String state = handle.map(h -> "running (pid " + h.pid() + ")").orElse("stopped");
            System.out.printf("  %-14s %s%n", service.name(), state);
            any |= handle.isPresent();
        }
        if (!any) {
            System.out.println("  (nothing running — `archforge dev` to start)");
        }
        return 0;
    }

    public static Path logsDir(Path root) {
        return root.resolve("logs");
    }

    public static Path runDir(Path root) {
        return root.resolve("run");
    }

    /** Loads .env so detached bootRun processes can resolve ${DB_PASSWORD} etc. */
    private static Map<String, String> loadDotEnv(Path root) {
        Path envFile = ProjectPaths.envFile(root);
        if (!Files.exists(envFile)) {
            System.out.println("note: no .env found — dev processes inherit the current shell env only.");
            return Map.of();
        }
        try {
            Map<String, String> env = new java.util.HashMap<>();
            for (String line : Files.readAllLines(envFile, StandardCharsets.UTF_8)) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                    continue;
                }
                int eq = trimmed.indexOf('=');
                if (eq > 0) {
                    env.put(trimmed.substring(0, eq).trim(), trimmed.substring(eq + 1).trim());
                }
            }
            return env;
        } catch (IOException e) {
            System.out.println("note: could not read .env: " + e.getMessage());
            return Map.of();
        }
    }

    private static void ensureDependencies(ProcessRunner runner, Service service) {
        if ("pnpm".equals(service.command().get(0)) && !Files.exists(service.workingDir().resolve("node_modules"))) {
            runner.run(List.of("pnpm", "install"), service.workingDir(), Map.of(), true);
        }
    }

    private static void writePid(Path root, String name, long pid) {
        try {
            Files.writeString(runDir(root).resolve(name + ".pid"), Long.toString(pid), StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.out.println("note: could not write run/" + name + ".pid: " + e.getMessage());
        }
    }

    private static Optional<ProcessHandle> liveProcess(Path root, String name) {
        Path pidFile = runDir(root).resolve(name + ".pid");
        if (!Files.exists(pidFile)) {
            return Optional.empty();
        }
        try {
            long pid = Long.parseLong(Files.readString(pidFile, StandardCharsets.UTF_8).trim());
            return ProcessHandle.of(pid).filter(ProcessHandle::isAlive);
        } catch (IOException | NumberFormatException e) {
            return Optional.empty();
        }
    }

    private static void clearPids(Path root) {
        Path dir = runDir(root);
        if (!Files.isDirectory(dir)) {
            return;
        }
        try (Stream<Path> files = Files.list(dir)) {
            files.filter(p -> String.valueOf(p.getFileName()).endsWith(".pid")).forEach(p -> {
                try {
                    Files.deleteIfExists(p);
                } catch (IOException ignored) {
                    // stale pid files are harmless
                }
            });
        } catch (IOException ignored) {
            // best-effort cleanup
        }
    }
}
