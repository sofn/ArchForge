package com.lesofn.archforge.user.domain.valueobject;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.lesofn.archforge.user.domain.adapter.port.PasswordEncoderPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class PasswordTest {

    private final PasswordEncoderPort encoder = new FakePasswordEncoderPort();

    @Test
    void shouldCreatePasswordFromRawAndMatch() {
        Password password = Password.ofRaw("secret123", this.encoder);
        assertTrue(password.matches("secret123", this.encoder));
    }

    @Test
    void shouldNotMatchDifferentPassword() {
        Password password = Password.ofRaw("secret123", this.encoder);
        assertFalse(password.matches("wrongPassword", this.encoder));
    }

    @Test
    void shouldRejectBlankRawPassword() {
        assertThrows(IllegalArgumentException.class, () -> Password.ofRaw("", this.encoder));
        assertThrows(IllegalArgumentException.class, () -> Password.ofRaw("   ", this.encoder));
        assertThrows(IllegalArgumentException.class, () -> Password.ofRaw(null, this.encoder));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "12345", "123456789012345678901234567890123"
    })
    void shouldRejectOutOfRangeRawPassword(String value) {
        assertThrows(IllegalArgumentException.class, () -> Password.ofRaw(value, this.encoder));
    }

    @Test
    void shouldCreatePasswordFromEncryptedValue() {
        String encoded = this.encoder.encode("myPassword");
        Password password = Password.ofEncrypted(encoded);
        assertTrue(password.matches("myPassword", this.encoder));
    }

    @Test
    void shouldRejectBlankEncryptedPassword() {
        assertThrows(IllegalArgumentException.class, () -> Password.ofEncrypted(""));
        assertThrows(IllegalArgumentException.class, () -> Password.ofEncrypted(null));
    }

    @Test
    void shouldReturnFalseWhenMatchingNullRawPassword() {
        Password password = Password.ofRaw("secret123", this.encoder);
        assertFalse(password.matches(null, this.encoder));
    }

    private static class FakePasswordEncoderPort implements PasswordEncoderPort {

        @Override
        public String encode(String rawPassword) {
            return "ENC(" + rawPassword + ")";
        }

        @Override
        public boolean matches(String rawPassword, String encodedPassword) {
            return encodedPassword != null && encodedPassword.equals("ENC(" + rawPassword + ")");
        }
    }
}
