package com.lesofn.archsmith.server.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 验证码 DTO
 *
 * @author sofn
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CaptchaDTO {

    /** 是否开启验证码 */
    private Boolean isCaptchaOn;

    /** 验证码唯一标识 */
    private String captchaCodeKey;

    /** 验证码图片 base64 */
    private String captchaCodeImg;
}
