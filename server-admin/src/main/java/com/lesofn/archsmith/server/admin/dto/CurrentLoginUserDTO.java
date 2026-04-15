package com.lesofn.archsmith.server.admin.dto;

import java.util.Set;
import lombok.Data;

/**
 * 当前登录用户信息 DTO
 *
 * @author sofn
 */
@Data
public class CurrentLoginUserDTO {
    /** 当前登录用户信息 */
    private UserDTO userInfo;

    /** 角色列表 */
    private String roleKey;

    /** 权限列表 */
    private Set<String> permissions;
}
