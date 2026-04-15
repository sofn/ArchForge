package com.lesofn.archsmith.server.admin.dto;

import lombok.Data;

/**
 * 管理端角色列表查询请求
 *
 * @author lesofn
 */
@Data
public class AdminRoleListRequest {

    /** 角色名称 */
    private String name;

    /** 角色编码 */
    private String code;

    /** 状态 */
    private Integer status;

    /** 当前页码（从1开始） */
    private Integer currentPage = 1;

    /** 每页大小 */
    private Integer pageSize = 10;
}
