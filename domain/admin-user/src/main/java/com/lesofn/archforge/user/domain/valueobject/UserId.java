package com.lesofn.archforge.user.domain.valueobject;

/**
 * 用户 ID 值对象。
 *
 * <p>
 * 不可变，基于内部 {@link Long} 自动实现 equals/hashCode。
 */
public record UserId(Long value) {

    public UserId {
        if (value == null) {
            throw new IllegalArgumentException("User id must not be null");
        }
        if (value <= 0L) {
            throw new IllegalArgumentException("User id must be greater than 0");
        }
    }
}
