package com.lesofn.archforge.demo.task.api.domain;

/** Task lifecycle state machine */
public enum TaskStatus {
    CREATED("待处理"),
    IN_PROGRESS("进行中"),
    COMPLETED("已完成"),
    CANCELLED("已取消");

    private final String label;

    TaskStatus(String label) {
        this.label = label;
    }

    public String getLabel() { return label; }

    public boolean canTransitionTo(TaskStatus target) {
        return switch (this) {
            case CREATED -> target == IN_PROGRESS || target == CANCELLED;
            case IN_PROGRESS -> target == COMPLETED || target == CANCELLED;
            case COMPLETED, CANCELLED -> false;
        };
    }
}
