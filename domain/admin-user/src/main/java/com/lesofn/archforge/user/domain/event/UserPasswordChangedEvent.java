package com.lesofn.archforge.user.domain.event;

import com.lesofn.archforge.user.domain.valueobject.UserId;
import java.time.Instant;

/**
 * 用户密码已修改领域事件。
 */
public record UserPasswordChangedEvent(UserId userId, Instant occurredAt) {

    public UserPasswordChangedEvent {
        if (userId == null) {
            throw new IllegalArgumentException("User id must not be null");
        }
        if (occurredAt == null) {
            throw new IllegalArgumentException("Occurred time must not be null");
        }
    }

    public UserPasswordChangedEvent(UserId userId) {
        this(userId, Instant.now());
    }
}
