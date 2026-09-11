package com.lesofn.archforge.common.persistence;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.configuration.FluentConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * Flyway 数据库迁移配置（server-admin 与 server-web 共用）。
 *
 * <p>
 * 项目使用 dynamic-datasource，且 Spring Boot 4 不再提供 Flyway 自动配置，此处手动创建 Flyway 实例，通过
 * {@link GroupDataSourceProxy} 路由到 user 数据源组。
 *
 * <p>
 * 所有行为由 {@code arch-forge.flyway.*}（{@link FlywayProperties}）控制：
 *
 * <ul>
 * <li>{@code enabled=true} 的 profile（dev/test/staging/prod）启动即执行 classpath:db/migration 迁移；
 * <li>JPA 侧一律 {@code ddl-auto: validate}——schema 归 Flyway 独占管理；
 * <li>server-web 与 server-admin 共享同一 archforge_user 库与 migration 目录，并发迁移由 flyway_schema_history
 * 锁串行化，天然安全。
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

    private final DataSource dataSource;
    private final FlywayProperties properties;

    @Bean(initMethod = "migrate")
    public Flyway flyway() {
        log.info("Flyway: 开始数据库迁移...");
        FluentConfiguration fluent = Flyway.configure()
                .dataSource(new GroupDataSourceProxy(dataSource, "user"))
                .locations(properties.getLocations().toArray(new String[0]))
                .baselineOnMigrate(properties.isBaselineOnMigrate())
                .baselineVersion(properties.getBaselineVersion())
                .encoding(properties.getEncoding())
                .validateOnMigrate(properties.isValidateOnMigrate())
                .outOfOrder(properties.isOutOfOrder());
        if (properties.getDefaultSchema() != null) {
            fluent.defaultSchema(properties.getDefaultSchema());
        }
        if (!properties.getIgnoreMigrationPatterns().isEmpty()) {
            fluent.ignoreMigrationPatterns(
                    properties.getIgnoreMigrationPatterns().toArray(new String[0]));
        }
        Flyway flyway = fluent.load();
        log.info("Flyway: 数据库迁移完成");
        return flyway;
    }
}
