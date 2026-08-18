package com.lesofn.archforge.user.domain.event;

import com.lesofn.archforge.user.domain.valueobject.UserId;
import java.time.Instant;

/**
 * 用户已启用领域事件。
 */
public record UserEnabledEvent(UserId userId, Instant occurredAt) {

    public UserEnabledEvent {
        if (userId == null) {
            throw new IllegalArgumentException("User id must not be null");
        }
        if (occurredAt == null) {
            throw new IllegalArgumentException("Occurred time must not be null");
        }
    }

    public UserEnabledEvent(UserId userId) {
        this(userId, Instant.now());
    }
}
