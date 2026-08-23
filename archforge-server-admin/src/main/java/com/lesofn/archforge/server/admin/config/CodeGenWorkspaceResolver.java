package com.lesofn.archforge.server.admin.config;

import com.lesofn.archforge.common.error.system.SystemException;
import com.lesofn.archforge.infrastructure.config.ArchForgeProperties;
import jakarta.annotation.Nullable;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.stereotype.Component;

/**
 * Resolves the workspace root for meta-table code generation.
 *
 * <p>
 * The workspace root comes from the {@code arch-forge.codegen.workspace-root} property and acts as
 * a security boundary: every request-supplied output directory is normalized, absolutized and
 * required to stay inside it.
 */
@Component
public class CodeGenWorkspaceResolver {

    private static final String BACKEND_DIR = "ArchForge";
    private static final String FRONTEND_DIR = "ArchForgeAdmin";

    private final Path workspaceRoot;

    public CodeGenWorkspaceResolver(ArchForgeProperties properties) {
        this.workspaceRoot = Paths.get(properties.getCodeGen().getWorkspaceRoot()).toAbsolutePath().normalize();
    }

    /**
     * Returns the configured workspace root, creating it on demand.
     */
    public Path resolve() {
        try {
            Files.createDirectories(workspaceRoot);
        } catch (IOException e) {
            throw new UncheckedIOException("无法创建代码生成工作区目录: " + workspaceRoot, e);
        }
        return workspaceRoot;
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
        return confine(backendDir);
    }

    /** Resolve a raw frontend dir string; {@code null}/blank returns the default. */
    public Path resolveFrontendDir(@Nullable String frontendDir, String tableCode) {
        if (frontendDir == null || frontendDir.isBlank()) {
            return defaultFrontendDir(tableCode);
        }
        return confine(frontendDir);
    }

    private Path confine(String rawDir) {
        Path target = Paths.get(rawDir).toAbsolutePath().normalize();
        if (!target.startsWith(workspaceRoot)) {
            throw new SystemException("代码生成目录必须位于工作区根目录内: " + target);
        }
        return target;
    }
}
