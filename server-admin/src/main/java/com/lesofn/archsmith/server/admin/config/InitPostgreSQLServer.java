package com.lesofn.archsmith.server.admin.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.HashMap;
import java.util.Map;

/**
 * 使用 Testcontainers 启动 PostgreSQL 容器，替代 H2 文件数据库。
 * 作为 ApplicationContextInitializer 在 Spring 上下文加载前启动容器。
 *
 * @author sofn
 */
@Slf4j
public class InitPostgreSQLServer
        implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private static final String POSTGRES_IMAGE = "postgres:17-alpine";
    private static final String DB_USER = "archsmith";
    private static final String DB_PASSWORD = "archsmith";

    private static PostgreSQLContainer<?> userContainer;
    private static PostgreSQLContainer<?> taskContainer;

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        ConfigurableEnvironment env = applicationContext.getEnvironment();

        // 只在 dev profile 且 embedded.postgresql=true 时启动
        if (!env.matchesProfiles("dev")) {
            return;
        }
        String pgEnabled = env.getProperty("arch-smith.embedded.postgresql", "false");
        if (!"true".equals(pgEnabled)) {
            return;
        }

        startContainers(env);
    }

    private synchronized void startContainers(ConfigurableEnvironment env) {
        if (userContainer != null && userContainer.isRunning()) {
            // 容器已在运行（如重启情况）
            applyProperties(env);
            return;
        }

        try {
            log.info("Starting PostgreSQL containers via Testcontainers...");

            userContainer =
                    new PostgreSQLContainer<>(POSTGRES_IMAGE)
                            .withDatabaseName("archsmith_user")
                            .withUsername(DB_USER)
                            .withPassword(DB_PASSWORD);
            userContainer.start();

            taskContainer =
                    new PostgreSQLContainer<>(POSTGRES_IMAGE)
                            .withDatabaseName("archsmith_task")
                            .withUsername(DB_USER)
                            .withPassword(DB_PASSWORD);
            taskContainer.start();

            log.info(
                    "PostgreSQL containers started: user={}, task={}",
                    userContainer.getJdbcUrl(),
                    taskContainer.getJdbcUrl());

            applyProperties(env);

            // 注册关闭钩子
            Runtime.getRuntime()
                    .addShutdownHook(
                            new Thread(
                                    () -> {
                                        if (userContainer != null) userContainer.stop();
                                        if (taskContainer != null) taskContainer.stop();
                                    }));

        } catch (Exception e) {
            log.error("Failed to start PostgreSQL containers", e);
            throw new RuntimeException("Failed to start PostgreSQL containers", e);
        }
    }

    private void applyProperties(ConfigurableEnvironment env) {
        Map<String, Object> props = new HashMap<>();

        props.put(
                "spring.datasource.dynamic.datasource.user_master.url",
                userContainer.getJdbcUrl());
        props.put(
                "spring.datasource.dynamic.datasource.user_master.username",
                userContainer.getUsername());
        props.put(
                "spring.datasource.dynamic.datasource.user_master.password",
                userContainer.getPassword());
        props.put(
                "spring.datasource.dynamic.datasource.user_master.driver-class-name",
                "org.postgresql.Driver");

        props.put(
                "spring.datasource.dynamic.datasource.user_slave.url",
                userContainer.getJdbcUrl());
        props.put(
                "spring.datasource.dynamic.datasource.user_slave.username",
                userContainer.getUsername());
        props.put(
                "spring.datasource.dynamic.datasource.user_slave.password",
                userContainer.getPassword());
        props.put(
                "spring.datasource.dynamic.datasource.user_slave.driver-class-name",
                "org.postgresql.Driver");

        props.put(
                "spring.datasource.dynamic.datasource.task_master.url",
                taskContainer.getJdbcUrl());
        props.put(
                "spring.datasource.dynamic.datasource.task_master.username",
                taskContainer.getUsername());
        props.put(
                "spring.datasource.dynamic.datasource.task_master.password",
                taskContainer.getPassword());
        props.put(
                "spring.datasource.dynamic.datasource.task_master.driver-class-name",
                "org.postgresql.Driver");

        props.put(
                "spring.datasource.dynamic.datasource.task_slave.url",
                taskContainer.getJdbcUrl());
        props.put(
                "spring.datasource.dynamic.datasource.task_slave.username",
                taskContainer.getUsername());
        props.put(
                "spring.datasource.dynamic.datasource.task_slave.password",
                taskContainer.getPassword());
        props.put(
                "spring.datasource.dynamic.datasource.task_slave.driver-class-name",
                "org.postgresql.Driver");

        env.getPropertySources()
                .addFirst(new MapPropertySource("testcontainersPostgresql", props));
    }
}
