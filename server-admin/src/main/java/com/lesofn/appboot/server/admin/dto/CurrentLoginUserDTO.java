package com.lesofn.appboot.server.admin.dto;

import lombok.Data;

import java.util.List;
import java.util.Set;

/**
 * 当前登录用户信息 DTO
 * @author sofn
 */
@Data
public class CurrentLoginUserDTO {
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 用户昵称
     */
    private String nickName;
    
    /**
     * 邮箱
     */
    private String email;
    
    /**
     * 手机号
     */
    private String phoneNumber;
    
    /**
     * 性别
     */
    private String sex;
    
    /**
     * 头像
     */
    private String avatar;
    
    /**
     * 部门ID
     */
    private Long deptId;
    
    /**
     * 部门名称
     */
    private String deptName;
    
    /**
     * 角色列表
     */
    private List<String> roles;
    
    /**
     * 权限列表
     */
    private Set<String> permissions;
}