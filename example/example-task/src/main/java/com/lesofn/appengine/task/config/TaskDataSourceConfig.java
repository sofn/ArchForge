package com.lesofn.appengine.task.config;

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
 * Task DataSource Configuration - replaces task-mysql.xml
 */
@Configuration
public class TaskDataSourceConfig {

    @Value("${task.jdbc.master.driver}")
    private String masterDriverClassName;
    
    @Value("${task.jdbc.master.url}")
    private String masterUrl;
    
    @Value("${task.jdbc.master.username}")
    private String masterUsername;
    
    @Value("${task.jdbc.master.password}")
    private String masterPassword;
    
    @Value("${task.jdbc.slave.driver}")
    private String slaveDriverClassName;
    
    @Value("${task.jdbc.slave.url}")
    private String slaveUrl;
    
    @Value("${task.jdbc.slave.username}")
    private String slaveUsername;
    
    @Value("${task.jdbc.slave.password}")
    private String slavePassword;

    /**
     * Stat filter for Druid monitoring
     */
    @Bean
    public StatFilter statFilter() {
        StatFilter filter = new StatFilter();
        filter.setMergeSql(true);
        filter.setSlowSqlMillis(500);
        filter.setLogSlowSql(true);
        return filter;
    }

    /**
     * Task Master DataSource
     */
    @Bean
    public DataSource taskDataSourceMaster() {
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
     * Task Slave DataSource
     */
    @Bean
    public DataSource taskDataSourceSlave() {
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
     * Task DataSource Router (Read/Write splitting)
     */
    @Bean
    public DataSource taskDataSource() {
        ReadWriteDataSourceRouter router = new ReadWriteDataSourceRouter();
        
        Map<Object, Object> targetDataSources = new HashMap<>();
        targetDataSources.put(DataBaseType.Master, taskDataSourceMaster());
        targetDataSources.put(DataBaseType.Slave, taskDataSourceSlave());
        
        router.setTargetDataSources(targetDataSources);
        router.setDefaultTargetDataSource(taskDataSourceMaster());
        
        return router;
    }
}
