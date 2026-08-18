package com.lesofn.archforge.infrastructure.security.datascope;

import com.lesofn.archforge.infrastructure.user.web.DataScopeEnum;
import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 数据权限上下文。
 *
 * @author sofn
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataScopeContext {

    /** 数据权限范围 */
    private DataScopeEnum dataScope;

    /** 当前登录用户 ID */
    private Long userId;

    /** 当前登录用户所属部门 ID */
    private Long deptId;

    /** 自定义部门 ID 集合（CUSTOM_DEFINE 时使用） */
    private Set<Long> customDeptIds;

    /** 部门字段别名 */
    private String deptAlias;

    /** 用户字段别名 */
    private String userAlias;

    public Set<Long> getCustomDeptIds() { return customDeptIds == null ? new HashSet<>() : new HashSet<>(customDeptIds); }
}
