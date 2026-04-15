package com.lesofn.archsmith.server.admin.dto;

import lombok.Data;

/**
 * 管理端角色列表项DTO，匹配vue-pure-admin前端角色管理格式
 *
 * @author lesofn
 */
@Data
public class AdminRoleItemDTO {

    /** 角色ID */
    private Long id;

    /** 角色名称 */
    private String name;

    /** 角色编码 */
    private String code;

    /** 状态（1=启用，0=停用） */
    private Integer status;

    /** 备注 */
    private String remark;

    /** 创建时间（epoch毫秒） */
    private Long createTime;

    /** 更新时间（epoch毫秒） */
    private Long updateTime;
}
