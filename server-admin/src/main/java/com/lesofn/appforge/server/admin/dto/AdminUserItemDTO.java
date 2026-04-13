package com.lesofn.appforge.server.admin.dto;

import lombok.Data;

/**
 * 管理端用户列表项DTO，匹配vue-pure-admin前端用户管理格式
 *
 * @author lesofn
 */
@Data
public class AdminUserItemDTO {

    /** 用户ID */
    private Long id;

    /** 头像 */
    private String avatar;

    /** 用户名 */
    private String username;

    /** 昵称 */
    private String nickname;

    /** 手机号 */
    private String phone;

    /** 邮箱 */
    private String email;

    /** 性别（0=男，1=女，2=未知） */
    private Integer sex;

    /** 状态（1=启用，0=停用） */
    private Integer status;

    /** 所属部门 */
    private DeptInfo dept;

    /** 备注 */
    private String remark;

    /** 创建时间（epoch毫秒） */
    private Long createTime;

    /** 部门简要信息 */
    @Data
    public static class DeptInfo {

        /** 部门ID */
        private Long id;

        /** 部门名称 */
        private String name;

        public static DeptInfo of(Long id, String name) {
            DeptInfo deptInfo = new DeptInfo();
            deptInfo.setId(id);
            deptInfo.setName(name);
            return deptInfo;
        }
    }
}
