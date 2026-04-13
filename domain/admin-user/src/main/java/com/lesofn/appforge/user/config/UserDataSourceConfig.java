package com.lesofn.appforge.user.config;

import com.lesofn.appforge.infrastructure.frame.database.GroupDataSourceProxy;
import javax.sql.DataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * User DataSource Configuration. Uses dynamic-datasource "user" group (user_master + user_slave).
 */
@Configuration
public class UserDataSourceConfig {

    /**
     * User DataSource - proxies to the "user" dynamic-datasource group. The group's master-slave
     * routing is handled by dynamic-datasource strategy.
     */
    @Bean
    public DataSource userDataSource(DataSource dataSource) {
        return new GroupDataSourceProxy(dataSource, "user");
    }
}
