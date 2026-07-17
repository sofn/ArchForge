package com.lesofn.archforge.server.admin.dto.request;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Data;

/**
 * 角色数据权限更新请求。
 *
 * @author sofn
 */
@Data
public class RoleDataScopeRequest {

    /** 角色ID */
    @NotNull
    private Long id;

    /**
     * 数据权限范围。
     *
     * <ul>
     * <li>1：全部数据权限</li>
     * <li>2：自定义数据权限</li>
     * <li>3：本部门数据权限</li>
     * <li>4：本部门及以下数据权限</li>
     * <li>5：仅本人数据权限</li>
     * </ul>
     */
    @NotNull
    private Integer dataScope;

    /** 自定义数据权限部门ID列表（dataScope=2 时有效） */
    private List<Long> deptIds;
}
