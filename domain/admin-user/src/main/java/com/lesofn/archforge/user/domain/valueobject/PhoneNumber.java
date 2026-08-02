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
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Phone number must not be blank");
        }
        if (!PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("Invalid phone number: " + value);
        }
    }
}
