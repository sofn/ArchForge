package com.lesofn.archforge.cli.docker;

import com.lesofn.archforge.cli.config.Profile;
import com.lesofn.archforge.cli.config.ProjectPaths;
import com.lesofn.archforge.cli.proc.ProcessRunner;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.jspecify.annotations.Nullable;

/**
 * All docker operations go through compose files under {@code docker/}.
 * Two layers: the infra file (postgres + redis, profile-agnostic) and the
 * per-profile stack file selected by {@link Profile}.
 */
public class ComposeSupport {

    private final ProcessRunner processRunner;
    private final Path repoRoot;

    public ComposeSupport(ProcessRunner processRunner, Path repoRoot) {
        this.processRunner = processRunner;
        this.repoRoot = repoRoot;
    }

    public Path infraFile() {
        return ProjectPaths.dockerDir(repoRoot).resolve("docker-compose.infra.yml");
    }

    public Path stackFile(Profile profile) {
        Path dockerDir = ProjectPaths.dockerDir(repoRoot);
        return switch (profile) {
            case dev -> dockerDir.resolve("docker-compose.yml");
            case fulljre -> dockerDir.resolve("docker-compose.fulljre.yml");
            case jlink -> dockerDir.resolve("docker-compose.jlink.yml");
            case nativeImage -> dockerDir.resolve("docker-compose.native.yml");
            case staging -> dockerDir.resolve("docker-compose.staging.yml");
            case prod -> dockerDir.resolve("docker-compose.prod.yml");
        };
    }

    // ---- infra layer (postgres + redis) ----

    public int upInfra(List<String> services, Map<String, String> extraEnv) {
        List<String> command = infraBase();
        command.add("up");
        command.add("-d");
        command.add("--wait");
        command.addAll(services);
        return processRunner.run(command, ProjectPaths.dockerDir(repoRoot), extraEnv, false);
    }

    public int downInfra(boolean volumes) {
        List<String> command = infraBase();
        command.add("down");
        if (volumes) {
            command.add("-v");
            command.add("--remove-orphans");
        }
        return processRunner.run(command, ProjectPaths.dockerDir(repoRoot));
    }

    public int stopInfra() {
        List<String> command = infraBase();
        command.add("stop");
        return processRunner.run(command, ProjectPaths.dockerDir(repoRoot));
    }

    public int psInfra() {
        List<String> command = infraBase();
        command.add("ps");
        return processRunner.run(command, ProjectPaths.dockerDir(repoRoot), Map.of(), true);
    }

    public int logsInfra(boolean follow) {
        List<String> command = infraBase();
        command.add("logs");
        if (follow) {
            command.add("-f");
        }
        command.add("--tail");
        command.add("100");
        return processRunner.run(command, ProjectPaths.dockerDir(repoRoot), Map.of(), true);
    }

    /** True when any infra service has a running container. */
    public boolean hasRunningInfra() {
        List<String> command = infraBase();
        command.add("ps");
        command.add("--status");
        command.add("running");
        command.add("-q");
        ProcessRunner.RunResult result = processRunner.runCapture(command, ProjectPaths.dockerDir(repoRoot));
        return result.exitCode() == 0 && !result.stdout().isBlank();
    }

    public int execInfra(List<String> execArgs) {
        return execInfra(execArgs, null);
    }

    public int execInfra(List<String> execArgs, @Nullable Path stdoutFile) {
        List<String> command = infraBase();
        command.add("exec");
        command.add("-T");
        command.addAll(execArgs);
        return processRunner.run(command, ProjectPaths.dockerDir(repoRoot), Map.of(), false, stdoutFile);
    }

    /** Exec with a host file piped to the process stdin — e.g. {@code psql < dump.sql}. */
    public int execInfraStdin(List<String> execArgs, Path stdinFile) {
        List<String> command = infraBase();
        command.add("exec");
        command.add("-T");
        command.addAll(execArgs);
        return processRunner.run(
                command, ProjectPaths.dockerDir(repoRoot), Map.of(), false, null, stdinFile);
    }

    /** Interactive exec (no -T) — for {@code db shell}. */
    public int execInfraInteractive(List<String> execArgs) {
        List<String> command = infraBase();
        command.add("exec");
        command.addAll(execArgs);
        return processRunner.run(command, ProjectPaths.dockerDir(repoRoot), Map.of(), true);
    }

    /**
     * The postgres data volume keeps the password it was initialized with —
     * {@code POSTGRES_PASSWORD} only applies on first init. Align the live
     * role with the resolved password so a stale volume cannot silently
     * desync (local connections inside the container are trust-authenticated).
     */
    public int syncDbPassword(String user, String password) {
        String sql = "ALTER USER \"" + user.replace("\"", "\"\"") + "\" WITH PASSWORD '" + password.replace("'", "''") + "'";
        int code = execInfra(List.of("postgres", "psql", "-U", user, "-d", "postgres", "-c", sql));
        if (code != 0) {
            System.out.println(
                    "WARN: could not sync DB_PASSWORD into postgres — the app may fail to connect.");
        }
        return code;
    }

    // ---- stack layer (per-profile compose file) ----

    public int upStack(Profile profile) {
        List<String> command = stackBase(profile);
        command.add("up");
        command.add("-d");
        command.add("--wait");
        return processRunner.run(command, ProjectPaths.dockerDir(repoRoot));
    }

    public int downStack(Profile profile, boolean volumes) {
        List<String> command = stackBase(profile);
        command.add("down");
        if (volumes) {
            command.add("-v");
            command.add("--remove-orphans");
        }
        return processRunner.run(command, ProjectPaths.dockerDir(repoRoot));
    }

    public int psStack(Profile profile) {
        List<String> command = stackBase(profile);
        command.add("ps");
        return processRunner.run(command, ProjectPaths.dockerDir(repoRoot), Map.of(), true);
    }

    public boolean stackFileExists(Profile profile) {
        return Files.exists(stackFile(profile));
    }

    private List<String> infraBase() {
        return composeBase(infraFile());
    }

    private List<String> stackBase(Profile profile) {
        return composeBase(stackFile(profile));
    }

    private List<String> composeBase(Path file) {
        List<String> command = new ArrayList<>();
        command.add("docker");
        command.add("compose");
        command.add("-f");
        command.add(file.toString());
        Path envFile = ProjectPaths.envFile(repoRoot);
        if (Files.exists(envFile)) {
            command.add("--env-file");
            command.add(envFile.toString());
        }
        return command;
    }
}
