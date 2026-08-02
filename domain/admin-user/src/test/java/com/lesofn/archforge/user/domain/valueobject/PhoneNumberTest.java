package com.lesofn.archforge.user.domain.valueobject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class PhoneNumberTest {

    @Test
    void shouldAcceptValidPhoneNumber() {
        PhoneNumber phone = new PhoneNumber("13800138000");
        assertEquals("13800138000", phone.value());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "13912345678", "15012345678", "19912345678"
    })
    void shouldAcceptValidPhoneNumbers(String value) {
        new PhoneNumber(value);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "", "1380013800", "138001380000", "028-12345678", "abcdefghijklmnopqrstuvwxyz"
    })
    void shouldRejectInvalidPhoneNumbers(String value) {
        assertThrows(IllegalArgumentException.class, () -> new PhoneNumber(value));
    }

    @Test
    void shouldRejectNullPhoneNumber() {
        assertThrows(IllegalArgumentException.class, () -> new PhoneNumber(null));
    }
}
