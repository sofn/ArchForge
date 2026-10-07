package com.lesofn.archforge.server.admin.config;

import com.lesofn.archforge.meta.table.api.definition.DefinitionSource;
import com.lesofn.archforge.meta.table.api.service.MetaTableDefinitionService;
import com.lesofn.archforge.meta.table.api.service.MetaTableDefinitionService.SyncReport;
import java.nio.file.Path;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
 * never writes, never blocks boot. The definition source is resolved like the
 * file-mode applier ({@link DefinitionSource#discover}: configured
 * {@code arch-forge.meta.definition-dir}, walk-up discovery, packaged
 * classpath mirror). Nothing found is an INFO skip.
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
        DefinitionSource source = DefinitionSource.discover(environment.getProperty("arch-forge.meta.definition-dir"),
                Path.of(""), getClass().getClassLoader());
        if (source == null) {
            log.info("meta shadow: no definition source found — skipped");
            return;
        }
        SyncReport report = definitionService.syncFrom(source, null, false);
        if (report.isEmpty()) {
            log.info("meta shadow: {} is in sync with DB", source);
            // "in sync" says nothing about the physical tables — surface registered-but-missing ones
            if (report.hasMissingPhysicalTables()) {
                report.lines().forEach(line -> log.warn("  meta shadow: {}", line));
            }
            return;
        }
        log.warn("meta shadow: {} drift line(s) between {} and DB", report.lines().size(), source);
        report.lines().forEach(line -> log.warn("  meta drift: {}", line));
    }
}
