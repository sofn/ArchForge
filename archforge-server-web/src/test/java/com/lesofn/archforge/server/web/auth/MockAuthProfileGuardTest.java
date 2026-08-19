package com.lesofn.archforge.server.web.auth;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;

class MockAuthProfileGuardTest {

    @Test
    void mockAuthIsLimitedToDevAndTest() {
        Profile profile = MockWebAuthInterceptor.class.getAnnotation(Profile.class);
        ConditionalOnProperty property = MockWebAuthInterceptor.class.getAnnotation(ConditionalOnProperty.class);
        assertTrue(Arrays.asList(profile.value()).contains("dev"));
        assertTrue(Arrays.asList(profile.value()).contains("test"));
        assertTrue(Arrays.asList(profile.value()).size() == 2);
        assertTrue("true".equals(property.havingValue()));
    }
}
