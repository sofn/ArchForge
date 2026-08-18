package com.lesofn.archforge.cli.command;

import com.lesofn.archforge.cli.config.ProjectPaths;
import com.lesofn.archforge.cli.config.YamlConfigPatcher;
import com.lesofn.archforge.cli.docker.ComposeSupport;
import com.lesofn.archforge.cli.proc.ProcessRunner;
import com.lesofn.archforge.cli.secret.SecretGenerator;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "init", description = "Generate credentials and optionally start local dependencies")
public class InitCommand implements Callable<Integer> {

    @Option(names = "--profile", defaultValue = "dev", description = "dev|test|staging|prod")
    String profile;

    @Option(names = "--write", description = "Persist generated secrets into .env")
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
        Map<String, String> generated = SecretGenerator.generate();
        if (write) {
            Map<String, String> written = SecretGenerator.writeIdempotent(ProjectPaths.envFile(repoRoot));
            System.out.println("Wrote " + written.size() + " new secret(s) to .env (existing keys kept).");
        } else {
            System.out.println("Dry-run: would generate " + generated.size() + " secrets. Re-run with --write to persist.");
        }

        YamlConfigPatcher.patchDevAndTest(repoRoot);
        System.out.println("Patched application-dev/test yaml placeholders where needed.");

        if ("dev".equals(profile)) {
            ComposeSupport compose = new ComposeSupport(processRunner, repoRoot);
            int infra = compose.up("dev", List.of("postgres", "redis"));
            if (infra != 0) {
                return infra;
            }
            int migrate = processRunner.run(
                    List.of("./gradlew", ":archforge-server-admin:flywayMigrate", "-x", "test"), repoRoot);
            if (migrate != 0) {
                System.err.println("flywayMigrate returned " + migrate + " (ok if Flyway plugin is not wired yet).");
            }
        } else {
            System.out.println("Profile " + profile + ": skipped docker and data import.");
        }
        return 0;
    }
}
