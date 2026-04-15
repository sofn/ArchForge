package com.lesofn.archsmith.server.admin.dto;

import lombok.Data;

/**
 * 管理端部门列表项DTO（扁平结构），匹配vue-pure-admin前端部门管理格式
 *
 * @author lesofn
 */
@Data
public class AdminDeptItemDTO {

    /** 部门ID */
    private Long id;

    /** 父级部门ID */
    private Long parentId;

    /** 部门名称 */
    private String name;

    /** 负责人 */
    private String principal;

    /** 联系电话 */
    private String phone;

    /** 邮箱 */
    private String email;

    /** 排序 */
    private Integer sort;

    /** 状态（1=启用，0=停用） */
    private Integer status;

    /** 类型 */
    private Integer type;

    /** 备注 */
    private String remark;

    /** 创建时间（epoch毫秒） */
    private Long createTime;
}
