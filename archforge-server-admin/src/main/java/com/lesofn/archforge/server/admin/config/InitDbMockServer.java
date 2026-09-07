package com.lesofn.archforge.server.admin.config;

import com.lesofn.archforge.common.persistence.GroupDataSourceProxy;
import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Component;

/**
 * @author sofn
 * @version 1.0 Created at: 2025-08-25 23:33
 */
@Slf4j
@Component
@Profile("test")
@ConditionalOnProperty(name = "arch-forge.embedded.db-init", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
public class InitDbMockServer {

    private final DataSource dataSource;

    @PostConstruct
    public void init() {
        try {
            log.info("开始初始化数据库数据...");

            DataSource userDs = new GroupDataSourceProxy(dataSource, "user");
            DataSource taskDs = new GroupDataSourceProxy(dataSource, "task");

            ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
            populator.addScript(new ClassPathResource("sql/data-admin-user.sql"));
            populator.addScript(new ClassPathResource("sql/data-admin-dept.sql"));
            populator.addScript(new ClassPathResource("sql/data-admin-config.sql"));
            populator.addScript(new ClassPathResource("sql/data-admin-scheduler.sql"));
            populator.addScript(new ClassPathResource("sql/data-admin-blog.sql"));
            populator.setSqlScriptEncoding(StandardCharsets.UTF_8.name());
            populator.setContinueOnError(true);

            populator.execute(userDs);

            // 重置 PostgreSQL 序列到最大 ID 之后
            resetSequences(userDs);
            resetTaskSequences(taskDs);

            log.info("数据库数据初始化完成！");
        } catch (Exception e) {
            log.error("数据库初始化失败: {}", e.getMessage(), e);
            throw new RuntimeException("数据库初始化失败", e);
        }
    }

    private void resetSequences(DataSource ds) {
        JdbcTemplate jdbc = new JdbcTemplate(ds);
        String[] sequences = {
                "sys_user_user_id_seq",
                "sys_menu_menu_id_seq",
                "sys_role_role_id_seq",
                "sys_dept_dept_id_seq",
                "sys_config_config_id_seq",
                "sys_notice_notice_id_seq",
                "sys_oper_log_oper_id_seq",
                "sys_login_log_info_id_seq",
                "sys_file_file_id_seq",
                "blog_category_id_seq",
                "blog_article_id_seq"
        };
        String[] tables = {
                "sys_user",
                "sys_menu",
                "sys_role",
                "sys_dept",
                "sys_config",
                "sys_notice",
                "sys_oper_log",
                "sys_login_log",
                "sys_file",
                "blog_category",
                "blog_article"
        };
        String[] idCols = {
                "user_id",
                "menu_id",
                "role_id",
                "dept_id",
                "config_id",
                "notice_id",
                "oper_id",
                "info_id",
                "file_id",
                "id",
                "id"
        };
        for (int i = 0; i < sequences.length; i++) {
            try {
                String sql = String.format(
                        "SELECT setval('%s', COALESCE((SELECT MAX(%s) FROM %s), 1))",
                        sequences[i], idCols[i], tables[i]);
                jdbc.execute(sql);
            } catch (Exception e) {
                log.warn("重置序列 {} 失败: {}", sequences[i], e.getMessage());
            }
        }
    }

    private void resetTaskSequences(DataSource ds) {
        JdbcTemplate jdbc = new JdbcTemplate(ds);
        try {
            jdbc.execute(
                    "SELECT setval('task_id_seq', COALESCE((SELECT MAX(id) FROM task), 1))");
        } catch (Exception e) {
            log.warn("重置 task 序列失败: {}", e.getMessage());
        }
    }
}
