package com.lesofn.archsmith.server.admin.controller;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lesofn.archsmith.server.admin.Application;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.web.client.RestClient;

/**
 * RestClient-based integration test for {@code /quartz/*} endpoints. Walks the full lifecycle:
 * login → add → list → run → log → pause → resume → delete.
 *
 * @author sofn
 */
@SpringBootTest(
        classes = Application.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class QuartzJobIntegrationTest {

    @LocalServerPort int port;

    private RestClient rest;
    private String accessToken;
    private static final ObjectMapper M = new ObjectMapper();
    private Long createdJobId;

    @BeforeAll
    @SuppressWarnings("unchecked")
    void setup() throws Exception {
        rest = RestClient.builder().baseUrl("http://localhost:" + port).build();
        String resp =
                rest.post()
                        .uri("/login")
                        .header("Content-Type", "application/json")
                        .body(Map.of("username", "admin", "password", "admin123"))
                        .retrieve()
                        .body(String.class);
        Map<String, Object> body = M.readValue(resp, Map.class);
        Map<String, Object> data = (Map<String, Object>) body.get("data");
        accessToken = (String) data.get("accessToken");
        assertNotNull(accessToken);
    }

    private Map<String, Object> post(String path, Object body) {
        try {
            String resp =
                    rest.post()
                            .uri(path)
                            .header("Content-Type", "application/json")
                            .header("Authorization", "Bearer " + accessToken)
                            .body(body == null ? "" : M.writeValueAsString(body))
                            .retrieve()
                            .body(String.class);
            return M.readValue(resp, Map.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Map<String, Object> put(String path, Object body) {
        try {
            String resp =
                    rest.put()
                            .uri(path)
                            .header("Content-Type", "application/json")
                            .header("Authorization", "Bearer " + accessToken)
                            .body(body == null ? "" : M.writeValueAsString(body))
                            .retrieve()
                            .body(String.class);
            return M.readValue(resp, Map.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Map<String, Object> delete(String path) {
        try {
            String resp =
                    rest.delete()
                            .uri(path)
                            .header("Authorization", "Bearer " + accessToken)
                            .retrieve()
                            .body(String.class);
            return M.readValue(resp, Map.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @Order(1)
    void validateCron() {
        Map<String, Object> good = post("/quartz/validate-cron", Map.of("cron", "0/30 * * * * ?"));
        assertEquals(0, good.get("code"));
        assertEquals(Boolean.TRUE, good.get("data"));

        Map<String, Object> bad = post("/quartz/validate-cron", Map.of("cron", "not-a-cron"));
        assertEquals(0, bad.get("code"));
        assertEquals(Boolean.FALSE, bad.get("data"));
    }

    @Test
    @Order(2)
    @SuppressWarnings("unchecked")
    void addJob() {
        Map<String, Object> resp =
                post(
                        "/quartz/add",
                        Map.of(
                                "jobName", "it-demo-hello",
                                "jobGroup", "DEFAULT",
                                "description", "integration test demo job",
                                "beanName", "demoQuartzJob",
                                "methodName", "echo",
                                "methodParams", "[\"hello-from-it\"]",
                                "cron", "0 0 1 1 1 ? 2099",
                                "misfirePolicy", 1,
                                "concurrent", false));
        assertEquals(0, resp.get("code"), "add failed: " + resp);
        createdJobId = ((Number) resp.get("data")).longValue();
        assertNotNull(createdJobId);
    }

    @Test
    @Order(3)
    @SuppressWarnings("unchecked")
    void listJobs() {
        Map<String, Object> resp =
                post(
                        "/quartz/list",
                        Map.of("jobName", "it-demo", "currentPage", 1, "pageSize", 10));
        assertEquals(0, resp.get("code"));
        Map<String, Object> data = (Map<String, Object>) resp.get("data");
        List<Map<String, Object>> list = (List<Map<String, Object>>) data.get("list");
        assertTrue(
                list.stream().anyMatch(j -> "it-demo-hello".equals(j.get("jobName"))),
                "created job must appear in list");
    }

    @Test
    @Order(4)
    void runJobAndCheckLog() throws Exception {
        Map<String, Object> runResp = post("/quartz/run/" + createdJobId, null);
        assertEquals(0, runResp.get("code"), "run failed: " + runResp);

        // poll the log endpoint up to 5 seconds
        long deadline = System.currentTimeMillis() + 5_000;
        boolean found = false;
        while (System.currentTimeMillis() < deadline) {
            @SuppressWarnings("unchecked")
            Map<String, Object> logResp =
                    post(
                            "/quartz/log/list",
                            Map.of("jobId", createdJobId, "currentPage", 1, "pageSize", 10));
            assertEquals(0, logResp.get("code"));
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) logResp.get("data");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> list = (List<Map<String, Object>>) data.get("list");
            if (!list.isEmpty()) {
                Map<String, Object> entry = list.getFirst();
                assertEquals(0, ((Number) entry.get("status")).intValue(), "should be SUCCESS");
                found = true;
                break;
            }
            Thread.sleep(200);
        }
        assertTrue(found, "log row must be persisted within 5 seconds");
    }

    @Test
    @Order(5)
    void updateJob() {
        Map<String, Object> resp =
                put(
                        "/quartz/update/" + createdJobId,
                        Map.of(
                                "jobName", "it-demo-hello",
                                "jobGroup", "DEFAULT",
                                "description", "updated description",
                                "beanName", "demoQuartzJob",
                                "methodName", "echo",
                                "methodParams", "[\"updated\"]",
                                "cron", "0 0 2 1 1 ? 2099",
                                "misfirePolicy", 1,
                                "concurrent", false));
        assertEquals(0, resp.get("code"));
    }

    @Test
    @Order(6)
    void pauseAndResume() {
        assertEquals(0, post("/quartz/pause/" + createdJobId, null).get("code"));
        assertEquals(0, post("/quartz/resume/" + createdJobId, null).get("code"));
    }

    @Test
    @Order(7)
    void deleteJob() {
        Map<String, Object> resp = delete("/quartz/delete/" + createdJobId);
        assertEquals(0, resp.get("code"));
    }
}
