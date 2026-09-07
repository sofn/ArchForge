package com.lesofn.archforge.server.admin.contract;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.lesofn.archforge.server.admin.Application;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.web.client.RestClient;

/**
 * Exports the live springdoc OpenAPI document so CI can diff it against the canonical contract
 * in spec/openapi.yaml (M3.1). Output: build/openapi/live-openapi.json.
 */
@Tag("contract")
@Tag("slow")
@SpringBootTest(
        classes = Application.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OpenApiSnapshotTest {

    @LocalServerPort
    int port;

    @Test
    void exportLiveOpenApiDocument() throws Exception {
        String body = RestClient.create()
                .get()
                .uri("http://localhost:" + port + "/v3/api-docs")
                .retrieve()
                .body(String.class);

        assertNotNull(body);
        assertTrue(body.contains("\"openapi\""), "response must be an OpenAPI document");

        Path output = Path.of("build", "openapi", "live-openapi.json");
        Files.createDirectories(output.getParent());
        Files.writeString(output, body);
    }
}
