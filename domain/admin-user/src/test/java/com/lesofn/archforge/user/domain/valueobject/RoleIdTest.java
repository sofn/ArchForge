package com.lesofn.archforge.user.domain.valueobject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class RoleIdTest {

    @Test
    void shouldCreateWithPositiveValue() {
        RoleId roleId = new RoleId(1L);
        assertEquals(1L, roleId.value());
    }

    @Test
    void shouldRejectNullValue() {
        assertThrows(IllegalArgumentException.class, () -> new RoleId(null));
    }

    @Test
    void shouldRejectNonPositiveValue() {
        assertThrows(IllegalArgumentException.class, () -> new RoleId(0L));
    }

    @Test
    void shouldBeEqualWhenSameValue() {
        RoleId roleId1 = new RoleId(5L);
        RoleId roleId2 = new RoleId(5L);
        assertEquals(roleId1, roleId2);
    }

    @Test
    void shouldNotBeEqualWhenDifferentValue() {
        RoleId roleId1 = new RoleId(1L);
        RoleId roleId2 = new RoleId(2L);
        assertNotEquals(roleId1, roleId2);
    }
}
