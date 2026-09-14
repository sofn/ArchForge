package com.lesofn.archforge.cli.command;

import com.lesofn.archforge.cli.config.DbPasswordResolver;
import com.lesofn.archforge.cli.config.Profile;
import com.lesofn.archforge.cli.config.ProjectPaths;
import com.lesofn.archforge.cli.docker.ComposeSupport;
import com.lesofn.archforge.cli.proc.ProcessRunner;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(
        mixinStandardHelpOptions = true,
        name = "up",
        description = "Start the full containerized stack: deps + Flyway migrate + app containers")
public class UpCommand implements Callable<Integer> {

    @Option(
            names = {
                    "-p", "--profile"
            },
            defaultValue = "dev",
            converter = Profile.Converter.class,
            description = "Stack profile: ${COMPLETION-CANDIDATES} (default: ${DEFAULT-VALUE})")
    Profile profile;

    @Override
    public Integer call() {
        Path root = ProjectPaths.repoRoot();
        ComposeSupport compose = new ComposeSupport(new ProcessRunner(), root);
        ProcessRunner runner = new ProcessRunner();
        DbPasswordResolver.Result password = DbPasswordResolver.resolve(root, null);
        Map<String, String> env = Map.of(
                "DB_PASSWORD", password.value(),
                "DB_USERNAME", DbPasswordResolver.resolveDbUsername(root));
        if (profile == Profile.allinone) {
            // The all-in-one stack carries its own postgres/redis — skip the
            // shared infra compose to avoid a duplicate database.
            return compose.upStack(profile, env);
        }
        int infra = compose.upInfra(List.of("postgres", "redis"), Map.of("DB_PASSWORD", password.value()));
        if (infra != 0) {
            return infra;
        }
        compose.syncDbPassword(DbPasswordResolver.resolveDbUsername(root), password.value());
        int migrate = runner.run(
                List.of("./gradlew", ":archforge-server-admin:flywayMigrate", "-x", "test"),
                root,
                Map.of(),
                true);
        if (migrate != 0) {
            System.err.println("flywayMigrate returned " + migrate);
        }
        return compose.upStack(profile, env);
    }
}
