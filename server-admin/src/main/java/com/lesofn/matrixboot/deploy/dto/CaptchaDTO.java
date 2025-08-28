package com.lesofn.matrixboot.deploy.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 验证码 DTO
 * @author sofn
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CaptchaDTO {
    
    /**
     * 是否开启验证码
     */
    private boolean captchaEnabled;
    
    /**
     * 验证码唯一标识
     */
    private String uuid;
    
    /**
     * 验证码图片 base64
     */
    private String img;
}