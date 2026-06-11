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
 * Integration tests proving that {@code QueryHelp} correctly translates {@code @Query}-annotated
 * criteria DTOs into JPA predicates via real HTTP calls.
 *
 * <p>Covered operators:
 *
 * <ul>
 *   <li>{@code INNER_LIKE} — username filter on {@code POST /user}
 *   <li>blurry multi-field LIKE — blurry filter on {@code POST /user}
 *   <li>{@code EQUAL} — status filter on {@code POST /user}
 *   <li>{@code INNER_LIKE} — jobName filter on {@code POST /quartz/list}
 * </ul>
 *
 * @author sofn
 */
@SpringBootTest(
        classes = Application.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class QueryHelpIntegrationTest {

    @LocalServerPort int port;

    private RestClient rest;
    private String accessToken;
    private static final ObjectMapper M = new ObjectMapper();

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
        assertNotNull(accessToken, "login must return a valid token");
    }

    @SuppressWarnings("unchecked")
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

    // =========================================================
    // /user — INNER_LIKE on username
    // =========================================================

    @Test
    @Order(1)
    @SuppressWarnings("unchecked")
    void searchUserByExactUsernameReturnsAdmin() {
        Map<String, Object> resp =
                post("/user", Map.of("username", "admin", "currentPage", 1, "pageSize", 10));
        assertEquals(0, resp.get("code"), "response code must be 0");

        Map<String, Object> data = (Map<String, Object>) resp.get("data");
        List<Map<String, Object>> list = (List<Map<String, Object>>) data.get("list");
        assertFalse(list.isEmpty(), "result must be non-empty");
        assertTrue(
                list.stream().anyMatch(u -> "admin".equals(u.get("username"))),
                "admin user must be in the result");
    }

    @Test
    @Order(2)
    @SuppressWarnings("unchecked")
    void innerLikeUsernameExcludesNonMatchingUsers() {
        // "zzz_no_match" should not match any seeded user
        Map<String, Object> resp =
                post(
                        "/user",
                        Map.of("username", "zzz_no_match", "currentPage", 1, "pageSize", 10));
        assertEquals(0, resp.get("code"));

        Map<String, Object> data = (Map<String, Object>) resp.get("data");
        List<Map<String, Object>> list = (List<Map<String, Object>>) data.get("list");
        assertTrue(list.isEmpty(), "no user should match the query");
    }

    // =========================================================
    // /user — blurry (multi-field LIKE: username, nickname, email)
    // =========================================================

    @Test
    @Order(3)
    @SuppressWarnings("unchecked")
    void blurrySearchMatchesAcrossFields() {
        // seeded admin: username="admin", nickname="Admin", email="admin@archsmith.com"
        // "dmin" matches all three
        Map<String, Object> resp =
                post("/user", Map.of("blurry", "dmin", "currentPage", 1, "pageSize", 50));
        assertEquals(0, resp.get("code"));

        Map<String, Object> data = (Map<String, Object>) resp.get("data");
        List<Map<String, Object>> list = (List<Map<String, Object>>) data.get("list");
        assertFalse(list.isEmpty(), "blurry 'dmin' must match admin via username/nickname/email");
        assertTrue(
                list.stream().anyMatch(u -> "admin".equals(u.get("username"))),
                "admin must be in blurry result");
    }

    // =========================================================
    // /user — EQUAL on status
    // =========================================================

    @Test
    @Order(4)
    @SuppressWarnings("unchecked")
    void searchByStatusFiltersActive() {
        // status=1 means active; seeded admin has status=1
        Map<String, Object> resp =
                post("/user", Map.of("status", "1", "currentPage", 1, "pageSize", 100));
        assertEquals(0, resp.get("code"));

        Map<String, Object> data = (Map<String, Object>) resp.get("data");
        List<Map<String, Object>> list = (List<Map<String, Object>>) data.get("list");
        assertFalse(list.isEmpty(), "active users must be returned");
        // all returned rows must be active
        assertTrue(
                list.stream().allMatch(u -> Integer.valueOf(1).equals(u.get("status"))),
                "all returned users must have status=1");
    }

    // =========================================================
    // /quartz/list — INNER_LIKE on jobName (proves Task 3 refactor)
    // =========================================================

    @Test
    @Order(5)
    @SuppressWarnings("unchecked")
    void quartzListFiltersByJobNameInnerLike() {
        // seeded demo-hello from data-admin-quartz.sql
        Map<String, Object> resp =
                post("/quartz/list", Map.of("jobName", "demo", "currentPage", 1, "pageSize", 10));
        assertEquals(0, resp.get("code"));

        Map<String, Object> data = (Map<String, Object>) resp.get("data");
        List<Map<String, Object>> list = (List<Map<String, Object>>) data.get("list");
        assertFalse(list.isEmpty(), "INNER_LIKE on 'demo' must match demo-hello");
        assertTrue(
                list.stream().anyMatch(j -> "demo-hello".equals(j.get("jobName"))),
                "demo-hello job must appear in the filtered list");
    }

    @Test
    @Order(6)
    @SuppressWarnings("unchecked")
    void quartzListNoMatchReturnsEmptyPage() {
        Map<String, Object> resp =
                post(
                        "/quartz/list",
                        Map.of("jobName", "zzz_no_such_job", "currentPage", 1, "pageSize", 10));
        assertEquals(0, resp.get("code"));

        Map<String, Object> data = (Map<String, Object>) resp.get("data");
        List<Map<String, Object>> list = (List<Map<String, Object>>) data.get("list");
        assertTrue(list.isEmpty(), "non-matching jobName must return empty list");
    }
}
