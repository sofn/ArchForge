package com.lesofn.archsmith.demo.task.domain;

/** Task lifecycle state machine */
public enum TaskStatus {
    CREATED,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED;

    public boolean canTransitionTo(TaskStatus target) {
        return switch (this) {
            case CREATED -> target == IN_PROGRESS || target == CANCELLED;
            case IN_PROGRESS -> target == COMPLETED || target == CANCELLED;
            case COMPLETED, CANCELLED -> false;
        };
    }
}
