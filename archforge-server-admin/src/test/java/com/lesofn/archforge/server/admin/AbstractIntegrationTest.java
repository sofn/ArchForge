package com.lesofn.archforge.server.admin;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.jspecify.annotations.Nullable;
import org.springframework.test.context.DynamicPropertySource;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.HttpWaitStrategy;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * 集成测试基础设施：以 JVM 内单例的方式提供 PostgreSQL 与 Redis。
 *
 * <p>
 * 这里只负责「把依赖服务准备好并把连接信息告诉 Spring」，不参与任何业务逻辑。 容器在静态块中启动一次，同一 JVM 内的所有集成测试共享，避免每个测试类都拉一次镜像。
 *
 * <p>
 * 与过去的区别：这些代码位于 {@code src/test}，不会进入生产包，testcontainers 也不再以 {@code api} 作用域泄漏给下游模块。
 *
 * <p>
 * 对象存储默认不做任何处理 —— 应用本身默认使用本地文件存储（{@code arch-forge.file-storage.type=local}）， 因此没有 S3 也能正常跑。需要覆盖真实 S3 路径时，用
 * {@code -Darch-forge.embedded.s3=true} 开启 RustFS 容器。
 *
 * @author sofn
 */
public abstract class AbstractIntegrationTest {

    @SuppressWarnings("unchecked")
    protected static Map<String, Object> dataOf(Map<String, Object> resp) {
        return (Map<String, Object>) Objects.requireNonNull(resp.get("data"));
    }

    @SuppressWarnings("unchecked")
    protected static List<Map<String, Object>> listOf(Map<String, Object> data) {
        return (List<Map<String, Object>>) Objects.requireNonNull(data.get("list"));
    }

    protected static long numOf(Map<String, Object> row, String key) {
        return ((Number) Objects.requireNonNull(row.get(key))).longValue();
    }

    protected static long numData(Map<String, Object> resp) {
        return ((Number) Objects.requireNonNull(resp.get("data"))).longValue();
    }

    private static final String POSTGRES_IMAGE = "postgres:17-alpine";
    private static final String REDIS_IMAGE = "redis:7-alpine";
    private static final String RUSTFS_IMAGE = "rustfs/rustfs:latest";

    private static final String DB_USER = "archforge";
    private static final String DB_PASSWORD = "archforge";
    private static final String USER_DATABASE = "archforge_user";

    private static final int POSTGRES_PORT = 5432;
    private static final int REDIS_PORT = 6379;
    private static final int S3_PORT = 9000;

    private static final String S3_ACCESS_KEY = "minioadmin";
    private static final String S3_SECRET_KEY = "minioadmin";

    /** 是否额外启动 RustFS (S3) 容器。默认关闭，对象存储走本地文件兜底。 */
    private static final String EMBEDDED_S3_PROPERTY = "arch-forge.embedded.s3";

    private static final String EMBEDDED_S3_ENV = "ARCHFORGE_EMBEDDED_S3";

    protected static final PostgreSQLContainer POSTGRES = createPostgres();

    protected static final GenericContainer<?> REDIS = createRedis();

    /** 仅当开启 embedded S3 时非 null。 */
    protected static final @Nullable GenericContainer<?> RUSTFS = createRustfsIfEnabled();

    private static PostgreSQLContainer createPostgres() {
        PostgreSQLContainer container = new PostgreSQLContainer(DockerImageName.parse(POSTGRES_IMAGE))
                .withDatabaseName(USER_DATABASE)
                .withUsername(DB_USER)
                .withPassword(DB_PASSWORD);
        container.start();
        return container;
    }

    private static GenericContainer<?> createRedis() {
        GenericContainer<?> container = new GenericContainer<>(DockerImageName.parse(REDIS_IMAGE))
                .withExposedPorts(REDIS_PORT);
        container.start();
        return container;
    }

    private static @Nullable GenericContainer<?> createRustfsIfEnabled() {
        if (!embeddedS3Enabled()) {
            return null;
        }
        GenericContainer<?> container = new GenericContainer<>(DockerImageName.parse(RUSTFS_IMAGE))
                .withExposedPorts(S3_PORT)
                .withEnv("RUSTFS_ROOT_USER", S3_ACCESS_KEY)
                .withEnv("RUSTFS_ROOT_PASSWORD", S3_SECRET_KEY)
                .withCommand("server", "/data")
                .waitingFor(new HttpWaitStrategy().forPort(S3_PORT).forPath("/health"));
        container.start();
        return container;
    }

    private static boolean embeddedS3Enabled() {
        String value = System.getProperty(EMBEDDED_S3_PROPERTY);
        if (value == null) {
            value = System.getenv(EMBEDDED_S3_ENV);
        }
        return Boolean.parseBoolean(value == null ? "false" : value);
    }

    @DynamicPropertySource
    static void registerTestInfrastructure(DynamicPropertyRegistry registry) {
        registerInfrastructureProperties(registry);
    }

    /**
     * 把容器连接信息注册进 Spring 环境。
     *
     * <p>
     * 供无法继承本类的测试复用 —— 典型是 Spock 的 {@code Specification}，它的父类位已被占用， 只能在自己的 {@code @DynamicPropertySource} 方法里委托过来。
     * 引用本类（哪怕是静态方法）会触发静态初始化，容器因此按需启动。
     */
    public static void registerInfrastructureProperties(DynamicPropertyRegistry registry) {
        String userUrl = jdbcUrl(USER_DATABASE);

        for (String name : new String[] {
                "user_master", "user_slave"
        }) {
            registerDataSource(registry, name, userUrl);
        }

        registry.add("spring.data.redis.host", REDIS::getHost);
        registry.add("spring.data.redis.port", () -> REDIS.getMappedPort(REDIS_PORT));

        if (RUSTFS != null) {
            registry.add("arch-forge.file-storage.type", () -> "s3");
            registry.add(
                    "arch-forge.file-storage.s3.endpoint",
                    () -> "http://" + RUSTFS.getHost() + ":" + RUSTFS.getMappedPort(S3_PORT));
            registry.add("arch-forge.file-storage.s3.access-key", () -> S3_ACCESS_KEY);
            registry.add("arch-forge.file-storage.s3.secret-key", () -> S3_SECRET_KEY);
        }
    }

    private static void registerDataSource(DynamicPropertyRegistry registry, String name, String url) {
        String prefix = "spring.datasource.dynamic.datasource." + name;
        registry.add(prefix + ".url", () -> url);
        registry.add(prefix + ".username", () -> DB_USER);
        registry.add(prefix + ".password", () -> DB_PASSWORD);
        registry.add(prefix + ".driver-class-name", () -> "org.postgresql.Driver");
    }

    private static String jdbcUrl(String database) {
        return String.format(
                "jdbc:postgresql://%s:%d/%s", POSTGRES.getHost(), POSTGRES.getMappedPort(POSTGRES_PORT), database);
    }
}
