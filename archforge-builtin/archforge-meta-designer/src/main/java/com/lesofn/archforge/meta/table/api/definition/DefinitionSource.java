package com.lesofn.archforge.meta.table.api.definition;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.jspecify.annotations.Nullable;

/**
 * Source of meta-table definition YAML documents — file name (e.g.
 * {@code blog_category.yaml}) mapped to raw YAML text. Implementations read
 * from a filesystem directory or from classpath resources; the codec turns the
 * text into {@link TableDefinition}s.
 */
public interface DefinitionSource {

    /** Classpath base of the definition mirror packaged into application jars. */
    String CLASSPATH_BASE = "archforge/meta";

    /**
     * All {@code *.yaml} documents this source exposes, keyed by file name.
     * Order is sorted by file name so downstream processing is deterministic.
     */
    Map<String, String> load();

    /**
     * Runtime resolution — filesystem first, packaged classpath mirror as
     * fallback. An explicit {@code configuredDir} wins outright (null when it
     * does not exist — a mistyped dir must not silently fall back); otherwise
     * walk up from {@code start} (bootRun's cwd is the module dir) looking for
     * {@code project-definition/meta} (this repo) or {@code archforge/meta}
     * (generated projects); otherwise {@code classpath*:archforge/meta/*.yaml}.
     * Returns null when nothing is found.
     */
    static @Nullable DefinitionSource discover(@Nullable String configuredDir, Path start, ClassLoader classLoader) {
        if (configuredDir != null) {
            Path dir = Path.of(configuredDir).toAbsolutePath().normalize();
            return Files.isDirectory(dir) ? new FsDefinitionSource(dir) : null;
        }
        Path cursor = start.toAbsolutePath();
        for (int i = 0; cursor != null && i < 6; i++, cursor = cursor.getParent()) {
            for (String candidate : List.of("project-definition/meta", "archforge/meta")) {
                Path dir = cursor.resolve(candidate);
                if (Files.isDirectory(dir)) {
                    return new FsDefinitionSource(dir);
                }
            }
        }
        ClasspathDefinitionSource packaged = new ClasspathDefinitionSource(CLASSPATH_BASE, classLoader);
        return packaged.load().isEmpty() ? null : packaged;
    }
}
