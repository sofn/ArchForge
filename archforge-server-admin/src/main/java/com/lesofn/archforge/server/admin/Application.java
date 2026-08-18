package com.lesofn.archforge.server.admin;

import com.lesofn.archforge.server.admin.config.InitPostgreSQLServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration;
import org.springframework.boot.jackson.autoconfigure.JacksonAutoConfiguration;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.boot.jdbc.autoconfigure.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication(
        scanBasePackages = {
                "com.lesofn.archforge.blog",
                "com.lesofn.archforge.common",
                "com.lesofn.archforge.demo.task",
                "com.lesofn.archforge.infrastructure",
                "com.lesofn.archforge.meta.table",
                "com.lesofn.archforge.server.admin",
                "com.lesofn.archforge.user"
        },
        exclude = {
                DataSourceAutoConfiguration.class,
                HibernateJpaAutoConfiguration.class,
                DataSourceTransactionManagerAutoConfiguration.class,
                JacksonAutoConfiguration.class
        })
@EnableTransactionManagement
public class Application {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(Application.class);
        app.addInitializers(new InitPostgreSQLServer());
        app.run(args);
    }
}
