package com.lesofn.archforge.user.infrastructure.config;

import com.lesofn.archforge.common.persistence.GroupDataSourceProxy;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * 用户领域持久化配置。
 *
 * <p>
 * 为新的 {@code UserPO} 提供独立的 {@link jakarta.persistence.EntityManagerFactory}，
 * 避免与旧 {@code SysUser} 等实体映射同一张表产生冲突。
 */
@Configuration
@RequiredArgsConstructor
@EnableJpaRepositories(
        basePackages = {
                "com.lesofn.archforge.user.infrastructure.dao"
        },
        entityManagerFactoryRef = "userDomainEntityManagerFactory",
        transactionManagerRef = "userDomainTransactionManager")
public class UserDomainDbConfig {

    private final DataSource dataSource;

    @Value("${spring.jpa.hibernate.ddl-auto:update}")
    private String ddlAuto;

    @Bean
    PlatformTransactionManager userDomainTransactionManager() {
        return new JpaTransactionManager(userDomainEntityManagerFactory().getObject());
    }

    @Bean
    LocalContainerEntityManagerFactoryBean userDomainEntityManagerFactory() {
        HibernateJpaVendorAdapter jpaVendorAdapter = new HibernateJpaVendorAdapter();
        jpaVendorAdapter.setGenerateDdl(false);
        jpaVendorAdapter.setShowSql(false);
        jpaVendorAdapter.setDatabasePlatform("org.hibernate.dialect.PostgreSQLDialect");

        LocalContainerEntityManagerFactoryBean factoryBean = new LocalContainerEntityManagerFactoryBean();

        factoryBean.setDataSource(new GroupDataSourceProxy(dataSource, "user"));
        factoryBean.setJpaVendorAdapter(jpaVendorAdapter);
        factoryBean.setPersistenceUnitName("user-domain");
        factoryBean.setPackagesToScan(
                "com.lesofn.archforge.user.infrastructure.adapter.repository.po",
                "com.lesofn.archforge.common.repository.converter");

        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.hbm2ddl.auto", "none");
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
