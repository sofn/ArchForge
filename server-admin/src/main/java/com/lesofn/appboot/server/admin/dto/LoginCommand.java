package com.lesofn.appboot.server.admin.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;

/**
 * 登录命令
 * @author sofn
 */
@Data
public class LoginCommand {
    
    /**
     * 用户名
     */
    @NotBlank(message = "用户名不能为空")
    private String username;
    
    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    private String password;
    
    /**
     * 验证码
     */
    private String code;
    
    /**
     * 验证码唯一标识
     */
    private String uuid;
}