package com.lesofn.appforge.server.admin.dto;

import lombok.Data;

/**
 * 管理端角色菜单项DTO（简化版），用于角色权限树
 *
 * @author lesofn
 */
@Data
public class AdminRoleMenuItemDTO {

    /** 父级菜单ID */
    private Long parentId;

    /** 菜单ID */
    private Long id;

    /** 菜单类型（0=目录，1=菜单，2=按钮） */
    private Integer menuType;

    /** 菜单标题 */
    private String title;
}
