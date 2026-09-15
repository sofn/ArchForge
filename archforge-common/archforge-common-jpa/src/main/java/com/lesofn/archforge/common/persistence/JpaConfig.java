package com.lesofn.archforge.common.persistence;

import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * 全应用唯一的 JPA 装配：单 EntityManagerFactory + 单 TransactionManager（server-admin 与 server-web 共用）。
 *
 * <p>
 * 扫描根固定为全局根包 {@code com.lesofn.archforge}：
 *
 * <ul>
 * <li>字符串包名不产生 common → domain 编译依赖（ARCH-006 不破）；
 * <li>Hibernate 只注册 classpath 上真实存在的 {@code @Entity}，Spring Data 只为 classpath 上存在的
 * Repository 接口建 Bean——各 app 自动适配自己的装配集（server-web 不装 meta-table 即不建其仓库）；
 * <li>生成的 {@code archforge-module-*} 模块（{@code com.lesofn.archforge.generated.*}）零配置被接管。
 * </ul>
 *
 * <p>
 * EMF 直接绑定 primary {@link DataSource}（dynamic-datasource 路由数据源）。方法级路由仍由 {@code @DS}
 * 注解承担——不再有 {@code GroupDataSourceProxy} 幽灵分组包装。schema 归 Flyway 独占，{@code ddl-auto} 默认
 * {@code validate}。
 */
@Configuration
@RequiredArgsConstructor
@EnableJpaRepositories(basePackages = JpaConfig.SCAN_ROOT)
public class JpaConfig {

    /** 全局根包：实体与仓库的扫描根。 */
    static final String SCAN_ROOT = "com.lesofn.archforge";

    private final DataSource dataSource;

    @Value("${spring.jpa.hibernate.ddl-auto:validate}")
    private String ddlAuto;

    @Bean
    @Primary
    LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        HibernateJpaVendorAdapter jpaVendorAdapter = new HibernateJpaVendorAdapter();
        jpaVendorAdapter.setGenerateDdl(false);
        jpaVendorAdapter.setShowSql(false);
        jpaVendorAdapter.setDatabasePlatform("org.hibernate.dialect.PostgreSQLDialect");

        LocalContainerEntityManagerFactoryBean factoryBean = new LocalContainerEntityManagerFactoryBean();
        factoryBean.setDataSource(dataSource);
        factoryBean.setJpaVendorAdapter(jpaVendorAdapter);
        factoryBean.setPersistenceUnitName("archforge");
        factoryBean.setPackagesToScan(SCAN_ROOT);

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
    @Primary
    PlatformTransactionManager transactionManager(
            LocalContainerEntityManagerFactoryBean entityManagerFactory) {
        return new JpaTransactionManager(java.util.Objects.requireNonNull(entityManagerFactory.getObject()));
    }
}
