package com.lesofn.appboot.server.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 系统配置 DTO
 * @author sofn
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConfigDTO {
    
    /**
     * 是否开启验证码
     */
    private boolean captchaEnabled = true;
    
    /**
     * 是否开启注册功能
     */
    private boolean registerEnabled = false;
}