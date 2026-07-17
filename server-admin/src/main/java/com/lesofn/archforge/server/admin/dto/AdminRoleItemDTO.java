package com.lesofn.archforge.server.admin.dto;

import java.util.List;
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

    /** 数据权限范围（1=全部，2=自定义，3=本部门，4=本部门及以下，5=仅本人） */
    private Integer dataScope;

    /** 自定义数据权限部门ID列表 */
    private List<Long> customDeptIds;

    /** 备注 */
    private String remark;

    /** 创建时间（epoch毫秒） */
    private Long createTime;

    /** 更新时间（epoch毫秒） */
    private Long updateTime;
}
