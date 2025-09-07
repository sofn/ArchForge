package com.lesofn.appboot.server.admin.dto;

import com.lesofn.appboot.infrastructure.auth.model.SystemLoginUser;
import lombok.Data;

import java.util.Set;

/**
 * 当前登录用户信息 DTO
 * @author sofn
 */
@Data
public class CurrentLoginUserDTO {
    /**
     * 当前登录用户信息
     */
    private SystemLoginUser userInfo;
    /**
     * 角色列表
     */
    private String roleKey;
    /**
     * 权限列表
     */
    private Set<String> permissions;
}