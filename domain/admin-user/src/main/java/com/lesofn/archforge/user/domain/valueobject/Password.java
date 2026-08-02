package com.lesofn.archforge.user.domain.valueobject;

import com.lesofn.archforge.user.domain.adapter.port.PasswordEncoderPort;

/**
 * 密码值对象。
 *
 * <p>
 * 仅持有加密后的密码值，不暴露明文；加密算法通过 {@link PasswordEncoderPort} 由基础设施层注入。
 */
public record Password(String value) {

    private static final int MIN_RAW_LENGTH = 6;
    private static final int MAX_RAW_LENGTH = 32;

    public Password {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Encrypted password must not be blank");
        }
    }

    /**
     * 根据明文密码创建加密后的 Password 值对象。
     *
     * @param rawPassword 明文密码，长度 6-32
     * @param encoder 领域定义的加密端口
     * @return 加密后的 Password
     */
    public static Password ofRaw(String rawPassword, PasswordEncoderPort encoder) {
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new IllegalArgumentException("Raw password must not be blank");
        }
        if (rawPassword.length() < MIN_RAW_LENGTH || rawPassword.length() > MAX_RAW_LENGTH) {
            throw new IllegalArgumentException("Raw password length must be between " + MIN_RAW_LENGTH + " and " +
                    MAX_RAW_LENGTH);
        }
        if (encoder == null) {
            throw new IllegalArgumentException("Password encoder must not be null");
        }
        return new Password(encoder.encode(rawPassword));
    }

    /**
     * 根据已加密字符串重建 Password 值对象（通常用于反序列化/持久化还原）。
     *
     * @param encryptedPassword 加密后的密码
     * @return Password
     */
    public static Password ofEncrypted(String encryptedPassword) {
        return new Password(encryptedPassword);
    }

    /**
     * 校验明文密码是否与当前加密值匹配。
     *
     * @param rawPassword 明文密码
     * @param encoder 领域定义的加密端口
     * @return 是否匹配
     */
    public boolean matches(String rawPassword, PasswordEncoderPort encoder) {
        if (rawPassword == null || encoder == null) {
            return false;
        }
        return encoder.matches(rawPassword, this.value);
    }
}
