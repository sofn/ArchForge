package com.lesofn.archforge.user.domain.valueobject;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class UserStatusTest {

    @Test
    void normalCanTransitionToDisabledOrFrozen() {
        assertTrue(UserStatus.NORMAL.canTransitionTo(UserStatus.DISABLED));
        assertTrue(UserStatus.NORMAL.canTransitionTo(UserStatus.FROZEN));
    }

    @Test
    void disabledCanOnlyTransitionToNormal() {
        assertTrue(UserStatus.DISABLED.canTransitionTo(UserStatus.NORMAL));
        assertFalse(UserStatus.DISABLED.canTransitionTo(UserStatus.FROZEN));
    }

    @Test
    void frozenCanOnlyTransitionToNormal() {
        assertTrue(UserStatus.FROZEN.canTransitionTo(UserStatus.NORMAL));
        assertFalse(UserStatus.FROZEN.canTransitionTo(UserStatus.DISABLED));
    }

    @Test
    void cannotTransitionToSameStatus() {
        assertFalse(UserStatus.NORMAL.canTransitionTo(UserStatus.NORMAL));
        assertFalse(UserStatus.DISABLED.canTransitionTo(UserStatus.DISABLED));
        assertFalse(UserStatus.FROZEN.canTransitionTo(UserStatus.FROZEN));
    }

    @Test
    void cannotTransitionToNull() {
        assertFalse(UserStatus.NORMAL.canTransitionTo(null));
    }
}
