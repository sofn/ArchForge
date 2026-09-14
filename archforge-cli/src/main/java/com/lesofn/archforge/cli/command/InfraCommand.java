package com.lesofn.archforge.cli.command;

import com.lesofn.archforge.cli.config.DbPasswordResolver;
import com.lesofn.archforge.cli.config.ProjectPaths;
import com.lesofn.archforge.cli.docker.ComposeSupport;
import com.lesofn.archforge.cli.proc.ProcessRunner;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import org.jspecify.annotations.Nullable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Spec;

@Command(
        mixinStandardHelpOptions = true,
        name = "infra",
        description = "Manage dependency containers (postgres + redis) via docker-compose.infra.yml",
        subcommands = {
                InfraCommand.Up.class,
                InfraCommand.Down.class,
                InfraCommand.Stop.class,
                InfraCommand.Clean.class,
                InfraCommand.Status.class,
                InfraCommand.Logs.class
        })
public class InfraCommand implements Callable<Integer> {

    @Spec
    CommandSpec spec;

    @Override
    public Integer call() {
        spec.commandLine().usage(spec.commandLine().getOut());
        return 0;
    }

    @Command(
            mixinStandardHelpOptions = true,
            name = "up",
            description = "Start postgres + redis and wait until healthy; syncs DB password into the live role")
    static class Up implements Callable<Integer> {
        @Option(
                names = "--db-password",
                description = "PostgreSQL password; resolved from DB_PASSWORD env or .env when unset, " +
                        "otherwise a 16-char password is generated and persisted to .env")
        @Nullable
        String dbPassword;

        @Override
        public Integer call() {
            Path repoRoot = ProjectPaths.repoRoot();
            DbPasswordResolver.Result password = DbPasswordResolver.resolve(repoRoot, dbPassword);
            DbPasswordResolver.printEnvHint(password);
            ComposeSupport compose = new ComposeSupport(new ProcessRunner(), repoRoot);
            int code = compose.upInfra(List.of("postgres", "redis"), Map.of("DB_PASSWORD", password.value()));
            if (code != 0) {
                return code;
            }
            return compose.syncDbPassword(DbPasswordResolver.resolveDbUsername(repoRoot), password.value());
        }
    }

    @Command(
            mixinStandardHelpOptions = true,
            name = "down",
            description = "Remove dependency containers; -v also removes named volumes (destroys data)")
    static class Down implements Callable<Integer> {
        @Option(
                names = {
                        "-v", "--volumes"
                },
                description = "Also remove named volumes — destroys local dev data")
        boolean volumes;

        @Option(names = {
                "-y", "--yes"
        }, description = "Skip confirmation when --volumes is used")
        boolean yes;

        @Override
        public Integer call() throws IOException {
            ComposeSupport compose = new ComposeSupport(new ProcessRunner(), ProjectPaths.repoRoot());
            if (volumes && !yes && !confirmVolumes()) {
                return 1;
            }
            if (volumes && compose.hasRunningInfra()) {
                System.out.println("Containers are still running — bringing them down first...");
            }
            return compose.downInfra(volumes);
        }
    }

    @Command(
            mixinStandardHelpOptions = true,
            name = "clean",
            hidden = true,
            description = "Alias for `infra down --volumes`")
    static class Clean implements Callable<Integer> {
        @Option(names = {
                "-y", "--yes"
        }, description = "Skip the confirmation prompt")
        boolean yes;

        @Override
        public Integer call() throws IOException {
            Down down = new Down();
            down.volumes = true;
            down.yes = yes;
            return down.call();
        }
    }

    @Command(mixinStandardHelpOptions = true, name = "stop", description = "Pause dependency containers")
    static class Stop implements Callable<Integer> {
        @Override
        public Integer call() {
            return new ComposeSupport(new ProcessRunner(), ProjectPaths.repoRoot()).stopInfra();
        }
    }

    @Command(mixinStandardHelpOptions = true, name = "status", description = "Show dependency container state")
    static class Status implements Callable<Integer> {
        @Override
        public Integer call() {
            return new ComposeSupport(new ProcessRunner(), ProjectPaths.repoRoot()).psInfra();
        }
    }

    @Command(
            mixinStandardHelpOptions = true,
            name = "logs",
            description = "Tail dependency container logs")
    static class Logs implements Callable<Integer> {
        @Option(names = {
                "-f", "--follow"
        }, description = "Follow output")
        boolean follow;

        @Override
        public Integer call() {
            return new ComposeSupport(new ProcessRunner(), ProjectPaths.repoRoot()).logsInfra(follow);
        }
    }

    private static boolean confirmVolumes() throws IOException {
        System.out.println(
                "This removes the containers and the postgres-data volume — all local dev data is lost.");
        System.out.print("Type YES to continue: ");
        String answer = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8)).readLine();
        if (!"YES".equals(answer)) {
            System.out.println("Aborted.");
            return false;
        }
        return true;
    }
}
