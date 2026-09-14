package com.lesofn.archforge.cli.command;

import com.lesofn.archforge.cli.config.DbPasswordResolver;
import com.lesofn.archforge.cli.config.ProjectPaths;
import com.lesofn.archforge.cli.docker.ComposeSupport;
import com.lesofn.archforge.cli.proc.ProcessRunner;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Callable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Spec;

@Command(
        mixinStandardHelpOptions = true,
        name = "db",
        description = "Database operations against the dev postgres container",
        subcommands = {
                DbCommand.Init.class,
                DbCommand.Migrate.class,
                DbCommand.Backup.class,
                DbCommand.Restore.class,
                DbCommand.Shell.class
        })
public class DbCommand implements Callable<Integer> {

    @Spec
    CommandSpec spec;

    @Override
    public Integer call() {
        spec.commandLine().usage(spec.commandLine().getOut());
        return 0;
    }

    @Command(
            mixinStandardHelpOptions = true,
            name = "init",
            description = "Start postgres and apply all Flyway migrations")
    static class Init implements Callable<Integer> {
        @Override
        public Integer call() {
            Path root = ProjectPaths.repoRoot();
            ProcessRunner runner = new ProcessRunner();
            DbPasswordResolver.Result password = DbPasswordResolver.resolve(root, null);
            ComposeSupport compose = new ComposeSupport(runner, root);
            int up = compose.upInfra(List.of("postgres"), Map.of("DB_PASSWORD", password.value()));
            if (up != 0) {
                return up;
            }
            compose.syncDbPassword(DbPasswordResolver.resolveDbUsername(root), password.value());
            return runner.run(
                    List.of("./gradlew", ":archforge-server-admin:flywayMigrate", "-x", "test"),
                    root,
                    Map.of(),
                    true);
        }
    }

    @Command(
            mixinStandardHelpOptions = true,
            name = "migrate",
            aliases = {
                    "update"
            },
            description = "Apply pending Flyway migrations (alias: update)")
    static class Migrate implements Callable<Integer> {
        @Override
        public Integer call() {
            return new ProcessRunner()
                    .run(
                            List.of("./gradlew", ":archforge-server-admin:flywayMigrate", "-x", "test"),
                            ProjectPaths.repoRoot(),
                            Map.of(),
                            true);
        }
    }

    @Command(
            mixinStandardHelpOptions = true,
            name = "backup",
            description = "pg_dump the app database into backup/db/")
    static class Backup implements Callable<Integer> {
        @Override
        public Integer call() {
            Path root = ProjectPaths.repoRoot();
            Path backupDir = ProjectPaths.backupDir(root);
            try {
                Files.createDirectories(backupDir);
            } catch (Exception e) {
                throw new IllegalStateException("Cannot create backup dir", e);
            }
            String stamp = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss", Locale.ROOT)
                    .format(LocalDateTime.now(ZoneId.systemDefault()));
            Path file = backupDir.resolve("archforge_" + stamp + ".sql");
            ComposeSupport compose = new ComposeSupport(new ProcessRunner(), root);
            String user = DbPasswordResolver.resolveDbUsername(root);
            String db = DbPasswordResolver.resolveDbName(root);
            int code = compose.execInfra(List.of("postgres", "pg_dump", "-U", user, "-d", db), file);
            if (code != 0) {
                try {
                    Files.deleteIfExists(file);
                } catch (Exception ignored) {
                    // best-effort cleanup of empty failed dumps
                }
                System.err.println("pg_dump via compose failed; no usable backup written.");
                return code;
            }
            System.out.println("Backup written: " + file);
            return 0;
        }
    }

    @Command(
            mixinStandardHelpOptions = true,
            name = "restore",
            aliases = {
                    "recovery"
            },
            description = "Restore a pg_dump backup into the app database (asks YES; alias: recovery)")
    static class Restore implements Callable<Integer> {

        @Parameters(index = "0", description = "Backup file (e.g. backup/db/archforge_20260101_120000.sql)")
        Path file;

        @Option(names = {
                "-y", "--yes"
        }, description = "Skip interactive confirmation (for scripts)")
        boolean yes;

        @Override
        public Integer call() throws Exception {
            if (!Files.exists(file)) {
                System.err.println("Backup file not found: " + file);
                return 1;
            }
            if (!yes) {
                System.out.print("This overwrites current tables. Type YES to continue: ");
                String answer = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8)).readLine();
                if (!"YES".equals(answer)) {
                    System.out.println("Aborted.");
                    return 1;
                }
            }
            Path root = ProjectPaths.repoRoot();
            ComposeSupport compose = new ComposeSupport(new ProcessRunner(), root);
            String user = DbPasswordResolver.resolveDbUsername(root);
            String db = DbPasswordResolver.resolveDbName(root);
            // Restore into the app database — the dump contains no CREATE
            // DATABASE. The file lives on the host: pipe it via stdin because
            // the container cannot see host paths (psql reads stdin).
            return compose.execInfraStdin(
                    List.of("postgres", "psql", "-U", user, "-d", db), file.toAbsolutePath());
        }
    }

    @Command(
            mixinStandardHelpOptions = true,
            name = "shell",
            description = "Open an interactive psql shell in the dev postgres container")
    static class Shell implements Callable<Integer> {
        @Override
        public Integer call() {
            Path root = ProjectPaths.repoRoot();
            String user = DbPasswordResolver.resolveDbUsername(root);
            String db = DbPasswordResolver.resolveDbName(root);
            return new ComposeSupport(new ProcessRunner(), root)
                    .execInfraInteractive(List.of("postgres", "psql", "-U", user, "-d", db));
        }
    }
}
