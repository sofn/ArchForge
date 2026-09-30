package com.lesofn.archforge.server.admin.config;

import com.lesofn.archforge.meta.table.api.definition.DefinitionSource;
import com.lesofn.archforge.meta.table.api.service.MetaTableDefinitionService;
import com.lesofn.archforge.meta.table.api.service.MetaTableDefinitionService.SyncReport;
import java.nio.file.Path;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * F2 startup apply (P3-4-3): under {@code arch-forge.meta.source=file} the
 * definition YAML is the source of truth — materialize it into the DB mirror
 * before any business runner. Fail-fast: no definitions found, an unparseable
 * or invalid file, or an apply-lock timeout
 * ({@code arch-forge.meta.apply-lock-timeout}, default 60s) aborts startup
 * with the sync rolled back. Rollback of the mode itself: {@code source=db}.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "arch-forge.meta.source", havingValue = "file")
// After ProductionSecretsInitializer (0), before the unordered business runners.
@Order(1)
public class MetaTableFileSourceApplier implements ApplicationRunner {

    private final Environment environment;
    private final MetaTableDefinitionService definitionService;

    @Override
    public void run(ApplicationArguments args) {
        String configuredDir = environment.getProperty("arch-forge.meta.definition-dir");
        DefinitionSource source = DefinitionSource.discover(configuredDir, Path.of(""), getClass().getClassLoader());
        if (source == null) {
            throw new IllegalStateException("arch-forge.meta.source=file but no definition files found (" +
                    (configuredDir != null
                            ? "arch-forge.meta.definition-dir=" + configuredDir + " does not exist"
                            : "no project-definition/meta or archforge/meta dir, no classpath:" +
                                    DefinitionSource.CLASSPATH_BASE + "/*.yaml") + ")");
        }
        Duration lockTimeout = environment.getProperty("arch-forge.meta.apply-lock-timeout", Duration.class,
                Duration.ofSeconds(60));
        SyncReport report = definitionService.materialize(source, lockTimeout);
        for (String line : report.lines()) {
            if (line.startsWith("!")) {
                log.warn("  meta file-source: {}", line);
            } else {
                log.info("  meta file-source: {}", line);
            }
        }
        log.info("meta file-source: {} materialized — {}", source,
                report.isEmpty() ? "DB mirror already in sync" : report.lines().size() + " line(s)");
    }
}
