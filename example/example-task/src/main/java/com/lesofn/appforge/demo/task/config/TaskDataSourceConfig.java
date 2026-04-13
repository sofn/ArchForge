package com.lesofn.appforge.demo.task.config;

import com.lesofn.appforge.infrastructure.frame.database.GroupDataSourceProxy;
import javax.sql.DataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Task DataSource Configuration. Uses dynamic-datasource "task" group (task_master + task_slave).
 */
@Configuration
public class TaskDataSourceConfig {

    /**
     * Task DataSource - proxies to the "task" dynamic-datasource group. The group's master-slave
     * routing is handled by dynamic-datasource strategy.
     */
    @Bean
    public DataSource taskDataSource(DataSource dataSource) {
        return new GroupDataSourceProxy(dataSource, "task");
    }
}
