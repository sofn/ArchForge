package com.lesofn.archforge.server.web;

import com.lesofn.archforge.common.persistence.testsupport.AbstractIntegrationTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.web.client.RestClient;

/**
 * server-web 集成测试基类：完整 Spring 上下文 + 随机端口 + RestClient。
 *
 * <p>
 * PostgreSQL / Redis 由父类经 Testcontainers 提供并注入数据源连接信息； schema 由
 * common-jpa classpath 上的 Flyway migrations 建立（application-test.yaml 已开启
 * {@code arch-forge.flyway.enabled}）。
 *
 * @author sofn
 */
@SpringBootTest(
        classes = Application.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class AbstractWebIntegrationTest extends AbstractIntegrationTest {

    @LocalServerPort
    protected int port;

    protected RestClient restClient() {
        return RestClient.builder().baseUrl("http://localhost:" + port).build();
    }
}
