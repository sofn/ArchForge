package com.lesofn.archforge.cli.command;

import com.lesofn.archforge.cli.config.DbPasswordResolver;
import com.lesofn.archforge.cli.config.ProjectPaths;
import com.lesofn.archforge.cli.config.YamlConfigPatcher;
import com.lesofn.archforge.cli.db.FlywayMigration;
import com.lesofn.archforge.cli.docker.ComposeSupport;
import com.lesofn.archforge.cli.proc.ProcessRunner;
import com.lesofn.archforge.cli.secret.SecretGenerator;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(mixinStandardHelpOptions = true, name = "init",
        description = "One-time setup: generate secrets into .env, patch dev/test yaml and — for dev — start " +
                "postgres/redis, sync the DB role password and apply Flyway migrations. Without --write it only " +
                "reports what it would do and changes nothing.")
public class InitCommand implements Callable<Integer> {

    @Option(names = "--profile", defaultValue = "dev", description = "dev|test|staging|prod")
    String profile;

    @Option(names = "--write", description = "Actually do it (default: dry-run report, nothing is changed)")
    boolean write;

    private final Path repoRoot;
    private final ProcessRunner processRunner;

    public InitCommand() {
        this(ProjectPaths.repoRoot(), new ProcessRunner());
    }

    InitCommand(Path repoRoot, ProcessRunner processRunner) {
        this.repoRoot = repoRoot;
        this.processRunner = processRunner;
    }

    @Override
    public Integer call() {
        if (!write) {
            // a dry-run must not touch anything: no .env, no yaml, no containers, no ALTER ROLE, no migration
            System.out.println("Dry-run — nothing was changed. With --write, init would:");
            System.out.println("  - write " + SecretGenerator.generate().size() + " secret(s) to .env (existing keys kept)");
            System.out.println("  - patch application-dev/test yaml placeholders where needed");
            if ("dev".equals(profile)) {
                System.out.println("  - start postgres/redis, sync the DB role password, apply Flyway migrations");
            }
            System.out.println("Re-run with --write to apply.");
            return 0;
        }
        Map<String, String> written = SecretGenerator.writeIdempotent(ProjectPaths.envFile(repoRoot));
        System.out.println("Wrote " + written.size() + " new secret(s) to .env (existing keys kept).");

        YamlConfigPatcher.patchDevAndTest(repoRoot);
        System.out.println("Patched application-dev/test yaml placeholders where needed.");

        if ("dev".equals(profile)) {
            DbPasswordResolver.Result password = DbPasswordResolver.resolve(repoRoot, null);
            DbPasswordResolver.printEnvHint(password);
            ComposeSupport compose = new ComposeSupport(processRunner, repoRoot);
            int infra = compose.upInfra(List.of("postgres", "redis"), Map.of("DB_PASSWORD", password.value()));
            if (infra != 0) {
                return infra;
            }
            compose.syncDbPassword(DbPasswordResolver.resolveDbUsername(repoRoot), password.value());
            int migrate = FlywayMigration.run(processRunner, repoRoot);
            if (migrate != 0) {
                return migrate;
            }
        } else {
            System.out.println("Profile " + profile + ": skipped docker and data import.");
        }
        return 0;
    }
}
