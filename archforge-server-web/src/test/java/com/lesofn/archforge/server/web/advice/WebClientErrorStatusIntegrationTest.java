package com.lesofn.archforge.server.web.advice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.lesofn.archforge.server.web.AbstractWebIntegrationTest;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/** Unmapped paths and wrong methods are client errors: 404 / 405 problem details, never 500. */
@Tag("slow")
class WebClientErrorStatusIntegrationTest extends AbstractWebIntegrationTest {

    private final HttpClient http = HttpClient.newHttpClient();

    @Test
    void unmappedPathIsNotFound() throws Exception {
        HttpResponse<String> response = send("GET", "/definitely-not-mapped");

        assertEquals(404, response.statusCode(), response.body());
        assertTrue(response.body().contains("\"status\":404"), response.body());
    }

    @Test
    void wrongMethodOnARealEndpointIsMethodNotAllowed() throws Exception {
        // /web/login is public (no auth interceptor) and POST-only
        HttpResponse<String> response = send("GET", "/web/login");

        assertEquals(405, response.statusCode(), response.body());
    }

    private HttpResponse<String> send(String method, String path) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                .method(method, HttpRequest.BodyPublishers.noBody())
                .build();
        return http.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
