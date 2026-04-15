package com.lesofn.archsmith.user.config;

import com.lesofn.archsmith.infrastructure.frame.database.GroupDataSourceProxy;
import jakarta.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@EnableJpaRepositories(
        basePackages = {
            "com.lesofn.archsmith.user.dao",
            "com.lesofn.archsmith.user.menu.repository",
            "com.lesofn.archsmith.user.dept.repository"
        },
        entityManagerFactoryRef = "userEntityManagerFactory",
        transactionManagerRef = "userTransactionManager")
public class UserDbConfig {

    @Resource private DataSource dataSource;

    @Value("${spring.jpa.hibernate.ddl-auto:update}")
    private String ddlAuto;

    @Bean
    @Primary
    PlatformTransactionManager userTransactionManager() {
        return new JpaTransactionManager(userEntityManagerFactory().getObject());
    }

    @Bean
    LocalContainerEntityManagerFactoryBean userEntityManagerFactory() {
        HibernateJpaVendorAdapter jpaVendorAdapter = new HibernateJpaVendorAdapter();
        jpaVendorAdapter.setGenerateDdl(true);
        jpaVendorAdapter.setShowSql(false);
        jpaVendorAdapter.setDatabasePlatform("org.hibernate.dialect.PostgreSQLDialect");

        LocalContainerEntityManagerFactoryBean factoryBean =
                new LocalContainerEntityManagerFactoryBean();

        factoryBean.setDataSource(new GroupDataSourceProxy(dataSource, "user"));
        factoryBean.setJpaVendorAdapter(jpaVendorAdapter);
        factoryBean.setPersistenceUnitName("user");
        // 此处应包含当前模块的domain类与需要自动应用的JPA转换器所在的包
        factoryBean.setPackagesToScan(
                "com.lesofn.archsmith.user.domain",
                "com.lesofn.archsmith.user.menu.repository",
                "com.lesofn.archsmith.user.dept.repository",
                "com.lesofn.archsmith.common.repository.converter");

        // Set JPA properties
        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.hbm2ddl.auto", ddlAuto);
        properties.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        properties.put(
                "hibernate.physical_naming_strategy",
                "org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy");
        properties.put(
                "hibernate.implicit_naming_strategy",
                "org.hibernate.boot.model.naming.ImplicitNamingStrategyJpaCompliantImpl");
        factoryBean.setJpaPropertyMap(properties);

        return factoryBean;
    }
}
