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
 * 基于 RestClient 的管理端 API 集成测试
 *
 * <p>覆盖用户、角色、部门管理的核心 CRUD 接口，使用测试用户 (testadmin) 执行操作。
 *
 * @author sofn
 */
@SpringBootTest(
        classes = Application.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RestClientIntegrationTest {

    @LocalServerPort int port;

    private RestClient restClient;
    private String accessToken;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    // 测试过程中记录的 ID
    private Long createdUserId;
    private Long createdRoleId;
    private Long createdDeptId;

    @BeforeAll
    void setup() {
        restClient = RestClient.builder().baseUrl("http://localhost:" + port).build();
    }

    // ==================== Helper Methods ====================

    @SuppressWarnings("unchecked")
    private Map<String, Object> post(String path, Map<String, Object> body) {
        String json;
        try {
            json = objectMapper.writeValueAsString(body);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        RestClient.RequestBodySpec spec =
                restClient
                        .post()
                        .uri(path)
                        .header("Content-Type", "application/json");
        if (accessToken != null) {
            spec = spec.header("Authorization", "Bearer " + accessToken);
        }
        String response = spec.body(json).retrieve().body(String.class);
        try {
            return objectMapper.readValue(response, Map.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse response: " + response, e);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> put(String path, Map<String, Object> body) {
        String json;
        try {
            json = objectMapper.writeValueAsString(body);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        String response =
                restClient
                        .put()
                        .uri(path)
                        .header("Content-Type", "application/json")
                        .header("Authorization", "Bearer " + accessToken)
                        .body(json)
                        .retrieve()
                        .body(String.class);
        try {
            return objectMapper.readValue(response, Map.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse response: " + response, e);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> get(String path) {
        String response =
                restClient
                        .get()
                        .uri(path)
                        .header("Authorization", "Bearer " + accessToken)
                        .retrieve()
                        .body(String.class);
        try {
            return objectMapper.readValue(response, Map.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse response: " + response, e);
        }
    }

    // ==================== 1. 认证 ====================

    @Test
    @Order(1)
    void login() {
        Map<String, Object> response = post("/login", Map.of("username", "admin", "password", "admin123"));
        assertEquals(0, response.get("code"));
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) response.get("data");
        assertNotNull(data.get("accessToken"));
        accessToken = (String) data.get("accessToken");
    }

    // ==================== 2. 用户管理 ====================

    @Test
    @Order(10)
    void createUser() {
        Map<String, Object> response =
                post(
                        "/user/create",
                        Map.of(
                                "username", "testadmin",
                                "nickname", "测试管理员",
                                "phone", "13900000001",
                                "email", "testadmin@archsmith.com",
                                "sex", 0,
                                "status", 1,
                                "password", "Test1234"));
        assertEquals(0, response.get("code"), "Create user failed: " + response);
    }

    @Test
    @Order(11)
    @SuppressWarnings("unchecked")
    void listUsers() {
        Map<String, Object> response =
                post("/user", Map.of("username", "", "currentPage", 1, "pageSize", 100));
        assertEquals(0, response.get("code"));
        Map<String, Object> data = (Map<String, Object>) response.get("data");
        List<Map<String, Object>> list = (List<Map<String, Object>>) data.get("list");
        assertTrue(list.stream().anyMatch(u -> "testadmin".equals(u.get("username"))));
        // 记录创建的用户ID
        createdUserId =
                list.stream()
                        .filter(u -> "testadmin".equals(u.get("username")))
                        .findFirst()
                        .map(u -> ((Number) u.get("id")).longValue())
                        .orElse(null);
        assertNotNull(createdUserId);
    }

    @Test
    @Order(12)
    void updateUser() {
        Map<String, Object> response =
                put(
                        "/user/update",
                        Map.of(
                                "id", createdUserId,
                                "nickname", "测试管理员-已修改",
                                "email", "testadmin-updated@archsmith.com"));
        assertEquals(0, response.get("code"));
    }

    @Test
    @Order(13)
    void toggleUserStatus() {
        Map<String, Object> response =
                post("/user/status", Map.of("id", createdUserId, "status", 2));
        assertEquals(0, response.get("code"));
    }

    @Test
    @Order(14)
    void resetUserPassword() {
        Map<String, Object> response =
                post("/user/reset-password", Map.of("id", createdUserId, "password", "NewPass123"));
        assertEquals(0, response.get("code"));
    }

    @Test
    @Order(19)
    void deleteUser() {
        Map<String, Object> response = post("/user/delete", Map.of("id", createdUserId));
        assertEquals(0, response.get("code"));
    }

    // ==================== 3. 角色管理 ====================

    @Test
    @Order(20)
    void createRole() {
        Map<String, Object> response =
                post(
                        "/role/create",
                        Map.of(
                                "name", "RestClient测试角色",
                                "code", "restclient_test_role",
                                "status", 1));
        assertEquals(0, response.get("code"), "Create role failed: " + response);
    }

    @Test
    @Order(21)
    @SuppressWarnings("unchecked")
    void listRoles() {
        Map<String, Object> response =
                post("/role", Map.of("currentPage", 1, "pageSize", 100));
        assertEquals(0, response.get("code"));
        Map<String, Object> data = (Map<String, Object>) response.get("data");
        List<Map<String, Object>> list = (List<Map<String, Object>>) data.get("list");
        assertTrue(
                list.stream().anyMatch(r -> "restclient_test_role".equals(r.get("code"))),
                "Role list: " + list);
        createdRoleId =
                list.stream()
                        .filter(r -> "restclient_test_role".equals(r.get("code")))
                        .findFirst()
                        .map(r -> ((Number) r.get("id")).longValue())
                        .orElse(null);
        assertNotNull(createdRoleId);
    }

    @Test
    @Order(22)
    void updateRole() {
        Map<String, Object> response =
                put(
                        "/role/update",
                        Map.of(
                                "id", createdRoleId,
                                "name", "RestClient测试角色-已修改",
                                "code", "restclient_test_role",
                                "status", 1));
        assertEquals(0, response.get("code"));
    }

    @Test
    @Order(29)
    void deleteRole() {
        Map<String, Object> response = post("/role/delete", Map.of("id", createdRoleId));
        assertEquals(0, response.get("code"));
    }

    // ==================== 4. 部门管理 ====================

    @Test
    @Order(30)
    void createDept() {
        Map<String, Object> response =
                post(
                        "/dept/create",
                        Map.of(
                                "name", "测试部门",
                                "parentId", 100,
                                "principal", "测试负责人",
                                "email", "testdept@archsmith.com",
                                "phone", "13900000099",
                                "status", 1,
                                "sort", 99));
        assertEquals(0, response.get("code"), "Create dept failed: " + response);
    }

    @Test
    @Order(31)
    @SuppressWarnings("unchecked")
    void listDepts() {
        Map<String, Object> response = post("/dept", Map.of());
        assertEquals(0, response.get("code"));
        List<Map<String, Object>> data =
                (List<Map<String, Object>>) response.get("data");
        assertTrue(data.stream().anyMatch(d -> "测试部门".equals(d.get("name"))));
        createdDeptId =
                data.stream()
                        .filter(d -> "测试部门".equals(d.get("name")))
                        .findFirst()
                        .map(d -> ((Number) d.get("id")).longValue())
                        .orElse(null);
        assertNotNull(createdDeptId);
    }

    @Test
    @Order(32)
    void updateDept() {
        Map<String, Object> response =
                put(
                        "/dept/update",
                        Map.of(
                                "id", createdDeptId,
                                "name", "测试部门-已修改",
                                "principal", "新负责人",
                                "email", "updated-dept@archsmith.com"));
        assertEquals(0, response.get("code"));
    }

    @Test
    @Order(39)
    void deleteDept() {
        Map<String, Object> response = post("/dept/delete", Map.of("id", createdDeptId));
        assertEquals(0, response.get("code"));
    }
}
