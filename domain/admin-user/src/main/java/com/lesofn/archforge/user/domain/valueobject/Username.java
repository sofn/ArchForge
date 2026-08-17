package com.lesofn.archforge.user.domain.valueobject;

import java.util.regex.Pattern;

/**
 * 用户名值对象。
 *
 * <p>
 * 2-64 位字符，仅允许字母、数字、下划线、点、横线。
 */
public record Username(String value) {

    private static final Pattern PATTERN = Pattern.compile("^[a-zA-Z0-9_.-]{2,64}$");

    public Username {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Username must not be blank");
        }
        if (!PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("Username must be 2-64 characters and only contain letters, digits, underscore, dot or hyphen");
        }
    }
}
