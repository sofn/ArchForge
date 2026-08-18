package com.lesofn.archforge.cli.docker;

import com.lesofn.archforge.cli.config.ProjectPaths;
import com.lesofn.archforge.cli.proc.ProcessRunner;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * All docker operations go through compose files under docker/.
 */
public class ComposeSupport {

    private final ProcessRunner processRunner;
    private final Path repoRoot;

    public ComposeSupport(ProcessRunner processRunner, Path repoRoot) {
        this.processRunner = processRunner;
        this.repoRoot = repoRoot;
    }

    public Path composeFile(String profile) {
        Path dockerDir = ProjectPaths.dockerDir(repoRoot);
        return switch (profile == null ? "dev" : profile) {
            case "staging" -> dockerDir.resolve("docker-compose.staging.yml");
            case "prod" -> dockerDir.resolve("docker-compose.prod.yml");
            case "infra", "dev" -> dockerDir.resolve("docker-compose.infra.yml");
            default -> dockerDir.resolve("docker-compose.yml");
        };
    }

    public int up(String profile, List<String> services) {
        List<String> command = base(profile);
        command.add("up");
        command.add("-d");
        command.addAll(services);
        return processRunner.run(command, ProjectPaths.dockerDir(repoRoot));
    }

    public int down(String profile) {
        List<String> command = base(profile);
        command.add("down");
        return processRunner.run(command, ProjectPaths.dockerDir(repoRoot));
    }

    public int stop(String profile) {
        List<String> command = base(profile);
        command.add("stop");
        return processRunner.run(command, ProjectPaths.dockerDir(repoRoot));
    }

    public int exec(String profile, List<String> execArgs) {
        return exec(profile, execArgs, null);
    }

    public int exec(String profile, List<String> execArgs, Path stdoutFile) {
        List<String> command = base(profile);
        command.add("exec");
        command.add("-T");
        command.addAll(execArgs);
        return processRunner.run(command, ProjectPaths.dockerDir(repoRoot), Map.of(), false, stdoutFile);
    }

    public boolean fileExists(String profile) {
        return Files.exists(composeFile(profile));
    }

    private List<String> base(String profile) {
        List<String> command = new ArrayList<>();
        command.add("docker");
        command.add("compose");
        command.add("-f");
        command.add(composeFile(profile).toString());
        Path envFile = ProjectPaths.envFile(repoRoot);
        if (Files.exists(envFile)) {
            command.add("--env-file");
            command.add(envFile.toString());
        }
        return command;
    }
}
