package com.lesofn.archforge.cli.command;

import com.lesofn.archforge.cli.config.ProjectPaths;
import com.lesofn.archforge.cli.render.TemplateWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.jspecify.annotations.Nullable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

/**
 * Scaffold a new ArchForge-based project: Gradle skeleton + Spring Boot 4 app +
 * contract/AI-rule directories. The generated project depends on the published
 * {@code com.lesofn.archforge:*} release set only — never on this repo's
 * internal {@code project(":…")} coordinates.
 */
@Command(
        mixinStandardHelpOptions = true,
        name = "new",
        description = "Create a new ArchForge-based project skeleton")
public class NewCommand implements Callable<Integer> {

    private static final Pattern NAME_PATTERN = Pattern.compile("[a-z][a-z0-9-]*");
    private static final String TEMPLATE_ROOT = "/templates/new/";

    @Parameters(index = "0", description = "Project name (lowercase, digits, dashes)")
    String name;

    @Option(names = {
            "-d", "--dir"
    }, description = "Parent directory (default: current directory)")
    @Nullable
    Path dir;

    @Option(names = "--group", description = "Maven group for the new project (default: com.example)")
    @Nullable
    String group;

    @Option(names = "--package", description = "Base package (default: <group>.<name without dashes>)")
    @Nullable
    String basePackage;

    @Override
    public Integer call() {
        if (!NAME_PATTERN.matcher(name).matches()) {
            System.err.println("Invalid project name '" + name + "' — use lowercase letters, digits, dashes.");
            return 1;
        }
        String resolvedGroup = group != null ? group : "com.example";
        String resolvedPackage = basePackage != null ? basePackage : resolvedGroup + "." + name.replace("-", "");
        String archforgeVersion = readArchForgeVersion();
        Path target = (dir != null ? dir : Path.of("").toAbsolutePath()).resolve(name).normalize();

        if (Files.exists(target) && !isEmptyDir(target)) {
            System.err.println("Refusing to scaffold into non-empty directory: " + target);
            return 1;
        }

        Map<String, String> placeholders = Map.of(
                "NAME", name,
                "GROUP", resolvedGroup,
                "PACKAGE", resolvedPackage,
                "ARCHFORGE_VERSION", archforgeVersion);
        TemplateWriter writer = new TemplateWriter(placeholders);
        String packagePath = resolvedPackage.replace('.', '/');

        System.out.println("Scaffolding " + name + " → " + target);
        writer.write(TEMPLATE_ROOT + "settings.gradle.kts", target.resolve("settings.gradle.kts"), false);
        writer.write(TEMPLATE_ROOT + "build.gradle.kts", target.resolve("build.gradle.kts"), false);
        writer.write(TEMPLATE_ROOT + "gradle.properties", target.resolve("gradle.properties"), false);
        writer.write(TEMPLATE_ROOT + "gitignore", target.resolve(".gitignore"), false);
        writer.write(
                TEMPLATE_ROOT + "Application.java",
                target.resolve("src/main/java/" + packagePath + "/Application.java"),
                false);
        writer.write(
                TEMPLATE_ROOT + "application.yaml",
                target.resolve("src/main/resources/application.yaml"),
                false);
        writer.write(TEMPLATE_ROOT + "project.yaml", target.resolve("archforge/project.yaml"), false);
        writer.write(TEMPLATE_ROOT + "openapi.yaml", target.resolve("spec/openapi.yaml"), false);
        writer.write(TEMPLATE_ROOT + "AGENTS.md", target.resolve("AGENTS.md"), false);
        writer.write(TEMPLATE_ROOT + "CLAUDE.md", target.resolve("CLAUDE.md"), false);
        writer.write(TEMPLATE_ROOT + "README.md", target.resolve("README.md"), false);
        for (String sub : new String[] {
                "meta", "api", "permissions", "enums"
        }) {
            writeGitkeep(target.resolve("archforge/" + sub));
        }
        writeGitkeep(target.resolve("spec/schemas"));

        copyGradleWrapper(target);

        System.out.println();
        System.out.println("Done. Next:");
        System.out.println("  cd " + name);
        System.out.println("  ./gradlew build");
        System.out.println("  archforge module new <module>   # add a business module");
        return 0;
    }

    private static boolean isEmptyDir(Path dir) {
        try (Stream<Path> entries = Files.list(dir)) {
            return entries.findAny().isEmpty();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static void writeGitkeep(Path dir) {
        try {
            Files.createDirectories(dir);
            Path keep = dir.resolve(".gitkeep");
            if (!Files.exists(keep)) {
                Files.writeString(keep, "");
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * The Gradle wrapper is binary + pinned to the repo's Gradle version — the files
     * are bundled into the cli jar as classpath resources and copied verbatim, so
     * `archforge new` works from any directory (not only inside this repo).
     */
    private static void copyGradleWrapper(Path target) {
        String base = TEMPLATE_ROOT + "wrapper/";
        TemplateWriter writer = new TemplateWriter(Map.of());
        writer.write(base + "gradlew", target.resolve("gradlew"), true);
        writer.write(base + "gradlew.bat", target.resolve("gradlew.bat"), true);
        // .bin extension: shadowJar explodes nested *.jar resources; copy bytes verbatim.
        writer.write(
                base + "gradle-wrapper.jar.bin",
                target.resolve("gradle/wrapper/gradle-wrapper.jar"),
                true);
        writer.write(
                base + "gradle-wrapper.properties",
                target.resolve("gradle/wrapper/gradle-wrapper.properties"),
                true);
        try {
            Files.setPosixFilePermissions(
                    target.resolve("gradlew"), PosixFilePermissions.fromString("rwxr-xr-x"));
        } catch (UnsupportedOperationException | IOException ignored) {
            // non-POSIX filesystem (Windows) — gradlew.bat still works
        }
    }

    /**
     * Release-set version = this cli's own version (built from the same source tree).
     * Read from the jar manifest; when running un-packaged (IDE/dev), fall back to
     * the repo's gradle.properties.
     */
    private static String readArchForgeVersion() {
        String manifest = NewCommand.class.getPackage().getImplementationVersion();
        if (manifest != null && !manifest.isBlank()) {
            return manifest;
        }
        Path props = ProjectPaths.repoRoot().resolve("gradle.properties");
        try {
            for (String line : Files.readAllLines(props)) {
                String trimmed = line.trim();
                if (trimmed.startsWith("archforgeVersion=")) {
                    return trimmed.substring("archforgeVersion=".length()).trim();
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        throw new IllegalStateException("archforgeVersion not found in " + props);
    }
}
