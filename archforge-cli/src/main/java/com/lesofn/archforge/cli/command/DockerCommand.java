package com.lesofn.archforge.cli.command;

import com.lesofn.archforge.cli.config.ProjectPaths;
import com.lesofn.archforge.cli.docker.ComposeSupport;
import com.lesofn.archforge.cli.proc.ProcessRunner;
import java.util.List;
import java.util.concurrent.Callable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(
        name = "docker",
        description = "Manage business images",
        subcommands = {
                DockerCommand.Up.class, DockerCommand.Down.class
        })
public class DockerCommand {

    @Command(name = "up", description = "Start latest images; start deps and migrate if needed")
    static class Up implements Callable<Integer> {
        @Option(names = "--profile", defaultValue = "dev")
        String profile;

        @Override
        public Integer call() {
            ComposeSupport compose = new ComposeSupport(new ProcessRunner(), ProjectPaths.repoRoot());
            int infra = compose.up(profile, List.of("postgres", "redis"));
            if (infra != 0) {
                return infra;
            }
            int migrate = new ProcessRunner()
                    .run(
                            List.of("./gradlew", ":archforge-server-admin:flywayMigrate", "-x", "test"),
                            ProjectPaths.repoRoot());
            if (migrate != 0) {
                System.err.println("flywayMigrate returned " + migrate);
            }
            return compose.up(profile, List.of());
        }
    }

    @Command(name = "down", description = "Stop all compose services")
    static class Down implements Callable<Integer> {
        @Option(names = "--profile", defaultValue = "dev")
        String profile;

        @Override
        public Integer call() {
            return new ComposeSupport(new ProcessRunner(), ProjectPaths.repoRoot()).down(profile);
        }
    }
}
