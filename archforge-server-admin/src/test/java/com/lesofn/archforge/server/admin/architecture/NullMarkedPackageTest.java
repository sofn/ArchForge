package com.lesofn.archforge.server.admin.architecture;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

class NullMarkedPackageTest {

    @Test
    void everyMainJavaPackageDeclaresNullMarked() throws IOException {
        Path repoRoot = findRepoRoot();
        List<Path> missing = new ArrayList<>();
        try (Stream<Path> javaRoots = Files.walk(repoRoot, 8)) {
            javaRoots.filter(path -> path.endsWith(Path.of("src", "main", "java")))
                    .filter(Files::isDirectory)
                    .forEach(javaRoot -> collectMissing(javaRoot, missing));
        }
        assertTrue(missing.isEmpty(), () -> "Packages without @NullMarked package-info.java (" + missing.size() + "):\n" +
                missing.stream().map(Path::toString).reduce("", (a, b) -> a + "\n" + b));
    }

    private static Path findRepoRoot() {
        Path current = Path.of("").toAbsolutePath().normalize();
        for (Path path = current; path != null; path = path.getParent()) {
            if (Files.isRegularFile(path.resolve("settings.gradle.kts")) && Files.isDirectory(path.resolve(
                    "archforge-server-admin"))) {
                return path;
            }
        }
        throw new IllegalStateException("Cannot locate ArchForge repo root from " + current);
    }

    private static void collectMissing(Path javaRoot, List<Path> missing) {
        try (Stream<Path> files = Files.walk(javaRoot)) {
            files.filter(path -> path.toString().endsWith(".java"))
                    .map(Path::getParent)
                    .distinct()
                    .forEach(pkgDir -> {
                        Path info = pkgDir.resolve("package-info.java");
                        if (!Files.isRegularFile(info)) {
                            missing.add(javaRoot.relativize(pkgDir));
                            return;
                        }
                        try {
                            String source = Files.readString(info);
                            if (!source.contains("@NullMarked") && !source.contains("org.jspecify.annotations.NullMarked")) {
                                missing.add(javaRoot.relativize(pkgDir));
                            }
                        } catch (IOException e) {
                            throw new IllegalStateException(e);
                        }
                    });
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
