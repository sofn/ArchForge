package com.lesofn.archforge.user.infrastructure.adapter.port;

import com.lesofn.archforge.user.domain.adapter.port.PasswordEncoderPort;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 基于 BCrypt 的密码加密端口实现。
 */
@Component
public class BCryptPasswordEncoderPort implements PasswordEncoderPort {

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Override
    public String encode(String rawPassword) {
        return this.encoder.encode(rawPassword);
    }

    @Override
    public boolean matches(String rawPassword, String encodedPassword) {
        return this.encoder.matches(rawPassword, encodedPassword);
    }
}
