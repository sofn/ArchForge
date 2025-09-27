package com.lesofn.appboot.user.config;

import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableJpaRepositories(
        basePackages = {"com.lesofn.appboot.user.dao", "com.lesofn.appboot.user.menu.repository", "com.lesofn.appboot.user.dept.repository"},
        entityManagerFactoryRef = "userEntityManagerFactory",
        transactionManagerRef = "userTransactionManager"
)
public class UserDbConfig {

    @Resource(name = "userDataSource")
    private DataSource dataSource;

    @Bean
    PlatformTransactionManager userTransactionManager() {
        return new JpaTransactionManager(userEntityManagerFactory().getObject());
    }

    @Bean
    LocalContainerEntityManagerFactoryBean userEntityManagerFactory() {
        HibernateJpaVendorAdapter jpaVendorAdapter = new HibernateJpaVendorAdapter();
        jpaVendorAdapter.setGenerateDdl(true);
        jpaVendorAdapter.setShowSql(false);
        jpaVendorAdapter.setDatabasePlatform("org.hibernate.dialect.MySQLDialect");

        LocalContainerEntityManagerFactoryBean factoryBean = new LocalContainerEntityManagerFactoryBean();

        factoryBean.setDataSource(dataSource);
        factoryBean.setJpaVendorAdapter(jpaVendorAdapter);
        factoryBean.setPersistenceUnitName("user");
        // 此处应包含当前模块的domain类与需要自动应用的JPA转换器所在的包
        factoryBean.setPackagesToScan(
                "com.lesofn.appboot.user.domain",
                "com.lesofn.appboot.user.menu.repository",
                "com.lesofn.appboot.user.dept.repository",
                "com.lesofn.appboot.common.repository.converter"
        );
        
        // Set JPA properties
        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.hbm2ddl.auto", "update");
        properties.put("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");
        properties.put("hibernate.physical_naming_strategy", "org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy");
        properties.put("hibernate.implicit_naming_strategy", "org.hibernate.boot.model.naming.ImplicitNamingStrategyJpaCompliantImpl");
        factoryBean.setJpaPropertyMap(properties);

        return factoryBean;
    }

}
