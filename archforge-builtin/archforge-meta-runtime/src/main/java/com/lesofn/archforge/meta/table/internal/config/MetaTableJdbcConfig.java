package com.lesofn.archforge.meta.table.internal.config;

import javax.sql.DataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

/**
 * 元表格模块的 {@link NamedParameterJdbcTemplate}。
 *
 * <p>
 * EMF 合并后全应用只有一个 primary {@link DataSource}，本 Bean 绑定它：与唯一
 * {@link org.springframework.orm.jpa.JpaTransactionManager} 按同一 DataSource 实例绑定事务连接，
 * 业务事务内元数据写入与物理表 DDL/DML 共享同一连接。
 *
 * <p>
 * Bean 名 {@code metaTableJdbcTemplate} 是历史限定符，消费方按 {@code @Qualifier} 注入——合并后它
 * 就是主库模板，不再专属 meta-table。
 */
@Configuration
public class MetaTableJdbcConfig {

    @Bean
    NamedParameterJdbcTemplate metaTableJdbcTemplate(DataSource dataSource) {
        return new NamedParameterJdbcTemplate(dataSource);
    }
}
