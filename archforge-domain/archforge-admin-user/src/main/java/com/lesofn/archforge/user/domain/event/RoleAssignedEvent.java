package com.lesofn.archforge.user.domain.event;

import com.lesofn.archforge.user.domain.valueobject.RoleId;
import com.lesofn.archforge.user.domain.valueobject.UserId;
import java.time.Instant;

/**
 * 用户角色已分配领域事件。
 */
public record RoleAssignedEvent(UserId userId, RoleId roleId, Instant occurredAt) {

    public RoleAssignedEvent {
        if (userId == null) {
            throw new IllegalArgumentException("User id must not be null");
        }
        if (roleId == null) {
            throw new IllegalArgumentException("Role id must not be null");
        }
        if (occurredAt == null) {
            throw new IllegalArgumentException("Occurred time must not be null");
        }
    }

    public RoleAssignedEvent(UserId userId, RoleId roleId) {
        this(userId, roleId, Instant.now());
    }
}
