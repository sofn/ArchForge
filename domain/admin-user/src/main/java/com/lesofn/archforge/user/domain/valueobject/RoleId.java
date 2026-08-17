package com.lesofn.archforge.user.domain.valueobject;

/**
 * 角色 ID 值对象。
 *
 * <p>
 * 不可变，基于内部 {@link Long} 自动实现 equals/hashCode。
 */
public record RoleId(Long value) {

    public RoleId {
        if (value == null) {
            throw new IllegalArgumentException("Role id must not be null");
        }
        if (value < 0L) {
            throw new IllegalArgumentException("Role id must not be negative");
        }
    }
}
