package com.lesofn.appforge.server.admin.dto;

import lombok.Data;

/**
 * 管理端用户列表查询请求
 *
 * @author lesofn
 */
@Data
public class AdminUserListRequest {

    /** 用户名 */
    private String username;

    /** 手机号 */
    private String phone;

    /** 状态 */
    private Integer status;

    /** 部门ID */
    private Long deptId;

    /** 当前页码（从1开始） */
    private Integer currentPage = 1;

    /** 每页大小 */
    private Integer pageSize = 10;
}
