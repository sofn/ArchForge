package com.lesofn.archforge.common.persistence;

import java.io.IOException;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.configuration.FluentConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

/**
 * Flyway 数据库迁移配置（server-admin 与 server-web 共用）。
 *
 * <p>
 * 项目使用 dynamic-datasource，且 Spring Boot 4 不再提供 Flyway 自动配置，此处手动创建迁移编排器，
 * 直接绑定 primary 路由数据源。
 *
 * <p>
 * <b>模块感知迁移</b>：classpath 上的 {@code db/migration/<module>/*.sql} 按目录名分治——
 *
 * <ul>
 * <li>{@code db/migration/__root/} 是共享存量序列，写入默认 {@code flyway_schema_history}，最先执行；
 * <li>其余每个 {@code db/migration/<module>/} 目录获得独立 Flyway 实例与独立历史表
 * {@code flyway_schema_history_<module>}，模块内版本号自增，按模块名排序依次执行；
 * <li>{@code archforge-module-*} 的 jar 只需内置 {@code db/migration/<module>/V1__*.sql} 即被接管。
 * </ul>
 *
 * <p>
 * 所有行为由 {@code arch-forge.flyway.*}（{@link FlywayProperties}）控制：
 *
 * <ul>
 * <li>{@code enabled=true} 的 profile（dev/test/staging/prod）启动即执行迁移；
 * <li>JPA 侧一律 {@code ddl-auto: validate}——schema 归 Flyway 独占管理；
 * <li>server-web 与 server-admin 共享同一 archforge 库与历史表，并发迁移由历史表锁串行化，天然安全。
 * </ul>
 *
 * @author sofn
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(FlywayProperties.class)
@ConditionalOnProperty(name = "arch-forge.flyway.enabled", havingValue = "true")
public class FlywayConfig {

    /** 共享存量序列目录：写入默认 flyway_schema_history，最先执行。 */
    static final String ROOT_MODULE = "__root";

    private static final String MIGRATION_ROOT = "db/migration";
    private static final String HISTORY_TABLE_PREFIX = "flyway_schema_history_";

    private final DataSource dataSource;
    private final FlywayProperties properties;

    @Bean(initMethod = "migrate")
    public FlywayModuleOrchestrator flyway() {
        return new FlywayModuleOrchestrator(dataSource, properties);
    }

    /**
     * 先跑 __root（默认历史表），再逐模块跑 db/migration/&lt;module&gt;/（独立历史表）。作为名为
     * {@code flyway} 的 bean 暴露——{@link FlywayDependencyBeanFactoryPostProcessor} 借此把 EMF 装配
     * 排在全部迁移之后。
     */
    @RequiredArgsConstructor
    public static class FlywayModuleOrchestrator {

        private final DataSource dataSource;
        private final FlywayProperties properties;

        public void migrate() {
            FluentConfiguration base = Flyway.configure()
                    .dataSource(dataSource)
                    .baselineOnMigrate(properties.isBaselineOnMigrate())
                    .baselineVersion(properties.getBaselineVersion())
                    .encoding(properties.getEncoding())
                    .validateOnMigrate(properties.isValidateOnMigrate())
                    .outOfOrder(properties.isOutOfOrder());
            if (properties.getDefaultSchema() != null) {
                base.defaultSchema(properties.getDefaultSchema());
            }
            if (!properties.getIgnoreMigrationPatterns().isEmpty()) {
                base.ignoreMigrationPatterns(
                        properties.getIgnoreMigrationPatterns().toArray(new String[0]));
            }

            log.info("Flyway: 开始 __root 迁移...");
            base.locations(properties.getLocations().toArray(new String[0]))
                    .load()
                    .migrate();
            log.info("Flyway: __root 迁移完成");

            for (String module : discoverModules()) {
                String table = HISTORY_TABLE_PREFIX + module.replace('-', '_');
                log.info("Flyway: 开始模块 {} 迁移（历史表 {}）...", module, table);
                base.locations("classpath:" + MIGRATION_ROOT + "/" + module)
                        .table(table)
                        .load()
                        .migrate();
                log.info("Flyway: 模块 {} 迁移完成", module);
            }
        }

        /** 发现 classpath 上所有 {@code db/migration/<module>/} 子目录（排除 __root），按名排序。 */
        static SortedSet<String> discoverModules() {
            SortedSet<String> modules = new TreeSet<>();
            try {
                Resource[] scripts = new PathMatchingResourcePatternResolver()
                        .getResources("classpath*:" + MIGRATION_ROOT + "/*/*.sql");
                for (Resource script : scripts) {
                    String path = script.getURI().toString();
                    int idx = path.indexOf(MIGRATION_ROOT + "/");
                    if (idx < 0) {
                        continue;
                    }
                    String rest = path.substring(idx + MIGRATION_ROOT.length() + 1);
                    String module = rest.substring(0, rest.indexOf('/'));
                    if (!ROOT_MODULE.equals(module)) {
                        modules.add(module);
                    }
                }
            } catch (IOException e) {
                throw new IllegalStateException("Flyway: 扫描模块迁移目录失败", e);
            }
            return modules;
        }
    }
}
