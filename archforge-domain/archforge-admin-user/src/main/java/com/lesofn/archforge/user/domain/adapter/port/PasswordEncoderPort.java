package com.lesofn.archforge.user.domain.adapter.port;

/**
 * 密码加密端口。
 *
 * <p>
 * 由领域层定义，基础设施层提供具体实现（如 BCrypt）。
 */
public interface PasswordEncoderPort {

    /**
     * 对明文密码进行加密。
     *
     * @param rawPassword 明文密码
     * @return 加密后的密码
     */
    String encode(String rawPassword);

    /**
     * 校验明文密码是否与加密后的密码匹配。
     *
     * @param rawPassword 明文密码
     * @param encodedPassword 加密后的密码
     * @return 是否匹配
     */
    boolean matches(String rawPassword, String encodedPassword);
}
