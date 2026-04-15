package com.lesofn.archsmith.server.admin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 登录命令
 *
 * @author sofn
 */
@Data
public class LoginCommand {

    /** 用户名 */
    @NotBlank(message = "用户名不能为空")
    private String username;

    /** 密码 */
    @NotBlank(message = "密码不能为空")
    private String password;

    /** 验证码 */
    private String captchaCode;

    /** 验证码唯一标识 */
    private String captchaCodeKey;
}
