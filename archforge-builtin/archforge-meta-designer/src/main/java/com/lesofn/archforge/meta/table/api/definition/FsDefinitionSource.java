package com.lesofn.archforge.meta.table.api.definition;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

/** {@link DefinitionSource} backed by a filesystem directory of {@code *.yaml} files. */
public class FsDefinitionSource implements DefinitionSource {

    private final Path dir;

    public FsDefinitionSource(Path dir) {
        this.dir = dir;
    }

    @Override
    public Map<String, String> load() {
        if (!Files.isDirectory(dir)) {
            throw new IllegalStateException("definition directory not found: " + dir);
        }
        Map<String, String> out = new LinkedHashMap<>();
        try (Stream<Path> files = Files.list(dir)) {
            for (Path file : files.filter(f -> f.toString().endsWith(".yaml")).sorted().toList()) {
                String name = java.util.Objects.requireNonNull(file.getFileName()).toString();
                out.put(name, Files.readString(file, StandardCharsets.UTF_8));
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return out;
    }
}
