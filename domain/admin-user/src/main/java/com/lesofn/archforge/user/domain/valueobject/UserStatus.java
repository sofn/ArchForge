package com.lesofn.archforge.user.domain.valueobject;

/**
 * 用户状态枚举值对象。
 */
public enum UserStatus {
    NORMAL,
    DISABLED,
    FROZEN;

    /**
     * 判断当前状态是否可以转换到目标状态。
     *
     * @param target 目标状态
     * @return 是否允许转换
     */
    public boolean canTransitionTo(UserStatus target) {
        if (target == null || this == target) {
            return false;
        }
        return switch (this) {
            case NORMAL -> target == DISABLED || target == FROZEN;
            case DISABLED -> target == NORMAL;
            case FROZEN -> target == NORMAL;
        };
    }

    public int toPersistenceValue() {
        return ordinal() + 1;
    }

    public static UserStatus fromPersistenceValue(Integer value) {
        if (value == null) {
            return NORMAL;
        }
        return switch (value) {
            case 2 -> DISABLED;
            case 3 -> FROZEN;
            default -> NORMAL;
        };
    }
}
