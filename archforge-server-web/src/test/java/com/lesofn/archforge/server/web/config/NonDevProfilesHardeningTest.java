package com.lesofn.archforge.server.web.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.boot.origin.OriginTrackedValue;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;

/**
 * The 2026-10-07 review findings were all "config that quietly says the opposite of the standard". Pin the staging and
 * prod profiles so a refactor cannot bring back a public actuator, payload logging or an unauthenticated Redis.
 */
@Tag("contract")
class NonDevProfilesHardeningTest {

    private static List<PropertySource<?>> load(String file) throws IOException {
        return new YamlPropertySourceLoader().load(file, new ClassPathResource(file));
    }

    /** First document that defines the key (profile files may hold several {@code ---} documents). */
    private static String text(List<PropertySource<?>> documents, String key) {
        for (PropertySource<?> document : documents) {
            Object value = document.getProperty(key);
            if (value != null) {
                return String.valueOf(value instanceof OriginTrackedValue tracked ? tracked.getValue() : value);
            }
        }
        return fail("missing " + key);
    }

    @Test
    void stagingAndProdServeTheActuatorOnAManagementPortWithoutMetricsOrEnv() throws IOException {
        for (String file : List.of("application-staging.yaml", "application-prod.yaml")) {
            List<PropertySource<?>> props = load(file);

            assertEquals("${MANAGEMENT_SERVER_PORT:8091}", text(props, "management.server.port"), file);
            assertEquals("health,info,prometheus", text(props, "management.endpoints.web.exposure.include"), file);
        }
    }

    @Test
    void stagingAndProdDoNotLogRequestOrResponseBodies() throws IOException {
        for (String file : List.of("application-staging.yaml", "application-prod.yaml")) {
            List<PropertySource<?>> props = load(file);

            assertEquals("false", text(props, "arch-forge.request-log.include-request-payload"), file);
            assertEquals("false", text(props, "arch-forge.request-log.include-response-payload"), file);
        }
    }

    @Test
    void prodAuthenticatesToRedis() throws IOException {
        assertEquals("${REDIS_PASSWORD:}", text(load("application-prod.yaml"), "spring.data.redis.password"));
    }
}
