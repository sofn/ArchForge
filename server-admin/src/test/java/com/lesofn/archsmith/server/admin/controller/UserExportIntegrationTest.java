package com.lesofn.archsmith.server.admin.controller;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lesofn.archsmith.server.admin.Application;
import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Map;
import org.dhatim.fastexcel.reader.ReadableWorkbook;
import org.dhatim.fastexcel.reader.Row;
import org.dhatim.fastexcel.reader.Sheet;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.web.client.RestClient;

/**
 * Integration test for {@code GET /user/export}: verifies the endpoint produces a valid xlsx stream
 * readable by FastExcel.
 *
 * @author sofn
 */
@SpringBootTest(
        classes = Application.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserExportIntegrationTest {

    @LocalServerPort int port;

    private RestClient restClient;
    private String accessToken;
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @BeforeAll
    @SuppressWarnings("unchecked")
    void setup() {
        restClient = RestClient.builder().baseUrl("http://localhost:" + port).build();
        String resp =
                restClient
                        .post()
                        .uri("/login")
                        .header("Content-Type", "application/json")
                        .body(Map.of("username", "admin", "password", "admin123"))
                        .retrieve()
                        .body(String.class);
        try {
            Map<String, Object> body = MAPPER.readValue(resp, Map.class);
            Map<String, Object> data = (Map<String, Object>) body.get("data");
            accessToken = (String) data.get("accessToken");
        } catch (Exception e) {
            throw new RuntimeException("login failed: " + resp, e);
        }
        assertNotNull(accessToken, "accessToken must be present after login");
    }

    @Test
    void exportReturnsXlsxWithUserRows() throws Exception {
        byte[] bytes =
                restClient
                        .get()
                        .uri("/user/export")
                        .header("Authorization", "Bearer " + accessToken)
                        .retrieve()
                        .body(byte[].class);

        assertNotNull(bytes);
        assertTrue(bytes.length > 0, "xlsx bytes should not be empty");

        try (ReadableWorkbook wb = new ReadableWorkbook(new ByteArrayInputStream(bytes))) {
            Sheet sheet = wb.getFirstSheet();
            assertNotNull(sheet, "first sheet must exist");
            try (java.util.stream.Stream<Row> stream = sheet.openStream()) {
                List<Row> rows = stream.toList();
                assertTrue(rows.size() >= 2, "header + at least one user row, got " + rows.size());
                Row header = rows.getFirst();
                assertEquals("ID", header.getCellAsString(0).orElse(""));
                assertEquals("Username", header.getCellAsString(1).orElse(""));
            }
        }
    }
}
