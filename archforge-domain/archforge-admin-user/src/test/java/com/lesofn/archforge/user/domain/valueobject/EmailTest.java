package com.lesofn.archforge.user.domain.valueobject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class EmailTest {

    @Test
    void shouldAcceptValidEmail() {
        Email email = new Email("admin@example.com");
        assertEquals("admin@example.com", email.value());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "user@lesofn.com", "a.b+c@example.cn", "root@localhost.io"
    })
    void shouldAcceptValidEmails(String value) {
        new Email(value);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "plainaddress", "@missing-local.com", "admin@", "admin@.com"
    })
    void shouldRejectInvalidEmails(String value) {
        assertThrows(IllegalArgumentException.class, () -> new Email(value));
    }

    @Test
    void shouldRejectNullEmail() {
        assertThrows(IllegalArgumentException.class, () -> new Email(null));
    }

    @Test
    void shouldAllowBlankEmailForPersistenceRestore() {
        Email email = Email.ofNullable("");
        assertEquals("", email.value());
        assertTrue(email.isBlank());
    }
}
