package com.lesofn.archforge.server.admin.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lesofn.archforge.common.persistence.testsupport.AbstractIntegrationTest;
import com.lesofn.archforge.server.admin.Application;
import java.net.InetAddress;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import javax.sql.DataSource;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.client.RestClient;

/**
 * The IP in the audit logs, through the assembled application: a client that is not a trusted proxy cannot choose
 * it with {@code X-Forwarded-For}. (A unit test of one filter missed that a later enricher overwrote its result.)
 */
@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Tag("slow")
class ClientIpIntegrationTest extends AbstractIntegrationTest {

    private static final String FORGED = "6.6.6.6";
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final AtomicLong SEQ = new AtomicLong(System.currentTimeMillis() % 100_000);

    @LocalServerPort
    private int port;

    @Autowired
    private DataSource dataSource;

    private RestClient restClient;
    private JdbcTemplate jdbc;

    @BeforeEach
    void setUp() {
        restClient = RestClient.builder().baseUrl("http://localhost:" + port).build();
        jdbc = new JdbcTemplate(dataSource);
    }

    @Test
    void aFailedLoginRecordsThePeerNotAForgedHeader() throws Exception {
        String username = "ipprobe" + SEQ.incrementAndGet();
        post("/admin/auth/login", Map.of("username", username, "password", "wrong"), null);

        String ip = jdbc.queryForObject("SELECT ip FROM sys_login_log WHERE username = ? ORDER BY login_time DESC LIMIT 1",
                String.class, username);

        assertPeer(ip);
    }

    @Test
    void aSuccessfulLoginRecordsThePeerNotAForgedHeader() throws Exception {
        login();

        String ip = jdbc.queryForObject(
                "SELECT ip FROM sys_login_log WHERE username = 'admin' AND status = 1 ORDER BY login_time DESC LIMIT 1",
                String.class);

        assertPeer(ip);
    }

    @Test
    void operationLogsRecordThePeerNotAForgedHeader() throws Exception {
        String token = login();
        String dictCode = "ipprobe_" + SEQ.incrementAndGet();
        post("/admin/system/dict/type/create", Map.of("dictCode", dictCode, "dictName", dictCode), token);

        String ip = null;
        for (int i = 0; i < 50 && ip == null; i++) { // operation logs are written asynchronously after commit
            List<String> rows = jdbc.queryForList(
                    "SELECT ip FROM sys_oper_log WHERE module = '字典配置' AND operating_time > now() - interval '1 minute'" +
                            " ORDER BY operating_time DESC LIMIT 1", String.class);
            ip = rows.isEmpty() ? null : rows.get(0);
            if (ip == null) {
                java.util.concurrent.locks.LockSupport.parkNanos(100_000_000L);
            }
        }

        assertPeer(ip);
    }

    private String login() throws Exception {
        Map<String, Object> response = post("/admin/auth/login", Map.of("username", "admin", "password", "admin123"),
                null);
        assertEquals(0, ((Number) Objects.requireNonNull(response.get("code"))).intValue(), response.toString());
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) Objects.requireNonNull(response.get("data"));
        return String.valueOf(Objects.requireNonNull(data.get("accessToken")));
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> post(String path, Map<String, Object> body, @Nullable String token) throws Exception {
        RestClient.RequestBodySpec spec = restClient.post().uri(path).header("Content-Type", "application/json")
                .header("X-Forwarded-For", FORGED);
        if (token != null) {
            spec = spec.header("Authorization", "Bearer " + token);
        }
        String raw = spec.body(MAPPER.writeValueAsString(body)).retrieve().onStatus(s -> true, (req, res) -> {
        }).body(String.class);
        return MAPPER.readValue(Objects.requireNonNull(raw), Map.class);
    }

    private static void assertPeer(@Nullable String ip) throws Exception {
        assertNotNull(ip, "no log row");
        assertTrue(!FORGED.equals(ip) && InetAddress.getByName(ip).isLoopbackAddress(), "logged IP: " + ip);
    }
}
