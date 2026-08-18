package com.lesofn.archforge.cli.command;

import com.lesofn.archforge.cli.config.ProjectPaths;
import com.lesofn.archforge.cli.docker.ComposeSupport;
import com.lesofn.archforge.cli.proc.ProcessRunner;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.Callable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(
        name = "db",
        description = "Database operations",
        subcommands = {
                DbCommand.Init.class, DbCommand.Update.class, DbCommand.Backup.class, DbCommand.Recovery.class
        })
public class DbCommand {

    @Command(name = "init", description = "Start database and apply migrations")
    static class Init implements Callable<Integer> {
        @Override
        public Integer call() {
            Path root = ProjectPaths.repoRoot();
            ProcessRunner runner = new ProcessRunner();
            ComposeSupport compose = new ComposeSupport(runner, root);
            int up = compose.up("dev", List.of("postgres"));
            if (up != 0) {
                return up;
            }
            return runner.run(List.of("./gradlew", ":archforge-server-admin:flywayMigrate", "-x", "test"), root);
        }
    }

    @Command(name = "update", description = "Apply latest Flyway migrations")
    static class Update implements Callable<Integer> {
        @Override
        public Integer call() {
            return new ProcessRunner()
                    .run(
                            List.of("./gradlew", ":archforge-server-admin:flywayMigrate", "-x", "test"),
                            ProjectPaths.repoRoot());
        }
    }

    @Command(name = "backup", description = "Dump database into backup/db/")
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
            String stamp = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss").format(LocalDateTime.now());
            Path file = backupDir.resolve("archforge_" + stamp + ".sql");
            ComposeSupport compose = new ComposeSupport(new ProcessRunner(), root);
            int code = compose.exec(
                    "dev",
                    List.of("postgres", "pg_dump", "-U", "archforge", "-d", "archforge_user"),
                    file);
            if (code != 0) {
                System.err.println("pg_dump via compose failed; no usable backup written.");
                return code;
            }
            System.out.println("Backup written: " + file);
            return 0;
        }
    }

    @Command(name = "recovery", description = "Restore a backup after confirmation")
    static class Recovery implements Callable<Integer> {
        @Option(names = "--file", required = true)
        Path file;

        @Option(names = "--yes", description = "Skip interactive confirmation (automation only)")
        boolean yes;

        @Override
        public Integer call() throws Exception {
            if (!Files.exists(file)) {
                System.err.println("Backup file not found: " + file);
                return 1;
            }
            if (!yes) {
                System.out.print("This will overwrite current tables. Type YES to continue: ");
                String answer = new BufferedReader(new InputStreamReader(System.in)).readLine();
                if (!"YES".equals(answer)) {
                    System.out.println("Aborted.");
                    return 1;
                }
            }
            ComposeSupport compose = new ComposeSupport(new ProcessRunner(), ProjectPaths.repoRoot());
            return compose.exec(
                    "dev",
                    List.of("postgres", "psql", "-U", "archforge", "-d", "postgres", "-f", file.toAbsolutePath().toString()));
        }
    }
}
