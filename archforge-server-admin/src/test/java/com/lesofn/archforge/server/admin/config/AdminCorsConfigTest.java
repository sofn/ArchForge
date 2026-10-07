package com.lesofn.archforge.server.admin.config;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.lesofn.archforge.infrastructure.config.ArchForgeProperties;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

/** Only local profiles may fall back to "allow every origin"; staging used to inherit it silently (SEC-M8). */
class AdminCorsConfigTest {

    private static AdminCorsConfig config(List<String> origins, String... profiles) {
        ArchForgeProperties properties = new ArchForgeProperties();
        properties.getCors().setAllowedOrigins(origins);
        MockEnvironment environment = new MockEnvironment();
        environment.setActiveProfiles(profiles);
        return new AdminCorsConfig(properties, environment);
    }

    @Test
    void stagingWithoutExplicitOriginsRefusesToStart() {
        assertThrows(IllegalStateException.class, () -> config(List.of(), "staging").corsFilter());
        assertThrows(IllegalStateException.class, () -> config(List.of("*"), "staging").corsFilter());
    }

    @Test
    void prodStillRequiresExplicitOrigins() {
        assertThrows(IllegalStateException.class, () -> config(List.of(), "prod").corsFilter());
    }

    @Test
    void localProfilesMayStayOpen() {
        assertDoesNotThrow(() -> config(List.of(), "dev").corsFilter());
        assertDoesNotThrow(() -> config(List.of(), "test").corsFilter());
    }

    @Test
    void explicitOriginsWorkEverywhere() {
        assertDoesNotThrow(() -> config(List.of("https://admin.example.com"), "staging").corsFilter());
    }
}
