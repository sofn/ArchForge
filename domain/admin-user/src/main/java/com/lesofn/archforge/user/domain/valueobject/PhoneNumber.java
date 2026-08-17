package com.lesofn.archforge.user.domain.valueobject;

import java.util.regex.Pattern;

/**
 * 手机号值对象。
 *
 * <p>
 * 当前仅支持中国大陆 11 位手机号格式。
 */
public record PhoneNumber(String value) {

    private static final Pattern PATTERN = Pattern.compile("^1[3-9]\\d{9}$");

    public PhoneNumber {
        if (value == null) {
            throw new IllegalArgumentException("Phone number must not be null");
        }
        if (!value.isBlank() && !PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("Invalid phone number: " + value);
        }
    }

    public static PhoneNumber ofNullable(String value) {
        return new PhoneNumber(value == null ? "" : value);
    }

    public boolean isBlank() { return this.value.isBlank(); }
}
