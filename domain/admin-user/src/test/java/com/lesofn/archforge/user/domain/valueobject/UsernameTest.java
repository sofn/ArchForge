package com.lesofn.archforge.user.domain.valueobject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class UsernameTest {

    @Test
    void shouldAcceptValidUsername() {
        Username username = new Username("admin_user");
        assertEquals("admin_user", username.value());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "user", "test", "a.b-c_123", "12345678901234567890123456789012"
    })
    void shouldAcceptValidUsernames(String value) {
        new Username(value);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "", "ab", "abc", "123456789012345678901234567890123", "user name", "user@name"
    })
    void shouldRejectInvalidUsernames(String value) {
        assertThrows(IllegalArgumentException.class, () -> new Username(value));
    }

    @Test
    void shouldRejectNullUsername() {
        assertThrows(IllegalArgumentException.class, () -> new Username(null));
    }
}
