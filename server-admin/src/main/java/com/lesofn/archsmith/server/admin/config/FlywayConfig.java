package com.lesofn.archsmith.server.admin.config;

import com.lesofn.archsmith.infrastructure.frame.database.GroupDataSourceProxy;
import jakarta.annotation.Resource;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Flyway 数据库迁移配置
 *
 * <p>因为项目使用 dynamic-datasource，Spring Boot 无法自动配置 Flyway。 此处手动创建 Flyway 实例，通过 GroupDataSourceProxy
 * 路由到 user 数据源组。
 *
 * <p>配置方式：
 *
 * <ul>
 *   <li>dev 环境：Flyway 禁用，使用 Hibernate DDL auto + InitDbMockServer
 *   <li>test/prod 环境：Flyway 启用，自动执行 db/migration 下的 SQL
 * </ul>
 *
 * @author sofn
 */
@Slf4j
@Configuration
@ConditionalOnProperty(name = "arch-smith.flyway.enabled", havingValue = "true")
public class FlywayConfig {

    @Resource private DataSource dataSource;

    @Bean(initMethod = "migrate")
    public Flyway flyway() {
        log.info("Flyway: 开始数据库迁移...");
        Flyway flyway =
                Flyway.configure()
                        .dataSource(new GroupDataSourceProxy(dataSource, "user"))
                        .locations("classpath:db/migration")
                        .baselineOnMigrate(true)
                        .baselineVersion("0")
                        .encoding("UTF-8")
                        .validateOnMigrate(true)
                        .outOfOrder(false)
                        .load();
        log.info("Flyway: 数据库迁移完成");
        return flyway;
    }
}
