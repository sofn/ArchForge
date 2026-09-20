package com.lesofn.archforge.server.admin.config;

import com.lesofn.archforge.meta.table.api.definition.FsDefinitionSource;
import com.lesofn.archforge.meta.table.api.service.MetaTableDefinitionService;
import com.lesofn.archforge.meta.table.api.service.MetaTableDefinitionService.SyncReport;
import java.nio.file.Files;
import java.nio.file.Path;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * F1 shadow read (P3-4-3): when {@code arch-forge.meta.source=shadow}, compares
 * the YAML definition files against the DB at startup and logs every drift —
 * never writes, never blocks boot. Resolution for the definition dir:
 * {@code arch-forge.meta.definition-dir} if set, else walk up from the working
 * directory looking for {@code project-definition/meta} or
 * {@code archforge/meta}. A missing dir is an INFO skip (dev machines may not
 * have files checked out).
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "arch-forge.meta.source", havingValue = "shadow")
// Runs last in callRunners — the comparison must see Flyway/seed output.
@Order(Ordered.LOWEST_PRECEDENCE)
public class MetaTableShadowVerifier implements ApplicationRunner {

    private final Environment environment;
    private final MetaTableDefinitionService definitionService;

    @Override
    public void run(ApplicationArguments args) {
        try {
            verify();
        } catch (Exception e) {
            // Observation-only component — its own failure must not block boot.
            log.warn("meta shadow verify failed (ignored): {}", e.toString());
        }
    }

    private void verify() {
        Path dir = resolveDir();
        if (dir == null) {
            log.info("meta shadow: no definition dir found — skipped");
            return;
        }
        SyncReport report = definitionService.syncFrom(new FsDefinitionSource(dir), null, false);
        if (report.isEmpty()) {
            log.info("meta shadow: {} is in sync with DB", dir);
            return;
        }
        log.warn("meta shadow: {} drift line(s) between {} and DB", report.lines().size(), dir);
        report.lines().forEach(line -> log.warn("  meta drift: {}", line));
    }

    @Nullable
    private Path resolveDir() {
        String configured = environment.getProperty("arch-forge.meta.definition-dir");
        if (configured != null) {
            Path dir = Path.of(configured).toAbsolutePath().normalize();
            return Files.isDirectory(dir) ? dir : null;
        }
        // bootRun's cwd is the module dir — walk up until a definition dir appears.
        Path cursor = Path.of("").toAbsolutePath();
        for (int i = 0; cursor != null && i < 6; i++, cursor = cursor.getParent()) {
            for (String candidate : new String[] {
                    "project-definition/meta", "archforge/meta"
            }) {
                Path dir = cursor.resolve(candidate);
                if (Files.isDirectory(dir)) {
                    return dir;
                }
            }
        }
        return null;
    }
}
