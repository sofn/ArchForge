package com.lesofn.archforge.meta.table.api.definition;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Classpath/FS enumeration contract + runtime discovery order (FS dir → walk-up → classpath). */
class DefinitionSourceTest {

    @TempDir
    Path tmp;

    @Test
    void classpathSourceIsSortedByFileName() throws IOException {
        Path first = root("first", "b.yaml");
        Path second = root("second", "a.yaml");
        try (URLClassLoader loader = loader(first, second)) {
            ClasspathDefinitionSource source = new ClasspathDefinitionSource("archforge/meta", loader);
            assertEquals(List.of("a.yaml", "b.yaml"), List.copyOf(source.load().keySet()));
            assertEquals("tableCode: a.yaml\n", source.load().get("a.yaml"));
        }
    }

    @Test
    void classpathSourceRejectsDuplicateFileNames() throws IOException {
        Path first = root("first", "x.yaml");
        Path second = root("second", "x.yaml");
        try (URLClassLoader loader = loader(first, second)) {
            ClasspathDefinitionSource source = new ClasspathDefinitionSource("archforge/meta", loader);
            IllegalStateException e = assertThrows(IllegalStateException.class, source::load);
            String message = String.valueOf(e.getMessage());
            assertTrue(message.contains("x.yaml"), message);
        }
    }

    @Test
    void discoverUsesConfiguredDirWhenItExists() throws IOException {
        Path dir = Files.createDirectories(tmp.resolve("defs"));
        try (URLClassLoader loader = loader()) {
            DefinitionSource source = Objects.requireNonNull(
                    DefinitionSource.discover(dir.toString(), tmp, loader));
            assertInstanceOf(FsDefinitionSource.class, source);
            assertEquals(dir.toString(), source.toString());
        }
    }

    @Test
    void discoverReturnsNullForMissingConfiguredDirEvenWithClasspathDefinitions() throws IOException {
        Path packaged = root("packaged", "a.yaml");
        try (URLClassLoader loader = loader(packaged)) {
            assertNull(DefinitionSource.discover(tmp.resolve("absent").toString(), tmp, loader));
        }
    }

    @Test
    void discoverWalksUpToProjectDefinitionDir() throws IOException {
        Path dir = Files.createDirectories(tmp.resolve("repo/project-definition/meta"));
        Path start = Files.createDirectories(tmp.resolve("repo/module/sub"));
        try (URLClassLoader loader = loader()) {
            DefinitionSource source = Objects.requireNonNull(DefinitionSource.discover(null, start, loader));
            assertEquals(dir.toString(), source.toString());
        }
    }

    @Test
    void discoverFallsBackToPackagedClasspathMirror() throws IOException {
        Path packaged = root("packaged", "a.yaml");
        Path start = Files.createDirectories(tmp.resolve("cwd"));
        try (URLClassLoader loader = loader(packaged)) {
            DefinitionSource source = Objects.requireNonNull(DefinitionSource.discover(null, start, loader));
            assertInstanceOf(ClasspathDefinitionSource.class, source);
            assertEquals(List.of("a.yaml"), List.copyOf(source.load().keySet()));
        }
    }

    @Test
    void discoverReturnsNullWhenNothingIsFound() throws IOException {
        Path start = Files.createDirectories(tmp.resolve("cwd"));
        try (URLClassLoader loader = loader()) {
            assertNull(DefinitionSource.discover(null, start, loader));
        }
    }

    /** A classpath root holding {@code archforge/meta/<file>}. */
    private Path root(String name, String file) throws IOException {
        Path dir = Files.createDirectories(tmp.resolve(name).resolve("archforge/meta"));
        Files.writeString(dir.resolve(file), "tableCode: " + file + "\n");
        return tmp.resolve(name);
    }

    /** Isolated loader (no parent) so the test JVM's own resources never leak in. */
    private static URLClassLoader loader(Path... roots) throws IOException {
        URL[] urls = new URL[roots.length];
        for (int i = 0; i < roots.length; i++) {
            urls[i] = roots[i].toUri().toURL();
        }
        return new URLClassLoader(urls, null);
    }
}
