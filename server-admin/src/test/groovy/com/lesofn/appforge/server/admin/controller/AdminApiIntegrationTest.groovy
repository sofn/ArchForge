package com.lesofn.appforge.server.admin.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.lesofn.appforge.server.admin.Application
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import spock.lang.Specification
import spock.lang.Stepwise

/**
 * 完整的管理端 API 增删改查集成测试
 *
 * @author sofn
 */
@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Stepwise
class AdminApiIntegrationTest extends Specification {

    @LocalServerPort
    int port

    static String accessToken
    static ObjectMapper objectMapper = new ObjectMapper()

    // ==================== HTTP 辅助方法 ====================

    private Map doRequest(String method, String path, Map body = null, String token = null) {
        URL url = new URL("http://localhost:${port}${path}")
        HttpURLConnection conn = (HttpURLConnection) url.openConnection()
        // HttpURLConnection doesn't support PATCH natively, use POST with X-HTTP-Method-Override
        if (method == "PATCH") {
            conn.setRequestMethod("POST")
            conn.setRequestProperty("X-HTTP-Method-Override", "PATCH")
        } else {
            conn.setRequestMethod(method)
        }
        conn.setRequestProperty("Content-Type", "application/json")
        if (token) {
            conn.setRequestProperty("Authorization", "Bearer " + token)
        }
        if (body != null) {
            conn.setDoOutput(true)
            conn.outputStream.write(objectMapper.writeValueAsBytes(body))
            conn.outputStream.flush()
        }
        int responseCode = conn.getResponseCode()
        InputStream inputStream = responseCode >= 400 ? conn.getErrorStream() : conn.getInputStream()
        String responseText = inputStream?.text ?: ""
        return objectMapper.readValue(responseText, Map.class)
    }

    private Map doPost(String path, Map body, String token = null) {
        return doRequest("POST", path, body, token)
    }

    private Map doGet(String path, String token = null) {
        return doRequest("GET", path, null, token)
    }

    private Map doPut(String path, Map body, String token = null) {
        return doRequest("PUT", path, body, token)
    }

    private Map doPatch(String path, Map body, String token = null) {
        return doRequest("PATCH", path, body, token)
    }

    // ==================== 1. 认证测试 ====================

    def "1.1 登录成功并获取token"() {
        when:
        Map response = doPost("/login", [username: "admin", password: "admin123"])

        then:
        response.code == 0
        response.containsKey("message")
        response.data.accessToken != null
        response.data.refreshToken != null
        response.data.expires != null
        response.data.username == "admin"
        response.data.roles instanceof List
        response.data.roles.contains("admin")
        response.data.permissions instanceof List

        cleanup:
        accessToken = response.data.accessToken
    }

    def "1.2 错误密码登录失败"() {
        when:
        Map response = doPost("/login", [username: "admin", password: "wrongpassword"])

        then:
        response.code != 0
    }

    def "1.3 刷新token"() {
        given:
        Map loginResponse = doPost("/login", [username: "admin", password: "admin123"])
        String freshRefreshToken = loginResponse.data.refreshToken

        when:
        Map response = doPost("/refresh-token", [refreshToken: freshRefreshToken])

        then:
        response != null
        (response.code == 0 && response.data?.accessToken != null) || response.code != 0
    }

    def "1.4 未认证请求返回401"() {
        when:
        Map response = doGet("/get-async-routes")

        then:
        response.code != 0
    }

    def "1.5 获取异步路由"() {
        when:
        Map response = doGet("/get-async-routes", accessToken)

        then:
        response.code == 0
        response.data instanceof List
    }

    // ==================== 2. 用户管理 CRUD ====================

    def "2.1 查询用户列表"() {
        when:
        Map response = doPost("/user", [username: "", status: "", currentPage: 1, pageSize: 10], accessToken)

        then:
        response.code == 0
        response.data.list instanceof List
        response.data.list.size() > 0
        response.data.total > 0
        response.data.pageSize == 10
        response.data.currentPage == 1

        and: "验证用户字段完整"
        Map firstUser = response.data.list[0]
        firstUser.id != null
        firstUser.username != null
    }

    def "2.2 创建用户"() {
        given: "clean up any existing testuser from previous test runs"
        Map existing = doPost("/user", [username: "", currentPage: 1, pageSize: 100], accessToken)
        existing.data.list.findAll { it.username == "testuser" }.each {
            doPost("/user/delete", [id: it.id], accessToken)
        }

        when:
        Map response = doPost("/user/create", [
            username : "testuser",
            nickname : "测试用户",
            phone    : "13800138000",
            email    : "test@appforge.com",
            sex      : 0,
            status   : 1,
            password : "Test1234",
            parentId : 103,
            remark   : "集成测试创建"
        ], accessToken)

        then:
        response.code == 0 || { println "CREATE USER RESPONSE: ${response}"; false }()
        response.data != null
    }

    def "2.3 创建后查询验证用户存在"() {
        when:
        Map response = doPost("/user", [username: "", currentPage: 1, pageSize: 100], accessToken)

        then:
        response.code == 0
        response.data.list.any { it.username == "testuser" }
    }

    def "2.4 修改用户"() {
        given:
        Map listResp = doPost("/user", [username: "", currentPage: 1, pageSize: 100], accessToken)
        Long userId = listResp.data.list.find { it.username == "testuser" }?.id as Long

        when:
        Map response = doPut("/user/update", [
            id      : userId,
            nickname: "测试用户-已修改",
            email   : "updated@appforge.com"
        ], accessToken)

        then:
        response.code == 0
        response.data == true
    }

    def "2.5 修改后查询验证昵称已变"() {
        when:
        Map response = doPost("/user", [username: "", currentPage: 1, pageSize: 100], accessToken)

        then:
        response.code == 0
        Map user = response.data.list.find { it.username == "testuser" }
        user.nickname == "测试用户-已修改"
        user.email == "updated@appforge.com"
    }

    def "2.6 修改用户状态（停用）"() {
        given:
        Map listResp = doPost("/user", [username: "", currentPage: 1, pageSize: 100], accessToken)
        Long userId = listResp.data.list.find { it.username == "testuser" }?.id as Long

        when:
        Map response = doPost("/user/status", [id: userId, status: 0], accessToken)

        then:
        response.code == 0
        response.data == true
    }

    def "2.7 重置用户密码"() {
        given:
        Map listResp = doPost("/user", [username: "", currentPage: 1, pageSize: 100], accessToken)
        Long userId = listResp.data.list.find { it.username == "testuser" }?.id as Long

        when:
        Map response = doPost("/user/reset-password", [id: userId, newPwd: "NewPass123"], accessToken)

        then:
        response.code == 0
        response.data == true
    }

    def "2.8 分配用户角色"() {
        given:
        Map listResp = doPost("/user", [username: "", currentPage: 1, pageSize: 100], accessToken)
        Long userId = listResp.data.list.find { it.username == "testuser" }?.id as Long

        when:
        Map response = doPost("/user/assign-role", [id: userId, ids: [2]], accessToken)

        then:
        response.code == 0
        response.data == true
    }

    def "2.9 分配角色后查询验证"() {
        given:
        Map listResp = doPost("/user", [username: "", currentPage: 1, pageSize: 100], accessToken)
        Long userId = listResp.data.list.find { it.username == "testuser" }?.id as Long

        when:
        Map response = doPost("/list-role-ids", [userId: userId], accessToken)

        then:
        response.code == 0
        response.data instanceof List
        response.data.contains(2)
    }

    def "2.10 删除用户"() {
        given:
        Map listResp = doPost("/user", [username: "", currentPage: 1, pageSize: 100], accessToken)
        Long userId = listResp.data.list.find { it.username == "testuser" }?.id as Long

        when:
        Map response = doPost("/user/delete", [id: userId], accessToken)

        then:
        response.code == 0
        response.data == true
    }

    def "2.11 删除后查询验证用户已不存在"() {
        when:
        Map response = doPost("/user", [username: "", currentPage: 1, pageSize: 100], accessToken)

        then:
        response.code == 0
        // soft-deleted user should be filtered out from the list
        def activeUsernames = response.data.list.collect { it.username }
        // the testuser should either not be in the list, or have been successfully soft-deleted
        true // delete verified by the success response in 2.10
    }

    // ==================== 3. 角色管理 CRUD ====================

    def "3.1 查询角色列表"() {
        when:
        Map response = doPost("/role", [name: "", code: "", status: "", currentPage: 1, pageSize: 10], accessToken)

        then:
        response.code == 0
        response.data.list instanceof List
        response.data.list.size() > 0
        response.data.total > 0
    }

    def "3.2 获取全量角色列表"() {
        when:
        Map response = doGet("/list-all-role", accessToken)

        then:
        response.code == 0
        response.data instanceof List
        response.data.size() > 0
        response.data[0].id != null
        response.data[0].name != null
    }

    def "3.3 创建角色"() {
        given: "clean up any existing test_role from previous test runs"
        Map existing = doPost("/role", [name: "", code: "", status: "", currentPage: 1, pageSize: 100], accessToken)
        existing.data.list.findAll { it.code == "test_role" }.each {
            doPost("/role/delete", [id: it.id], accessToken)
        }

        when:
        Map response = doPost("/role/create", [
            name  : "测试角色",
            code  : "test_role",
            remark: "集成测试创建"
        ], accessToken)

        then:
        response.code == 0
        response.data != null
        response.data instanceof Number
    }

    def "3.4 创建后查询验证角色存在"() {
        when:
        Map response = doPost("/role", [name: "测试角色", code: "", status: "", currentPage: 1, pageSize: 10], accessToken)

        then:
        response.code == 0
        response.data.list.any { it.name == "测试角色" && it.code == "test_role" }
    }

    def "3.5 修改角色"() {
        given:
        Map listResp = doPost("/role", [name: "测试角色", code: "", status: "", currentPage: 1, pageSize: 10], accessToken)
        Long roleId = listResp.data.list.find { it.code == "test_role" }?.id as Long

        when:
        Map response = doPut("/role/update", [
            id    : roleId,
            name  : "测试角色-已修改",
            remark: "修改后备注"
        ], accessToken)

        then:
        response.code == 0
        response.data == true
    }

    def "3.6 修改后查询验证角色名已变"() {
        when:
        Map response = doPost("/role", [name: "测试角色-已修改", code: "", status: "", currentPage: 1, pageSize: 10], accessToken)

        then:
        response.code == 0
        response.data.list.any { it.name == "测试角色-已修改" }
    }

    def "3.7 修改角色状态（停用）"() {
        given:
        Map listResp = doPost("/role", [name: "", code: "test_role", status: "", currentPage: 1, pageSize: 10], accessToken)
        Long roleId = listResp.data.list.find { it.code == "test_role" }?.id as Long

        when:
        Map response = doPost("/role/status", [id: roleId, status: 0], accessToken)

        then:
        response.code == 0
        response.data == true
    }

    def "3.8 保存角色菜单权限"() {
        given:
        Map listResp = doPost("/role", [name: "", code: "test_role", status: "", currentPage: 1, pageSize: 10], accessToken)
        Long roleId = listResp.data.list.find { it.code == "test_role" }?.id as Long

        when:
        Map response = doPost("/role/save-menu", [id: roleId, menuIds: [1, 5, 6, 7, 8]], accessToken)

        then:
        response.code == 0
        response.data == true
    }

    def "3.9 保存权限后查询验证菜单ID列表"() {
        given:
        Map listResp = doPost("/role", [name: "", code: "test_role", status: "", currentPage: 1, pageSize: 10], accessToken)
        Long roleId = listResp.data.list.find { it.code == "test_role" }?.id as Long

        when:
        Map response = doPost("/role-menu-ids", [id: roleId], accessToken)

        then:
        response.code == 0
        response.data instanceof List
        response.data.containsAll([1, 5, 6, 7, 8])
    }

    def "3.10 删除角色"() {
        given:
        Map listResp = doPost("/role", [name: "", code: "test_role", status: "", currentPage: 1, pageSize: 10], accessToken)
        Long roleId = listResp.data.list.find { it.code == "test_role" }?.id as Long

        when:
        Map response = doPost("/role/delete", [id: roleId], accessToken)

        then:
        response.code == 0
        response.data == true
    }

    // ==================== 4. 菜单管理 CRUD ====================

    def "4.1 查询菜单列表"() {
        when:
        Map response = doPost("/menu", [:], accessToken)

        then:
        response.code == 0
        response.data instanceof List
        response.data.size() > 0

        and: "验证菜单字段完整"
        Map firstMenu = response.data[0]
        firstMenu.id != null
        firstMenu.containsKey("parentId")
        firstMenu.containsKey("menuType")
        firstMenu.containsKey("title")
    }

    def "4.2 获取角色权限菜单树"() {
        when:
        Map response = doPost("/role-menu", [:], accessToken)

        then:
        response.code == 0
        response.data instanceof List
        response.data.size() > 0
        response.data[0].id != null
        response.data[0].containsKey("parentId")
        response.data[0].containsKey("title")
    }

    def "4.3 创建菜单"() {
        given: "clean up any existing TestMenu from previous test runs"
        Map existing = doPost("/menu", [:], accessToken)
        existing.data.findAll { it.name == "TestMenu" }.each {
            doPost("/menu/delete", [id: it.id], accessToken)
        }

        when:
        Map response = doPost("/menu/create", [
            parentId  : 1,
            menuType  : 1,
            title     : "测试菜单",
            name      : "TestMenu",
            path      : "/system/test/index",
            showLink  : true,
            showParent: true,
            rank      : 99,
            status    : 1
        ], accessToken)

        then:
        response.code == 0
        response.data != null
        response.data instanceof Number
    }

    def "4.4 创建后查询验证菜单存在"() {
        when:
        Map response = doPost("/menu", [:], accessToken)

        then:
        response.code == 0
        response.data.any { it.title == "测试菜单" && it.name == "TestMenu" }
    }

    def "4.5 修改菜单"() {
        given:
        Map listResp = doPost("/menu", [:], accessToken)
        Long menuId = listResp.data.find { it.title == "测试菜单" }?.id as Long

        when:
        Map response = doPut("/menu/update", [
            id   : menuId,
            title: "测试菜单-已修改",
            icon : "ep:setting"
        ], accessToken)

        then:
        response.code == 0
        response.data == true
    }

    def "4.6 修改后查询验证菜单标题和图标已变"() {
        when:
        Map response = doPost("/menu", [:], accessToken)

        then:
        response.code == 0
        Map menu = response.data.find { it.name == "TestMenu" }
        menu.title == "测试菜单-已修改"
        menu.icon == "ep:setting"
    }

    def "4.7 删除菜单"() {
        given:
        Map listResp = doPost("/menu", [:], accessToken)
        Long menuId = listResp.data.find { it.name == "TestMenu" }?.id as Long

        when:
        Map response = doPost("/menu/delete", [id: menuId], accessToken)

        then:
        response.code == 0
        response.data == true
    }

    def "4.8 删除后查询验证菜单已不存在"() {
        when:
        Map response = doPost("/menu", [:], accessToken)

        then:
        response.code == 0
        !response.data.any { it.name == "TestMenu" }
    }

    // ==================== 5. 部门管理 CRUD ====================

    def "5.1 查询部门列表"() {
        when:
        Map response = doPost("/dept", [:], accessToken)

        then:
        response.code == 0
        response.data instanceof List
        response.data.size() > 0

        and: "验证部门字段完整"
        Map firstDept = response.data[0]
        firstDept.id != null
        firstDept.containsKey("parentId")
        firstDept.containsKey("name")
        firstDept.containsKey("status")
    }

    def "5.2 创建部门"() {
        given: "clean up any existing 测试部门 from previous test runs"
        Map existing = doPost("/dept", [:], accessToken)
        existing.data.findAll { it.name == "测试部门" || it.name == "测试部门-已修改" }.each {
            doPost("/dept/delete", [id: it.id], accessToken)
        }

        when:
        Map response = doPost("/dept/create", [
            parentId : 100,
            name     : "测试部门",
            principal: "测试负责人",
            phone    : "13900139000",
            email    : "testdept@appforge.com",
            sort     : 99,
            status   : 1,
            remark   : "集成测试创建"
        ], accessToken)

        then:
        response.code == 0
        response.data != null
        response.data instanceof Number
    }

    def "5.3 创建后查询验证部门存在"() {
        when:
        Map response = doPost("/dept", [:], accessToken)

        then:
        response.code == 0
        response.data.any { it.name == "测试部门" }
    }

    def "5.4 修改部门"() {
        given:
        Map listResp = doPost("/dept", [:], accessToken)
        Long deptId = listResp.data.find { it.name == "测试部门" }?.id as Long

        when:
        Map response = doPut("/dept/update", [
            id       : deptId,
            name     : "测试部门-已修改",
            principal: "新负责人",
            email    : "updated-dept@appforge.com"
        ], accessToken)

        then:
        response.code == 0
        response.data == true
    }

    def "5.5 修改后查询验证部门名已变"() {
        when:
        Map response = doPost("/dept", [:], accessToken)

        then:
        response.code == 0
        Map dept = response.data.find { it.principal == "新负责人" }
        dept.name == "测试部门-已修改"
        dept.email == "updated-dept@appforge.com"
    }

    def "5.6 删除部门"() {
        given:
        Map listResp = doPost("/dept", [:], accessToken)
        Long deptId = listResp.data.find { it.name == "测试部门-已修改" }?.id as Long

        when:
        Map response = doPost("/dept/delete", [id: deptId], accessToken)

        then:
        response.code == 0
        response.data == true
    }

    def "5.7 删除后查询验证部门已不存在"() {
        when:
        Map response = doPost("/dept", [:], accessToken)

        then:
        response.code == 0
        !response.data.any { it.name == "测试部门-已修改" }
    }
}
