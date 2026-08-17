package com.lesofn.archforge.user.domain.valueobject;

import java.util.regex.Pattern;

/**
 * 邮箱值对象。
 */
public record Email(String value) {

    private static final Pattern PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    public Email {
        if (value == null) {
            throw new IllegalArgumentException("Email must not be null");
        }
        if (!value.isBlank() && !PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("Invalid email format: " + value);
        }
    }

    public static Email ofNullable(String value) {
        return new Email(value == null ? "" : value);
    }

    public boolean isBlank() { return this.value.isBlank(); }
}
