package com.lesofn.archforge.server.admin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.lesofn.archforge.common.persistence.testsupport.AbstractIntegrationTest;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalManagementPort;
import org.springframework.boot.test.web.server.LocalServerPort;

/**
 * Production shape of the actuator: metrics on a separate management port (never published, never behind the
 * frontend nginx {@code /api} proxy), orchestrator probes {@code /livez} / {@code /readyz} on the business port
 * without a login. The property overrides mirror the {@code management} block of {@code application-prod.yaml}.
 */
@SpringBootTest(classes = {
        Application.class
}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "management.server.port=0",
        "management.endpoints.web.exposure.include=health,info,prometheus"
})
@Tag("slow")
class ActuatorEndpointsIntegrationTest extends AbstractIntegrationTest {

    private final HttpClient http = HttpClient.newHttpClient();

    @LocalServerPort
    int port;

    @LocalManagementPort
    int managementPort;

    @Test
    void probesAreOnTheBusinessPortAndNeedNoLogin() throws Exception {
        for (String probe : new String[] {
                "/livez", "/readyz"
        }) {
            HttpResponse<String> response = get(port, probe);

            assertEquals(200, response.statusCode(), probe + " -> " + response.body());
            assertTrue(response.body().contains("\"UP\""), response.body());
        }
    }

    @Test
    void prometheusIsNotReachableOnTheBusinessPort() throws Exception {
        assertEquals(404, get(port, "/actuator/prometheus").statusCode());
        assertEquals(404, get(port, "/actuator/health").statusCode());
    }

    @Test
    void metricsAndHealthGroupsLiveOnTheManagementPort() throws Exception {
        HttpResponse<String> prometheus = get(managementPort, "/actuator/prometheus");
        assertEquals(200, prometheus.statusCode());
        assertTrue(prometheus.body().contains("jvm_memory_used_bytes"), "scrape payload missing JVM metrics");

        assertEquals(200, get(managementPort, "/actuator/health/liveness").statusCode());
        assertEquals(200, get(managementPort, "/actuator/health/readiness").statusCode());
    }

    private HttpResponse<String> get(int onPort, String path) throws Exception {
        return http.send(HttpRequest.newBuilder(URI.create("http://localhost:" + onPort + path)).GET().build(),
                HttpResponse.BodyHandlers.ofString());
    }
}
