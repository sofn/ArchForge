package com.lesofn.appboot.server.admin.config;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.io.IOException;

/**
 * @author sofn
 * @version 1.0 Created at: 2025-08-25 23:33
 */
@Slf4j
@Component
@Profile("dev")
@ConditionalOnProperty(name = "app-boot.embedded.h2", havingValue = "true")
public class InitDbMockServer {

    @Resource(name = "userDataSource")
    private DataSource dataSource;

    @PostConstruct
    public void init() {
        DataSourceInitializer initializer = new DataSourceInitializer();
        initializer.setDataSource(dataSource);

        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.addScript(new ClassPathResource("sql/data-admin-user.sql"));
        populator.setSqlScriptEncoding("UTF-8");
        populator.setContinueOnError(true);

        initializer.setDatabasePopulator(populator);
    }
}
