package com.lesofn.archforge.server.admin.config;

import jakarta.annotation.Nullable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.stereotype.Component;

/**
 * Resolves the workspace root for meta-table code generation.
 *
 * <p>
 * The workspace is expected to contain both the backend project ({@code ArchForge})
 * and the frontend project ({@code ArchForgeAdmin}). If the current working directory
 * is inside either project (or one of their subdirectories), walking up the tree will
 * find the workspace root.
 */
@Component
public class CodeGenWorkspaceResolver {

    private static final String BACKEND_DIR = "ArchForge";
    private static final String FRONTEND_DIR = "ArchForgeAdmin";

    /**
     * Returns the workspace root, or the current working directory if no workspace root
     * can be determined.
     */
    public Path resolve() {
        Path start = Paths.get(System.getProperty("user.dir")).toAbsolutePath().normalize();
        Path current = start;
        while (current != null) {
            if (isWorkspaceRoot(current)) {
                return current;
            }
            current = current.getParent();
        }
        return start;
    }

    private boolean isWorkspaceRoot(Path dir) {
        return Files.isDirectory(dir.resolve(BACKEND_DIR)) && Files.isDirectory(dir.resolve(FRONTEND_DIR));
    }

    /** Default backend output directory relative to the workspace root. */
    public Path defaultBackendDir(String tableCode) {
        return Paths.get(BACKEND_DIR, "example", tableCode);
    }

    /** Default frontend output directory relative to the workspace root. */
    public Path defaultFrontendDir(String tableCode) {
        return Paths.get(FRONTEND_DIR, "src", "views", tableCode);
    }

    /** Resolve a raw backend dir string; {@code null}/blank returns the default. */
    public Path resolveBackendDir(@Nullable String backendDir, String tableCode) {
        if (backendDir == null || backendDir.isBlank()) {
            return defaultBackendDir(tableCode);
        }
        return Paths.get(backendDir);
    }

    /** Resolve a raw frontend dir string; {@code null}/blank returns the default. */
    public Path resolveFrontendDir(@Nullable String frontendDir, String tableCode) {
        if (frontendDir == null || frontendDir.isBlank()) {
            return defaultFrontendDir(tableCode);
        }
        return Paths.get(frontendDir);
    }
}
