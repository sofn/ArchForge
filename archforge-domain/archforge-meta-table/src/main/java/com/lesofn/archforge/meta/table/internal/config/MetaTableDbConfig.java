package com.lesofn.archforge.meta.table.internal.config;

import com.lesofn.archforge.common.persistence.GroupDataSourceProxy;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * 元表格模块持久化配置。
 *
 * <p>
 * 复用 {@code user} 数据源组，为该模块提供独立的 {@link jakarta.persistence.EntityManagerFactory}
 * 与 {@link NamedParameterJdbcTemplate}，以便在业务事务内同时执行元数据写入与物理表 DDL/DML。
 *
 * <p>
 * 注意：本配置不注册任何 {@link DataSource} Bean，仅使用注入的 primary {@code dataSource} 构造
 * {@link GroupDataSourceProxy} 实例，避免与 dynamic-datasource 形成循环依赖。
 */
@Configuration
@RequiredArgsConstructor
@EnableJpaRepositories(
        basePackages = "com.lesofn.archforge.meta.table.api.dao",
        entityManagerFactoryRef = "metaTableEntityManagerFactory",
        transactionManagerRef = "metaTableTransactionManager")
public class MetaTableDbConfig {

    @Value("${spring.jpa.hibernate.ddl-auto:none}")
    private String ddlAuto;

    private final DataSource dataSource;

    private DataSource metaDataSource() {
        return new GroupDataSourceProxy(dataSource, "user");
    }

    @Bean
    PlatformTransactionManager metaTableTransactionManager() {
        JpaTransactionManager transactionManager = new JpaTransactionManager(metaTableEntityManagerFactory().getObject());
        transactionManager.setDataSource(metaDataSource());
        return transactionManager;
    }

    @Bean
    LocalContainerEntityManagerFactoryBean metaTableEntityManagerFactory() {
        HibernateJpaVendorAdapter jpaVendorAdapter = new HibernateJpaVendorAdapter();
        jpaVendorAdapter.setGenerateDdl(true);
        jpaVendorAdapter.setShowSql(false);
        jpaVendorAdapter.setDatabasePlatform("org.hibernate.dialect.PostgreSQLDialect");

        LocalContainerEntityManagerFactoryBean factoryBean = new LocalContainerEntityManagerFactoryBean();
        factoryBean.setDataSource(metaDataSource());
        factoryBean.setJpaVendorAdapter(jpaVendorAdapter);
        factoryBean.setPersistenceUnitName("meta-table");
        factoryBean.setPackagesToScan(
                "com.lesofn.archforge.meta.table.api.domain",
                "com.lesofn.archforge.common.repository.converter");

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

    @Bean
    NamedParameterJdbcTemplate metaTableJdbcTemplate() {
        return new NamedParameterJdbcTemplate(metaDataSource());
    }
}
