package com.lesofn.archforge.cli.command;

import com.lesofn.archforge.cli.config.ProjectPaths;
import com.lesofn.archforge.cli.docker.ComposeSupport;
import com.lesofn.archforge.cli.proc.ProcessRunner;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "up", description = "Start the full stack")
public class UpCommand implements Callable<Integer> {

    @Option(names = "--profile", defaultValue = "dev")
    String profile;

    @Override
    public Integer call() {
        Path root = ProjectPaths.repoRoot();
        ProcessRunner runner = new ProcessRunner();
        ComposeSupport compose = new ComposeSupport(runner, root);
        if (!"dev".equals(profile)) {
            return compose.up(profile, List.of());
        }
        int infra = compose.up("dev", List.of("postgres", "redis"));
        if (infra != 0) {
            return infra;
        }
        Path logs = root.resolve("logs");
        try {
            Files.createDirectories(logs);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        runner.startDetached(
                List.of("./gradlew", ":archforge-server-admin:bootRun"),
                root,
                logs.resolve("server-admin.log").toFile());
        runner.startDetached(
                List.of("./gradlew", ":archforge-server-web:bootRun"),
                root,
                logs.resolve("server-web.log").toFile());
        startFrontend(runner, ProjectPaths.adminRepo(root), logs.resolve("admin.log").toFile());
        startFrontend(runner, ProjectPaths.webRepo(root), logs.resolve("web.log").toFile());
        System.out.println("Started backend and frontend processes. Logs under " + logs);
        return 0;
    }

    private void startFrontend(ProcessRunner runner, Path repo, java.io.File logFile) {
        if (!Files.exists(repo)) {
            return;
        }
        if (!Files.exists(repo.resolve("node_modules"))) {
            runner.run(List.of("pnpm", "install"), repo);
        }
        runner.startDetached(List.of("pnpm", "dev"), repo, logFile);
    }
}
