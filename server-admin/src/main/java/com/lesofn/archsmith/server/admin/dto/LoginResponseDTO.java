package com.lesofn.archsmith.server.admin.dto;

import java.util.List;
import lombok.Data;

/**
 * 登录响应DTO - 匹配前端vue-pure-admin期望的格式
 *
 * @author lesofn
 */
@Data
public class LoginResponseDTO {

    /** 用户头像 */
    private String avatar;

    /** 用户名 */
    private String username;

    /** 用户昵称 */
    private String nickname;

    /** 角色列表 */
    private List<String> roles;

    /** 权限列表 */
    private List<String> permissions;

    /** 访问令牌 */
    private String accessToken;

    /** 刷新令牌 */
    private String refreshToken;

    /** 过期时间 格式: yyyy/MM/dd HH:mm:ss */
    private String expires;
}
