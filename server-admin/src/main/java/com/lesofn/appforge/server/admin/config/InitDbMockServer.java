package com.lesofn.appforge.server.admin.config;

import com.lesofn.appforge.infrastructure.frame.database.GroupDataSourceProxy;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Component;

/**
 * @author sofn
 * @version 1.0 Created at: 2025-08-25 23:33
 */
@Slf4j
@Component
@Profile("dev")
@ConditionalOnProperty(name = "app-forge.embedded.h2-init", havingValue = "true")
public class InitDbMockServer {

    @Resource private DataSource dataSource;

    @PostConstruct
    public void init() {
        try {
            log.info("开始初始化数据库数据...");

            ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
            populator.addScript(new ClassPathResource("sql/data-admin-user.sql"));
            populator.addScript(new ClassPathResource("sql/data-admin-dept.sql"));
            populator.setSqlScriptEncoding("UTF-8");
            populator.setContinueOnError(true);

            // 直接使用 ResourceDatabasePopulator 执行SQL脚本
            populator.execute(new GroupDataSourceProxy(dataSource, "user"));

            log.info("数据库数据初始化完成！");
        } catch (Exception e) {
            log.error("数据库初始化失败: {}", e.getMessage(), e);
            throw new RuntimeException("数据库初始化失败", e);
        }
    }
}
