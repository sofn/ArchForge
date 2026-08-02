package com.lesofn.archforge.user.domain.model.aggregate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.lesofn.archforge.user.domain.event.RoleAssignedEvent;
import com.lesofn.archforge.user.domain.event.UserCreatedEvent;
import com.lesofn.archforge.user.domain.event.UserDisabledEvent;
import com.lesofn.archforge.user.domain.event.UserEnabledEvent;
import com.lesofn.archforge.user.domain.event.UserPasswordChangedEvent;
import com.lesofn.archforge.user.domain.model.entity.User;
import com.lesofn.archforge.user.domain.valueobject.Email;
import com.lesofn.archforge.user.domain.valueobject.Password;
import com.lesofn.archforge.user.domain.valueobject.PhoneNumber;
import com.lesofn.archforge.user.domain.valueobject.RoleId;
import com.lesofn.archforge.user.domain.valueobject.UserId;
import com.lesofn.archforge.user.domain.valueobject.Username;
import org.junit.jupiter.api.Test;

class UserAggregateTest {

    private static UserAggregate newAggregate() {
        return UserAggregate.create(
                new UserId(1L),
                new Username("admin"),
                new Email("admin@example.com"),
                new PhoneNumber("13800138000"),
                Password.ofEncrypted("encrypted"),
                new RoleId(2L));
    }

    @Test
    void shouldCreateAggregateAndRegisterEvent() {
        UserAggregate aggregate = newAggregate();
        assertEquals(new UserId(1L), aggregate.getId());
        assertEquals(new RoleId(2L), aggregate.getRoleId());
        assertTrue(aggregate.canLogin());
        assertEquals(1, aggregate.getDomainEvents().size());
        assertTrue(aggregate.getDomainEvents().get(0) instanceof UserCreatedEvent);
    }

    @Test
    void shouldRejectNullUserOrRoleId() {
        User user = User.create(
                new UserId(1L),
                new Username("admin"),
                new Email("admin@example.com"),
                new PhoneNumber("13800138000"),
                Password.ofEncrypted("encrypted"));
        assertThrows(IllegalArgumentException.class, () -> new UserAggregate(null, new RoleId(1L)));
        assertThrows(IllegalArgumentException.class, () -> new UserAggregate(user, null));
    }

    @Test
    void shouldDisableAndEnableAndRegisterEvents() {
        UserAggregate aggregate = newAggregate();
        aggregate.disable();
        assertFalse(aggregate.canLogin());

        aggregate.enable();
        assertTrue(aggregate.canLogin());

        long eventCount = aggregate.getDomainEvents().stream()
                .filter(e -> e instanceof UserDisabledEvent || e instanceof UserEnabledEvent)
                .count();
        assertEquals(2L, eventCount);
    }

    @Test
    void shouldChangePasswordAndRegisterEvent() {
        UserAggregate aggregate = newAggregate();
        Password newPassword = Password.ofEncrypted("newEncrypted");
        aggregate.changePassword(newPassword);
        assertEquals("newEncrypted", aggregate.getUser().getPassword().value());
        assertTrue(aggregate.getDomainEvents().stream()
                .anyMatch(e -> e instanceof UserPasswordChangedEvent));
    }

    @Test
    void shouldAssignRoleAndRegisterEvent() {
        UserAggregate aggregate = newAggregate();
        RoleId newRole = new RoleId(5L);
        aggregate.assignRole(newRole);
        assertEquals(newRole, aggregate.getRoleId());
        assertTrue(aggregate.getDomainEvents().stream()
                .anyMatch(e -> e instanceof RoleAssignedEvent));
    }

    @Test
    void shouldRejectAssignSameRole() {
        UserAggregate aggregate = newAggregate();
        assertThrows(IllegalStateException.class, () -> aggregate.assignRole(new RoleId(2L)));
    }

    @Test
    void shouldRejectAssignNullRole() {
        UserAggregate aggregate = newAggregate();
        assertThrows(IllegalArgumentException.class, () -> aggregate.assignRole(null));
    }
}
