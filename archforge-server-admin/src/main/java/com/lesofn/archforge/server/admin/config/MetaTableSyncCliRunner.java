package com.lesofn.archforge.server.admin.config;

import com.lesofn.archforge.meta.table.api.service.MetaTableDefinitionService;
import com.lesofn.archforge.meta.table.api.service.MetaTableDefinitionService.SyncReport;
import java.nio.file.Path;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Meta-table definition sync CLI entry — DB ↔ {@code archforge/meta/*.yaml}.
 * Activated by {@code --arch-forge.meta.sync.mode=export|import} plus
 * {@code .dir} (default {@code archforge/meta}), {@code .table} (optional
 * single-table filter) and {@code .apply} (import actually writes; default is
 * a dry-run diff report). One-shot process: exits via {@code System.exit}
 * after running, so later unordered runners never see a closed context.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "arch-forge.meta.sync.mode")
// Last in callRunners — the context is closed inside run(), so every other
// runner must have finished on a live context first.
@Order(Ordered.LOWEST_PRECEDENCE)
public class MetaTableSyncCliRunner implements CommandLineRunner {

    private final ApplicationContext applicationContext;
    private final Environment environment;
    private final MetaTableDefinitionService definitionService;

    @Override
    public void run(String... args) {
        String mode = environment.getRequiredProperty("arch-forge.meta.sync.mode");
        String dir = environment.getProperty("arch-forge.meta.sync.dir", "archforge/meta");
        String table = environment.getProperty("arch-forge.meta.sync.table");
        boolean apply = Boolean.parseBoolean(environment.getProperty("arch-forge.meta.sync.apply", "false"));

        switch (mode) {
            case "export" -> {
                var written = definitionService.exportTo(Path.of(dir), table);
                log.info("meta export: {} definition file(s) under {}", written.size(), dir);
            }
            case "import" -> {
                SyncReport report = definitionService.syncFrom(Path.of(dir), table, apply);
                report.lines().forEach(line -> log.info("  {}", line));
                log.info("meta import {}: {}", apply ? "applied" : "dry-run",
                        report.isEmpty() ? "no changes" : report.lines().size() + " change line(s)");
            }
            default -> throw new IllegalArgumentException("unknown arch-forge.meta.sync.mode=" + mode + " (export|import)");
        }

        // One-shot process — exit immediately rather than returning into
        // callRunners: closing the context first makes every subsequent
        // unordered runner hit a dead datasource. System.exit is safe here
        // because this bean only exists under the explicit sync property.
        System.exit(SpringApplication.exit(applicationContext, () -> 0));
    }
}
