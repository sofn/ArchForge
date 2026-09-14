package com.lesofn.archforge.server.web.contract;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.lesofn.archforge.server.web.AbstractWebIntegrationTest;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Exports the live springdoc OpenAPI document of server-web so the
 * {@code generateOpenApi} task can merge it with the server-admin export into
 * spec/openapi.yaml. Output: build/openapi/live-openapi.json.
 */
@Tag("contract")
@Tag("slow")
class OpenApiSnapshotTest extends AbstractWebIntegrationTest {

    @Test
    void exportLiveOpenApiDocument() throws Exception {
        String body = restClient().get().uri("/v3/api-docs").retrieve().body(String.class);

        assertNotNull(body);
        // springdoc's api-docs endpoint returns byte[]; server-web's custom
        // JacksonJsonHttpMessageConverter claims application/json for byte[] too, so the
        // response arrives as a base64 JSON string ("eyJ...") instead of raw JSON.
        // Decode it back before validating — prod disables api-docs, so this is test-only.
        if (body.startsWith("\"") && body.endsWith("\"")) {
            body = new String(java.util.Base64.getDecoder()
                    .decode(body.substring(1, body.length() - 1)), java.nio.charset.StandardCharsets.UTF_8);
        }
        assertTrue(
                body.contains("\"openapi\""),
                "response must be an OpenAPI document, got: " + body.substring(0, Math.min(body.length(), 500)));

        Path output = Path.of("build", "openapi", "live-openapi.json");
        Files.createDirectories(output.getParent());
        Files.writeString(output, body);
    }
}
