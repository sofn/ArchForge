package com.lesofn.archforge.cli.command;

import com.lesofn.archforge.cli.config.ProjectPaths;
import com.lesofn.archforge.cli.docker.ComposeSupport;
import com.lesofn.archforge.cli.proc.ProcessRunner;
import java.util.List;
import java.util.concurrent.Callable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(
        name = "infra",
        description = "Manage dependency containers via docker compose",
        subcommands = {
                InfraCommand.Up.class, InfraCommand.Down.class, InfraCommand.Stop.class
        })
public class InfraCommand {

    @Command(name = "up", description = "Start postgres/redis")
    static class Up implements Callable<Integer> {
        @Option(names = "--profile", defaultValue = "dev")
        String profile;

        @Override
        public Integer call() {
            return new ComposeSupport(new ProcessRunner(), ProjectPaths.repoRoot())
                    .up(profile, List.of("postgres", "redis"));
        }
    }

    @Command(name = "down", description = "Remove dependency containers")
    static class Down implements Callable<Integer> {
        @Option(names = "--profile", defaultValue = "dev")
        String profile;

        @Override
        public Integer call() {
            return new ComposeSupport(new ProcessRunner(), ProjectPaths.repoRoot()).down(profile);
        }
    }

    @Command(name = "stop", description = "Pause dependency containers")
    static class Stop implements Callable<Integer> {
        @Option(names = "--profile", defaultValue = "dev")
        String profile;

        @Override
        public Integer call() {
            return new ComposeSupport(new ProcessRunner(), ProjectPaths.repoRoot()).stop(profile);
        }
    }
}
