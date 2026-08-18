package com.lesofn.archforge.user.domain.valueobject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class UserIdTest {

    @Test
    void shouldCreateUserIdWithPositiveValue() {
        UserId id = new UserId(1L);
        assertEquals(1L, id.value());
    }

    @Test
    void shouldRejectNullValue() {
        assertThrows(IllegalArgumentException.class, () -> new UserId(null));
    }

    @Test
    void shouldRejectNonPositiveValue() {
        assertThrows(IllegalArgumentException.class, () -> new UserId(0L));
        assertThrows(IllegalArgumentException.class, () -> new UserId(-1L));
    }

    @Test
    void shouldBeEqualWhenSameValue() {
        UserId id1 = new UserId(42L);
        UserId id2 = new UserId(42L);
        assertEquals(id1, id2);
        assertEquals(id1.hashCode(), id2.hashCode());
    }

    @Test
    void shouldNotBeEqualWhenDifferentValue() {
        UserId id1 = new UserId(1L);
        UserId id2 = new UserId(2L);
        assertNotEquals(id1, id2);
    }
}
