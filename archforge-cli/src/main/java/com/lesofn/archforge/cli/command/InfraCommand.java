package com.lesofn.archforge.cli.command;

import com.lesofn.archforge.cli.config.DbPasswordResolver;
import com.lesofn.archforge.cli.config.ProjectPaths;
import com.lesofn.archforge.cli.docker.ComposeSupport;
import com.lesofn.archforge.cli.proc.ProcessRunner;
import com.lesofn.archforge.cli.secret.SecretGenerator;
import java.io.IOException;
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
            return syncDbPassword(compose, repoRoot, password.value());
        }

        /**
         * The postgres data volume keeps the password it was initialized with —
         * {@code POSTGRES_PASSWORD} only applies on first init. Align the live
         * role with the resolved password so a stale volume cannot silently
         * desync (local connections inside the container are trust-authenticated).
         */
        private int syncDbPassword(ComposeSupport compose, Path repoRoot, String password) {
            String user = dbUsername(repoRoot);
            String sql = "ALTER USER \"" + user.replace("\"", "\"\"") + "\" WITH PASSWORD '" + password.replace("'", "''") +
                    "'";
            int code = compose.exec(
                    profile, List.of("postgres", "psql", "-U", user, "-d", "postgres", "-c", sql));
            if (code != 0) {
                System.out.println(
                        "WARN: could not sync DB_PASSWORD into postgres — the app may fail to connect.");
            }
            return code;
        }

        private String dbUsername(Path repoRoot) {
            String env = System.getenv("DB_USERNAME");
            if (env != null && !env.isBlank()) {
                return env;
            }
            try {
                String fromFile = SecretGenerator.readEnv(ProjectPaths.envFile(repoRoot)).get("DB_USERNAME");
                if (fromFile != null && !fromFile.isBlank()) {
                    return fromFile;
                }
            } catch (IOException ignored) {
                // fall through to default
            }
            return "archforge";
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
