package com.lesofn.appengine.user.config;

import com.alibaba.druid.filter.stat.StatFilter;
import com.alibaba.druid.pool.DruidDataSource;
import com.lesofn.appengine.frame.database.DataBaseType;
import com.lesofn.appengine.frame.database.ReadWriteDataSourceRouter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * User DataSource Configuration - replaces user-mysql.xml
 */
@Configuration
public class UserDataSourceConfig {

    @Value("${user.jdbc.master.driver}")
    private String masterDriverClassName;
    
    @Value("${user.jdbc.master.url}")
    private String masterUrl;
    
    @Value("${user.jdbc.master.username}")
    private String masterUsername;
    
    @Value("${user.jdbc.master.password}")
    private String masterPassword;
    
    @Value("${user.jdbc.slave.driver}")
    private String slaveDriverClassName;
    
    @Value("${user.jdbc.slave.url}")
    private String slaveUrl;
    
    @Value("${user.jdbc.slave.username}")
    private String slaveUsername;
    
    @Value("${user.jdbc.slave.password}")
    private String slavePassword;

    /**
     * User Master DataSource
     */
    @Bean
    public DataSource userDataSourceMaster() {
        DruidDataSource dataSource = new DruidDataSource();
        
        // Database connection
        dataSource.setDriverClassName(masterDriverClassName);
        dataSource.setUrl(masterUrl);
        dataSource.setUsername(masterUsername);
        dataSource.setPassword(masterPassword);
        
        // Connection pool settings
        dataSource.setInitialSize(0);
        dataSource.setMaxActive(5);
        dataSource.setMinIdle(1);
        dataSource.setMaxWait(60000);
        
        // Connection validation
        dataSource.setTimeBetweenEvictionRunsMillis(60000);
        dataSource.setMinEvictableIdleTimeMillis(300000);
        dataSource.setValidationQuery("SELECT 1");
        dataSource.setTestWhileIdle(true);
        dataSource.setTestOnBorrow(false);
        dataSource.setTestOnReturn(false);
        
        // PSCache settings
        dataSource.setPoolPreparedStatements(true);
        dataSource.setMaxPoolPreparedStatementPerConnectionSize(20);
        
        return dataSource;
    }

    /**
     * User Slave DataSource
     */
    @Bean
    public DataSource userDataSourceSlave() {
        DruidDataSource dataSource = new DruidDataSource();
        
        // Database connection
        dataSource.setDriverClassName(slaveDriverClassName);
        dataSource.setUrl(slaveUrl);
        dataSource.setUsername(slaveUsername);
        dataSource.setPassword(slavePassword);
        
        // Connection pool settings
        dataSource.setInitialSize(0);
        dataSource.setMaxActive(5);
        dataSource.setMinIdle(1);
        dataSource.setMaxWait(60000);
        
        // Connection validation
        dataSource.setTimeBetweenEvictionRunsMillis(60000);
        dataSource.setMinEvictableIdleTimeMillis(300000);
        dataSource.setValidationQuery("SELECT 1");
        dataSource.setTestWhileIdle(true);
        dataSource.setTestOnBorrow(false);
        dataSource.setTestOnReturn(false);
        
        // PSCache settings
        dataSource.setPoolPreparedStatements(true);
        dataSource.setMaxPoolPreparedStatementPerConnectionSize(20);
        
        return dataSource;
    }

    /**
     * User DataSource Router (Read/Write splitting)
     */
    @Bean
    public DataSource userDataSource() {
        ReadWriteDataSourceRouter router = new ReadWriteDataSourceRouter();
        
        Map<Object, Object> targetDataSources = new HashMap<>();
        targetDataSources.put(DataBaseType.Master, userDataSourceMaster());
        targetDataSources.put(DataBaseType.Slave, userDataSourceSlave());
        
        router.setTargetDataSources(targetDataSources);
        router.setDefaultTargetDataSource(userDataSourceMaster());
        
        return router;
    }
}
