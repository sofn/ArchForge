package com.lesofn.archforge.cli.render;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/**
 * Copies classpath templates to a target directory with {@code %NAME%}-style
 * placeholder substitution. Binary assets (e.g. gradle-wrapper.jar) pass
 * through verbatim. Never overwrites existing files — a scaffold must not
 * clobber user edits on a re-run.
 */
public final class TemplateWriter {

    private final Map<String, String> placeholders;

    public TemplateWriter(Map<String, String> placeholders) {
        this.placeholders = placeholders;
    }

    /** Write one classpath template to {@code target}; returns false when it already exists. */
    public boolean write(String resource, Path target, boolean binary) {
        if (Files.exists(target)) {
            System.out.println("  skip (exists): " + target.getFileName());
            return false;
        }
        try {
            Path parent = target.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            try (InputStream in = TemplateWriter.class.getResourceAsStream(resource)) {
                if (in == null) {
                    throw new IllegalStateException("missing template resource: " + resource);
                }
                byte[] content = in.readAllBytes();
                if (!binary) {
                    String text = new String(content, StandardCharsets.UTF_8);
                    for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                        text = text.replace("%" + entry.getKey() + "%", entry.getValue());
                    }
                    content = text.getBytes(StandardCharsets.UTF_8);
                }
                Files.write(target, content);
            }
            System.out.println("  wrote: " + target);
            return true;
        } catch (IOException e) {
            throw new UncheckedIOException("write template failed: " + target, e);
        }
    }
}
