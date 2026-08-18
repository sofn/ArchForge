package com.lesofn.archforge.cli.config;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Resolves ArchForge repository root and sibling frontend repos.
 */
public final class ProjectPaths {

    private ProjectPaths() {
    }

    public static Path repoRoot() {
        Path current = Paths.get("").toAbsolutePath().normalize();
        Path probe = current;
        for (int i = 0; i < 6; i++) {
            if (Files.exists(probe.resolve("settings.gradle.kts")) && Files.exists(probe.resolve("archforge-server-admin"))) {
                return probe;
            }
            Path parent = probe.getParent();
            if (parent == null) {
                break;
            }
            probe = parent;
        }
        return current;
    }

    public static Path dockerDir(Path root) {
        return root.resolve("docker");
    }

    public static Path envFile(Path root) {
        return root.resolve(".env");
    }

    public static Path backupDir(Path root) {
        return root.resolve("backup").resolve("db");
    }

    public static Path adminRepo(Path root) {
        return root.getParent().resolve("ArchForgeAdmin");
    }

    public static Path webRepo(Path root) {
        return root.getParent().resolve("ArchForgeWeb");
    }
}
