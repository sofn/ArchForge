package com.lesofn.archforge.cli.command;

import com.lesofn.archforge.cli.config.DbPasswordResolver;
import com.lesofn.archforge.cli.config.ProjectPaths;
import com.lesofn.archforge.cli.proc.ProcessRunner;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import org.jspecify.annotations.Nullable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Spec;

/**
 * Meta-table definition sync: {@code archforge meta export|import}. Wraps the
 * server-admin {@code MetaTableSyncCliRunner} via bootRun args — the DB access
 * stays inside the Spring context (datasource config, transactions) instead of
 * duplicating it in the cli.
 */
@Command(
        mixinStandardHelpOptions = true,
        name = "meta",
        description = "Meta-table definition sync (definition YAML <-> DB)",
        subcommands = {
                MetaCommand.Export.class,
                MetaCommand.Import.class,
                MetaCommand.Check.class
        })
public class MetaCommand implements Callable<Integer> {

    @Spec
    CommandSpec spec;

    @Override
    public Integer call() {
        spec.commandLine().usage(spec.commandLine().getOut());
        return 0;
    }

    static class SyncOptions {
        @Option(names = {
                "-d", "--dir"
        }, description = "Definition directory (default: archforge/meta in generated projects," +
                " project-definition/meta in the ArchForge repo)")
        @Nullable
        String dir;

        @Option(names = "--table", description = "Single tableCode (default: all enabled tables)")
        @Nullable
        String table;
    }

    @Command(
            mixinStandardHelpOptions = true,
            name = "export",
            description = "Export meta-table definitions from DB to YAML files")
    static class Export implements Callable<Integer> {

        @picocli.CommandLine.Mixin
        SyncOptions options = new SyncOptions();

        @Override
        public Integer call() {
            return runSync("export", options, false);
        }
    }

    @Command(
            mixinStandardHelpOptions = true,
            name = "import",
            description = "Import definition YAML files into DB (dry-run unless --apply)")
    static class Import implements Callable<Integer> {

        @picocli.CommandLine.Mixin
        SyncOptions options = new SyncOptions();

        @Option(names = "--apply", description = "Write changes to the DB (default: dry-run diff report)")
        boolean apply;

        @Override
        public Integer call() {
            return runSync("import", options, apply);
        }
    }

    @Command(
            mixinStandardHelpOptions = true,
            name = "check",
            description = "Compare definition YAML files against the DB (exit 1 on drift)")
    static class Check implements Callable<Integer> {

        @picocli.CommandLine.Mixin
        SyncOptions options = new SyncOptions();

        @Override
        public Integer call() {
            return runSync("check", options, false);
        }
    }

    private static int runSync(String mode, SyncOptions options, boolean apply) {
        Path root = ProjectPaths.repoRoot();
        // bootRun's cwd is the module dir — resolve the definition dir against
        // the project root. Generated projects use archforge/meta (scaffolded
        // placeholder); this repo can't — its root `archforge` is a script
        // file, so the dogfood dir is project-definition/meta instead.
        String defaultDir = Files.isDirectory(root.resolve("archforge")) ? "archforge/meta" : "project-definition/meta";
        Path dir = root.resolve(options.dir != null ? options.dir : defaultDir).normalize();

        // The sync runner lives in server-admin (designer module on classpath).
        // Generated projects without the designer artifact get a clear refusal.
        if (!Files.exists(root.resolve("archforge-server-admin"))) {
            System.err.println("meta sync requires the designer module on the app classpath" +
                    " — available in the ArchForge repo, not in generated projects yet.");
            return 1;
        }

        List<String> args = new ArrayList<>(List.of(
                "--spring.main.web-application-type=none",
                "--arch-forge.meta.sync.mode=" + mode,
                "--arch-forge.meta.sync.dir=" + dir));
        if (options.table != null) {
            args.add("--arch-forge.meta.sync.table=" + options.table);
        }
        if (apply) {
            args.add("--arch-forge.meta.sync.apply=true");
        }

        // bootRun connects to the dev datasource — resolve DB_PASSWORD the same
        // way `db init` does (env → .env).
        DbPasswordResolver.Result password = DbPasswordResolver.resolve(root, null);
        return new ProcessRunner()
                .run(
                        List.of(
                                "./gradlew", ":archforge-server-admin:bootRun",
                                "--args=" + String.join(" ", args)),
                        root,
                        Map.of("DB_PASSWORD", password.value()),
                        true);
    }
}
