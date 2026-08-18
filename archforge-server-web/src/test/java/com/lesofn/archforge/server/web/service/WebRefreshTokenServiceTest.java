package com.lesofn.archforge.server.web.service;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class WebRefreshTokenServiceTest {

    private final WebRefreshTokenService service = new WebRefreshTokenService();

    @Test
    void validateRejectsBlankToken() {
        assertThrows(RuntimeException.class, () -> service.validateRefreshToken(" "));
        assertThrows(RuntimeException.class, () -> service.validateRefreshToken(null));
    }
}
