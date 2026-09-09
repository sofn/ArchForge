package com.lesofn.archforge.common.profile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class DefaultProfileLoaderTest {

    @Test
    void knownProfileIsParsed() {
        assertEquals(Env.dev, DefaultProfileLoader.resolveEnv("dev"));
        assertEquals(Env.test, DefaultProfileLoader.resolveEnv("test"));
        assertEquals(Env.prod, DefaultProfileLoader.resolveEnv("prod"));
    }

    @Test
    void nullProfileFallsBackToDefault() {
        assertEquals(DefaultProfileLoader.DEFAULT_DEV, DefaultProfileLoader.resolveEnv(null));
    }

    @Test
    void unknownProfileThrowsRatherThanDefaultingToProd() {
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> DefaultProfileLoader.resolveEnv(
                "stging"));
        String message = ex.getMessage();
        assertTrue(message != null && message.contains("stging"));
    }
}
