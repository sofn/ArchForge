package com.lesofn.archforge.common.encrypt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Base64;
import org.junit.jupiter.api.Test;

/**
 * AES helper contract: no built-in key (a key published in source protects nothing), authenticated
 * encryption with a fresh IV per message, and errors that never echo plaintext, ciphertext or key.
 */
class AESEncrypterTest {

    /** Same shape {@code archforge init} writes to .env: standard Base64 of 32 random bytes. */
    private static final String KEY = Base64.getEncoder().encodeToString(new byte[] {
            1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16,
            17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32
    });

    @Test
    void roundTripsAndAcceptsAllAesKeySizes() {
        for (int size : new int[] {
                16, 24, 32
        }) {
            AESEncrypter aes = AESEncrypter.of(Base64.getEncoder().encodeToString(new byte[size]));
            assertEquals("秘密 payload", aes.decryptAsString(aes.encrypt("秘密 payload")), "key size " + size);
        }
    }

    @Test
    void sameInputEncryptsDifferentlyEveryTime() {
        AESEncrypter aes = AESEncrypter.of(KEY);

        // ECB (the old default) would make identical plaintexts produce identical ciphertexts
        assertNotEquals(aes.encrypt("hello"), aes.encrypt("hello"));
    }

    @Test
    void tamperedCiphertextIsRejected() {
        AESEncrypter aes = AESEncrypter.of(KEY);
        String hex = aes.encrypt("hello");
        char last = hex.charAt(hex.length() - 1);
        String tampered = hex.substring(0, hex.length() - 1) + (last == '0' ? '1' : '0');

        assertThrows(EncrypterException.class, () -> aes.decrypt(tampered));
    }

    @Test
    void wrongKeyCannotDecrypt() {
        String hex = AESEncrypter.of(KEY).encrypt("hello");
        AESEncrypter other = AESEncrypter.of(Base64.getEncoder().encodeToString(new byte[32]));

        assertThrows(EncrypterException.class, () -> other.decrypt(hex));
    }

    @Test
    void keyMustBeBase64OfAValidAesLength() {
        String badLength = Base64.getEncoder().encodeToString(new byte[10]);

        EncrypterException e = assertThrows(EncrypterException.class, () -> AESEncrypter.of(badLength));
        assertFalse(String.valueOf(e.getMessage()).contains(badLength), "key material must not be echoed");
        assertThrows(EncrypterException.class, () -> AESEncrypter.of("%%% not base64 %%%"));
    }

    @Test
    void errorMessagesNeverEchoSensitiveInput() {
        AESEncrypter aes = AESEncrypter.of(KEY);
        String garbage = "not-hex-secret-looking-data";

        EncrypterException e = assertThrows(EncrypterException.class, () -> aes.decrypt(garbage));

        assertFalse(String.valueOf(e.getMessage()).contains(garbage), e.getMessage());
        Throwable cause = e.getCause();
        assertTrue(cause == null || !String.valueOf(cause.getMessage()).contains(garbage));
    }

    @Test
    void thereIsNoDefaultKeyFactory() {
        // The previous getInstance() fell back to a key committed to source control.
        boolean zeroArgStatic = Arrays.stream(AESEncrypter.class.getMethods())
                .filter(m -> Modifier.isStatic(m.getModifiers()) && m.getReturnType() == AESEncrypter.class)
                .map(Method::getParameterCount)
                .anyMatch(count -> count == 0);

        assertFalse(zeroArgStatic, "a key-less factory means a built-in key");
    }
}
