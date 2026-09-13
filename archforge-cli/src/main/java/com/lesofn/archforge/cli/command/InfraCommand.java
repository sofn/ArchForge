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
import picocli.CommandLine.Option;

@Command(
        mixinStandardHelpOptions = true,
        name = "infra",
        description = "Manage dependency containers via docker compose",
        subcommands = {
                InfraCommand.Up.class, InfraCommand.Down.class, InfraCommand.Stop.class, InfraCommand.Clean.class
        })
public class InfraCommand {

    @Command(mixinStandardHelpOptions = true, name = "up", description = "Start postgres/redis")
    static class Up implements Callable<Integer> {
        @Option(names = "--profile", defaultValue = "dev")
        String profile;

        @Option(
                names = "--db-password",
                description = "PostgreSQL password; if unset, resolved from DB_PASSWORD env or .env, " +
                        "otherwise a 16-char password is generated and persisted to .env")
        @Nullable
        String dbPassword;

        @Override
        public Integer call() {
            Path repoRoot = ProjectPaths.repoRoot();
            DbPasswordResolver.Result password = DbPasswordResolver.resolve(repoRoot, dbPassword);
            DbPasswordResolver.printEnvHint(password);
            ComposeSupport compose = new ComposeSupport(new ProcessRunner(), repoRoot);
            int code = compose.up(profile, List.of("postgres", "redis"), Map.of("DB_PASSWORD", password.value()));
            if (code != 0) {
                return code;
            }
            return compose.syncDbPassword(
                    profile, DbPasswordResolver.resolveDbUsername(repoRoot), password.value());
        }
    }

    @Command(mixinStandardHelpOptions = true, name = "down", description = "Remove dependency containers")
    static class Down implements Callable<Integer> {
        @Option(names = "--profile", defaultValue = "dev")
        String profile;

        @Override
        public Integer call() {
            return new ComposeSupport(new ProcessRunner(), ProjectPaths.repoRoot()).down(profile);
        }
    }

    @Command(
            mixinStandardHelpOptions = true,
            name = "clean",
            description = "Remove dependency containers AND named volumes (destroys local dev data)")
    static class Clean implements Callable<Integer> {
        @Option(names = "--profile", defaultValue = "dev")
        String profile;

        @Option(names = "--yes", description = "Skip the confirmation prompt (for scripts)")
        boolean yes;

        @Override
        public Integer call() throws IOException {
            Path repoRoot = ProjectPaths.repoRoot();
            ComposeSupport compose = new ComposeSupport(new ProcessRunner(), repoRoot);
            if (!yes) {
                System.out.println(
                        "This removes the containers and the postgres-data volume — all local dev data is lost.");
                System.out.print("Type YES to continue: ");
                String answer = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8)).readLine();
                if (!"YES".equals(answer)) {
                    System.out.println("Aborted.");
                    return 1;
                }
            }
            if (compose.hasRunningServices(profile)) {
                System.out.println("Containers are still running — bringing them down first...");
                int code = compose.down(profile);
                if (code != 0) {
                    return code;
                }
            }
            return compose.downVolumes(profile);
        }
    }

    @Command(mixinStandardHelpOptions = true, name = "stop", description = "Pause dependency containers")
    static class Stop implements Callable<Integer> {
        @Option(names = "--profile", defaultValue = "dev")
        String profile;

        @Override
        public Integer call() {
            return new ComposeSupport(new ProcessRunner(), ProjectPaths.repoRoot()).stop(profile);
        }
    }
}
