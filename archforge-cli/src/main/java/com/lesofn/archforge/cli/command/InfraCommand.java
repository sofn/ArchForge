package com.lesofn.archforge.cli.command;

import com.lesofn.archforge.cli.config.DbPasswordResolver;
import com.lesofn.archforge.cli.config.ProjectPaths;
import com.lesofn.archforge.cli.docker.ComposeSupport;
import com.lesofn.archforge.cli.proc.ProcessRunner;
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
                InfraCommand.Up.class, InfraCommand.Down.class, InfraCommand.Stop.class
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
