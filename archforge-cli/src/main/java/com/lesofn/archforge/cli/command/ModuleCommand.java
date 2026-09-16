package com.lesofn.archforge.cli.command;

import com.lesofn.archforge.cli.render.TemplateWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;
import org.jspecify.annotations.Nullable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Spec;

/** Business-module scaffolding: {@code archforge module new <name>}. */
@Command(
        mixinStandardHelpOptions = true,
        name = "module",
        description = "Business module scaffolding (archforge-module-*)",
        subcommands = {
                ModuleCommand.New.class
        })
public class ModuleCommand implements Callable<Integer> {

    @Spec
    CommandSpec spec;

    @Override
    public Integer call() {
        spec.commandLine().usage(spec.commandLine().getOut());
        return 0;
    }

    @Command(
            mixinStandardHelpOptions = true,
            name = "new",
            description = "Create archforge-module-<name> in the current project")
    static class New implements Callable<Integer> {

        private static final Pattern NAME_PATTERN = Pattern.compile("[a-z][a-z0-9-]*");

        @Parameters(index = "0", description = "Module name (lowercase, digits, dashes)")
        String name;

        @Option(
                names = "--package",
                description = "Base package (default: <project group>.<name without dashes>)")
        @Nullable
        String basePackage;

        @Option(names = "-d", description = "Project root (default: nearest ancestor with settings.gradle.kts)")
        @Nullable
        Path dir;

        @Override
        public Integer call() {
            if (!NAME_PATTERN.matcher(name).matches()) {
                System.err.println("Invalid module name '" + name + "' — use lowercase letters, digits, dashes.");
                return 1;
            }
            Path root = dir != null ? dir.toAbsolutePath().normalize() : findProjectRoot();
            if (root == null) {
                System.err.println("No settings.gradle.kts found upward from " + Path.of("").toAbsolutePath() +
                        " — run inside a project or pass -d.");
                return 1;
            }
            String resolvedPackage = basePackage != null ? basePackage : readGroup(root) + "." + name.replace("-", "");
            Path module = root.resolve("archforge-module-" + name);
            if (Files.exists(module)) {
                System.err.println("Module already exists: " + module);
                return 1;
            }

            TemplateWriter writer = new TemplateWriter(Map.of("NAME", name, "PACKAGE", resolvedPackage));
            String packagePath = resolvedPackage.replace('.', '/');
            System.out.println("Scaffolding module " + name + " → " + module);
            writer.write("/templates/module/build.gradle.kts", module.resolve("build.gradle.kts"), false);
            writer.write(
                    "/templates/module/package-info.java",
                    module.resolve("src/main/java/" + packagePath + "/api/package-info.java"),
                    false);
            writer.write(
                    "/templates/module/package-info-internal.java",
                    module.resolve("src/main/java/" + packagePath + "/internal/package-info.java"),
                    false);
            ensureIncluded(root, "archforge-module-" + name);
            System.out.println();
            System.out.println("Done — module is on the build path now.");
            return 0;
        }

        /**
         * Generated projects auto-include via the flat-prefix scan; repos with
         * explicit includes (like ArchForge itself) need an include() line appended.
         */
        private static void ensureIncluded(Path root, String moduleName) {
            Path settings = root.resolve("settings.gradle.kts");
            try {
                String text = Files.exists(settings) ? Files.readString(settings) : "";
                if (text.contains("archforge-module-") && text.contains("listFiles")) {
                    return; // flat-prefix scan already covers it
                }
                if (text.contains("\"" + moduleName + "\"")) {
                    return; // already included
                }
                Files.writeString(
                        settings,
                        text + (text.endsWith("\n") ? "" : "\n") + "include(\"" + moduleName + "\")\n");
                System.out.println("  appended include(\"" + moduleName + "\") to settings.gradle.kts");
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        /** Walk up from cwd until a settings.gradle.kts is found (generated or ArchForge repo). */
        private static @Nullable Path findProjectRoot() {
            Path probe = Path.of("").toAbsolutePath().normalize();
            for (int i = 0; i < 8; i++) {
                if (Files.exists(probe.resolve("settings.gradle.kts"))) {
                    return probe;
                }
                Path parent = probe.getParent();
                if (parent == null) {
                    return null;
                }
                probe = parent;
            }
            return null;
        }

        private static String readGroup(Path root) {
            Path buildFile = root.resolve("build.gradle.kts");
            try {
                if (Files.exists(buildFile)) {
                    for (String line : Files.readAllLines(buildFile)) {
                        String trimmed = line.trim();
                        if (trimmed.startsWith("group =")) {
                            String value = trimmed.substring("group =".length()).trim();
                            return value.replace("\"", "");
                        }
                    }
                }
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
            return "com.example";
        }
    }
}
