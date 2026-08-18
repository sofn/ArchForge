package com.lesofn.archforge.user.domain.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.lesofn.archforge.user.domain.model.aggregate.UserAggregate;
import com.lesofn.archforge.user.domain.adapter.repository.InMemoryUserRepository;
import com.lesofn.archforge.user.domain.valueobject.Email;
import com.lesofn.archforge.user.domain.valueobject.Password;
import com.lesofn.archforge.user.domain.valueobject.PhoneNumber;
import com.lesofn.archforge.user.domain.valueobject.RoleId;
import com.lesofn.archforge.user.domain.valueobject.UserId;
import com.lesofn.archforge.user.domain.valueobject.UserStatus;
import com.lesofn.archforge.user.domain.valueobject.Username;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UserDomainServiceImplTest {

    private InMemoryUserRepository repository;
    private UserDomainServiceImpl service;

    @BeforeEach
    void setUp() {
        this.repository = new InMemoryUserRepository();
        this.service = new UserDomainServiceImpl(this.repository);
    }

    @Test
    void shouldCreateUser() {
        UserAggregate aggregate = this.service.createUser(
                new UserId(1L),
                new Username("admin"),
                new Email("admin@example.com"),
                new PhoneNumber("13800138000"),
                Password.ofEncrypted("encrypted"),
                new RoleId(2L));

        assertEquals(new UserId(1L), aggregate.getId());
        assertTrue(aggregate.canLogin());
    }

    @Test
    void shouldDisableAndEnableUser() {
        this.service.createUser(
                new UserId(1L),
                new Username("admin"),
                new Email("admin@example.com"),
                new PhoneNumber("13800138000"),
                Password.ofEncrypted("encrypted"),
                new RoleId(2L));

        this.service.disableUser(new UserId(1L));
        UserAggregate disabled = this.repository.findById(new UserId(1L)).orElseThrow();
        assertEquals(UserStatus.DISABLED, disabled.getUser().getStatus());
        assertFalse(disabled.canLogin());

        this.service.enableUser(new UserId(1L));
        UserAggregate enabled = this.repository.findById(new UserId(1L)).orElseThrow();
        assertEquals(UserStatus.NORMAL, enabled.getUser().getStatus());
        assertTrue(enabled.canLogin());
    }

    @Test
    void shouldChangePassword() {
        this.service.createUser(
                new UserId(1L),
                new Username("admin"),
                new Email("admin@example.com"),
                new PhoneNumber("13800138000"),
                Password.ofEncrypted("encrypted"),
                new RoleId(2L));

        this.service.changePassword(new UserId(1L), Password.ofEncrypted("newEncrypted"));
        UserAggregate aggregate = this.repository.findById(new UserId(1L)).orElseThrow();
        assertEquals("newEncrypted", aggregate.getUser().getPassword().value());
    }

    @Test
    void shouldAssignRole() {
        this.service.createUser(
                new UserId(1L),
                new Username("admin"),
                new Email("admin@example.com"),
                new PhoneNumber("13800138000"),
                Password.ofEncrypted("encrypted"),
                new RoleId(2L));

        this.service.assignRole(new UserId(1L), new RoleId(5L));
        UserAggregate aggregate = this.repository.findById(new UserId(1L)).orElseThrow();
        assertEquals(new RoleId(5L), aggregate.getRoleId());
    }

    @Test
    void shouldThrowWhenUserNotFound() {
        UserId notFound = new UserId(99L);
        assertThrows(IllegalArgumentException.class, () -> this.service.disableUser(notFound));
        assertThrows(IllegalArgumentException.class, () -> this.service.enableUser(notFound));
        assertThrows(IllegalArgumentException.class, () -> this.service.changePassword(notFound, Password.ofEncrypted("x")));
        assertThrows(IllegalArgumentException.class, () -> this.service.assignRole(notFound, new RoleId(1L)));
    }
}
