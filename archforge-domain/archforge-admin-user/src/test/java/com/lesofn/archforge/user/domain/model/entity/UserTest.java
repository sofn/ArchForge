package com.lesofn.archforge.user.domain.model.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.lesofn.archforge.user.domain.valueobject.Email;
import com.lesofn.archforge.user.domain.valueobject.Password;
import com.lesofn.archforge.user.domain.valueobject.PhoneNumber;
import com.lesofn.archforge.user.domain.valueobject.UserId;
import com.lesofn.archforge.user.domain.valueobject.UserStatus;
import com.lesofn.archforge.user.domain.valueobject.Username;
import org.junit.jupiter.api.Test;

class UserTest {

    private static User newUser() {
        return User.create(
                new UserId(1L),
                new Username("admin"),
                new Email("admin@example.com"),
                new PhoneNumber("13800138000"),
                Password.ofEncrypted("encrypted"));
    }

    @Test
    void shouldCreateUserWithNormalStatus() {
        User user = newUser();
        assertEquals(new UserId(1L), user.getId());
        assertEquals(UserStatus.NORMAL, user.getStatus());
        assertTrue(user.isActive());
    }

    @Test
    void shouldAllowUnsavedUserWithoutId() {
        User user = User.create(null, new Username("admin"), new Email("admin@example.com"),
                new PhoneNumber("13800138000"), Password.ofEncrypted("encrypted"));
        assertEquals(null, user.getId());
    }

    @Test
    void shouldRejectCreateWithNullFields() {
        UserId id = new UserId(1L);
        Username username = new Username("admin");
        Email email = new Email("admin@example.com");
        PhoneNumber phone = new PhoneNumber("13800138000");
        Password password = Password.ofEncrypted("encrypted");

        assertThrows(IllegalArgumentException.class, () -> User.create(id, null, email, phone, password));
        assertThrows(IllegalArgumentException.class, () -> User.create(id, username, null, phone, password));
        assertThrows(IllegalArgumentException.class, () -> User.create(id, username, email, null, password));
        assertThrows(IllegalArgumentException.class, () -> User.create(id, username, email, phone, null));
    }

    @Test
    void shouldDisableAndEnableUser() {
        User user = newUser();
        user.disable();
        assertEquals(UserStatus.DISABLED, user.getStatus());
        assertFalse(user.isActive());

        user.enable();
        assertEquals(UserStatus.NORMAL, user.getStatus());
        assertTrue(user.isActive());
    }

    @Test
    void shouldRejectInvalidTransitions() {
        User user = User.create(
                new UserId(1L),
                new Username("admin"),
                new Email("admin@example.com"),
                new PhoneNumber("13800138000"),
                Password.ofEncrypted("encrypted"),
                UserStatus.DISABLED);

        assertThrows(IllegalStateException.class, user::disable);
    }

    @Test
    void shouldChangePassword() {
        User user = newUser();
        Password newPassword = Password.ofEncrypted("newEncrypted");
        user.changePassword(newPassword);
        assertEquals("newEncrypted", user.getPassword().value());
    }

    @Test
    void shouldRejectNullPassword() {
        User user = newUser();
        assertThrows(IllegalArgumentException.class, () -> user.changePassword(null));
    }

    @Test
    void shouldBeEqualById() {
        User user1 = newUser();
        User user2 = User.create(
                new UserId(1L),
                new Username("other"),
                new Email("other@example.com"),
                new PhoneNumber("13900139000"),
                Password.ofEncrypted("other"));
        assertEquals(user1, user2);
        assertEquals(user1.hashCode(), user2.hashCode());
    }

    @Test
    void shouldNotBeEqualWhenDifferentId() {
        User user1 = newUser();
        User user2 = User.create(
                new UserId(2L),
                new Username("admin"),
                new Email("admin@example.com"),
                new PhoneNumber("13800138000"),
                Password.ofEncrypted("encrypted"));
        assertNotEquals(user1, user2);
    }
}
