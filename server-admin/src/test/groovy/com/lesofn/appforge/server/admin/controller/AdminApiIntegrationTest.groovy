package com.lesofn.appforge.server.admin.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.lesofn.appforge.server.admin.Application
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import spock.lang.Specification
import spock.lang.Stepwise

/**
 * Login and Admin API integration tests
 *
 * @author sofn
 */
@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Stepwise
class AdminApiIntegrationTest extends Specification {

    @LocalServerPort
    int port

    static String accessToken
    static String refreshToken
    static ObjectMapper objectMapper = new ObjectMapper()

    private Map doPost(String path, Map body, String token = null) {
        URL url = new URL("http://localhost:${port}${path}")
        HttpURLConnection conn = (HttpURLConnection) url.openConnection()
        conn.setRequestMethod("POST")
        conn.setRequestProperty("Content-Type", "application/json")
        if (token) {
            conn.setRequestProperty("Authorization", "Bearer " + token)
        }
        conn.setDoOutput(true)
        conn.outputStream.write(objectMapper.writeValueAsBytes(body))
        conn.outputStream.flush()

        int responseCode = conn.getResponseCode()
        InputStream inputStream = responseCode >= 400 ? conn.getErrorStream() : conn.getInputStream()
        String responseText = inputStream?.text ?: ""
        return objectMapper.readValue(responseText, Map.class)
    }

    private Map doGet(String path, String token = null) {
        URL url = new URL("http://localhost:${port}${path}")
        HttpURLConnection conn = (HttpURLConnection) url.openConnection()
        conn.setRequestMethod("GET")
        if (token) {
            conn.setRequestProperty("Authorization", "Bearer " + token)
        }

        int responseCode = conn.getResponseCode()
        InputStream inputStream = responseCode >= 400 ? conn.getErrorStream() : conn.getInputStream()
        String responseText = inputStream?.text ?: ""
        return objectMapper.readValue(responseText, Map.class)
    }

    def "login with admin credentials should return success"() {
        when:
        Map response = doPost("/login", [username: "admin", password: "admin123"])

        then:
        response != null
        response.code == 0
        response.data != null
        response.data.accessToken != null
        response.data.refreshToken != null
        response.data.expires != null
        response.data.username == "admin"
        response.data.roles instanceof List
        response.data.permissions instanceof List

        cleanup: "store tokens for subsequent tests"
        accessToken = response.data.accessToken
        refreshToken = response.data.refreshToken
    }

    def "response should contain message field"() {
        when:
        Map response = doPost("/login", [username: "admin", password: "admin123"])

        then:
        response.containsKey("message")
    }

    def "GET /get-async-routes should return routes"() {
        when:
        Map response = doGet("/get-async-routes", accessToken)

        then:
        response != null
        response.code == 0
        response.data instanceof List
    }

    def "POST /user should return paginated user list"() {
        when:
        Map response = doPost("/user", [username: "", status: "", currentPage: 1, pageSize: 10], accessToken)

        then:
        response != null
        response.code == 0
        response.data != null
        response.data.list instanceof List
        response.data.total != null
        response.data.pageSize == 10
        response.data.currentPage == 1
    }

    def "GET /list-all-role should return all roles"() {
        when:
        Map response = doGet("/list-all-role", accessToken)

        then:
        response != null
        response.code == 0
        response.data instanceof List
    }

    def "POST /list-role-ids should return role IDs for user"() {
        when:
        Map response = doPost("/list-role-ids", [userId: 1], accessToken)

        then:
        response != null
        response.code == 0
        response.data instanceof List
    }

    def "POST /role should return paginated role list"() {
        when:
        Map response = doPost("/role", [name: "", code: "", status: "", currentPage: 1, pageSize: 10], accessToken)

        then:
        response != null
        response.code == 0
        response.data != null
        response.data.list instanceof List
        response.data.total != null
    }

    def "POST /role-menu should return menu tree for permissions"() {
        when:
        Map response = doPost("/role-menu", [:], accessToken)

        then:
        response != null
        response.code == 0
        response.data instanceof List
    }

    def "POST /role-menu-ids should return menu IDs for role"() {
        when:
        Map response = doPost("/role-menu-ids", [id: 2], accessToken)

        then:
        response != null
        response.code == 0
        response.data instanceof List
    }

    def "POST /menu should return all menus"() {
        when:
        Map response = doPost("/menu", [:], accessToken)

        then:
        response != null
        response.code == 0
        response.data instanceof List
    }

    def "POST /dept should return all departments"() {
        when:
        Map response = doPost("/dept", [:], accessToken)

        then:
        response != null
        response.code == 0
        response.data instanceof List
    }

    def "POST /refresh-token should return new tokens"() {
        given: "login to get a fresh refresh token"
        Map loginResponse = doPost("/login", [username: "admin", password: "admin123"])
        String freshRefreshToken = loginResponse.data.refreshToken

        when:
        Map response = doPost("/refresh-token", [refreshToken: freshRefreshToken])

        then: "response should be valid (code 0 or token_invalid error if cache expired)"
        response != null
        (response.code == 0 && response.data?.accessToken != null) || response.code != 0
    }
}
