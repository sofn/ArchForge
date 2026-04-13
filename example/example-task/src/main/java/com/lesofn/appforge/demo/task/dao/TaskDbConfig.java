package com.lesofn.appforge.demo.task.dao;

import com.lesofn.appforge.infrastructure.frame.database.GroupDataSourceProxy;
import jakarta.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@EnableJpaRepositories(
        entityManagerFactoryRef = "taskEntityManagerFactory",
        transactionManagerRef = "taskTransactionManager")
public class TaskDbConfig {

    @Resource private DataSource dataSource;

    @Bean
    PlatformTransactionManager taskTransactionManager() {
        return new JpaTransactionManager(taskEntityManagerFactory().getObject());
    }

    @Bean
    LocalContainerEntityManagerFactoryBean taskEntityManagerFactory() {

        HibernateJpaVendorAdapter jpaVendorAdapter = new HibernateJpaVendorAdapter();
        jpaVendorAdapter.setGenerateDdl(true);
        jpaVendorAdapter.setShowSql(false);
        jpaVendorAdapter.setDatabasePlatform("org.hibernate.dialect.MySQLDialect");

        LocalContainerEntityManagerFactoryBean factoryBean =
                new LocalContainerEntityManagerFactoryBean();

        factoryBean.setDataSource(new GroupDataSourceProxy(dataSource, "task"));
        factoryBean.setJpaVendorAdapter(jpaVendorAdapter);
        factoryBean.setPersistenceUnitName("task");
        // 此处应包含当前模块的domain类
        String packageName = TaskDbConfig.class.getPackage().getName();
        factoryBean.setPackagesToScan(packageName.substring(0, packageName.lastIndexOf('.')));

        // Set JPA properties
        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.hbm2ddl.auto", "update");
        properties.put("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");
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
