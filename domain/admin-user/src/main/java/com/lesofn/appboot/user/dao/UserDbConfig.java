package com.lesofn.appboot.user.dao;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.core.io.ClassPathResource;

import jakarta.annotation.Resource;
import javax.sql.DataSource;

@Configuration
@EnableJpaRepositories(entityManagerFactoryRef = "userEntityManagerFactory", transactionManagerRef = "userTransactionManager")
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

        LocalContainerEntityManagerFactoryBean factoryBean = new LocalContainerEntityManagerFactoryBean();

        factoryBean.setDataSource(dataSource);
        factoryBean.setJpaVendorAdapter(jpaVendorAdapter);
        factoryBean.setPersistenceUnitName("user");
        //此处应包含当前模块的domain类
        factoryBean.setPackagesToScan("com.lesofn.appboot.user.domain");

        return factoryBean;
    }

    @Bean
    @Profile("dev")
    public DataSourceInitializer userDataSourceInitializer() {
        DataSourceInitializer initializer = new DataSourceInitializer();
        initializer.setDataSource(dataSource);
        
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.addScript(new ClassPathResource("sql/data-admin-user.sql"));
        populator.setSqlScriptEncoding("UTF-8");
        populator.setContinueOnError(true);
        
        initializer.setDatabasePopulator(populator);
        return initializer;
    }

}
