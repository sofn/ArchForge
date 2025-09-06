package com.lesofn.appboot.infrastructure.config;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 验证码类型枚举
 * @author sofn
 */
@Getter
@AllArgsConstructor
public enum CaptchaType {
    /**
     * 数学运算验证码
     */
    MATH("math"),
    
    /**
     * 字符验证码
     */
    CHAR("char");
    
    private final String value;
}
