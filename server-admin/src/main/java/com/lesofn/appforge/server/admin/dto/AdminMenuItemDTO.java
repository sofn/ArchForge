package com.lesofn.appforge.server.admin.dto;

import lombok.Data;

/**
 * 管理端菜单列表项DTO（扁平结构），匹配vue-pure-admin前端菜单管理格式
 *
 * @author lesofn
 */
@Data
public class AdminMenuItemDTO {

    /** 父级菜单ID */
    private Long parentId;

    /** 菜单ID */
    private Long id;

    /** 菜单类型（0=目录，1=菜单，2=按钮） */
    private Integer menuType;

    /** 菜单标题 */
    private String title;

    /** 路由名称 */
    private String name;

    /** 路由路径 */
    private String path;

    /** 组件路径 */
    private String component;

    /** 排序 */
    private Integer rank;

    /** 重定向地址 */
    private String redirect;

    /** 图标 */
    private String icon;

    /** 额外图标 */
    private String extraIcon;

    /** 进场动画 */
    private String enterTransition;

    /** 离场动画 */
    private String leaveTransition;

    /** 激活路径 */
    private String activePath;

    /** 权限标识 */
    private String auths;

    /** 内嵌iframe地址 */
    private String frameSrc;

    /** iframe首次加载动画 */
    private Boolean frameLoading;

    /** 是否缓存 */
    private Boolean keepAlive;

    /** 是否隐藏标签 */
    private Boolean hiddenTag;

    /** 是否固定标签 */
    private Boolean fixedTag;

    /** 是否显示菜单 */
    private Boolean showLink;

    /** 是否显示父级菜单 */
    private Boolean showParent;
}
