package com.lesofn.archforge.common.auth;

import java.util.Collections;
import org.jspecify.annotations.Nullable;
import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录用户的角色与权限信息。
 *
 * @author sofn
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuppressWarnings("NullAway.Init") // fields populated via setters/builders
public class RoleInfo {

    public static final RoleInfo EMPTY_ROLE = new RoleInfo();
    public static final long ADMIN_ROLE_ID = -1;
    public static final String ADMIN_ROLE_KEY = "admin";
    public static final String ALL_PERMISSIONS = "*:*:*";

    public static final Set<String> ADMIN_PERMISSIONS = new HashSet<>(Collections.singleton(ALL_PERMISSIONS));

    public RoleInfo(
            Long roleId,
            String roleKey,
            @Nullable DataScopeEnum dataScope,
            @Nullable Set<Long> deptIdSet,
            @Nullable Set<String> menuPermissions,
            @Nullable Set<Long> menuIds) {
        this.roleId = roleId;
        this.roleKey = roleKey;
        this.dataScope = dataScope;
        setDeptIdSet(deptIdSet);
        setMenuPermissions(menuPermissions);
        setMenuIds(menuIds);
    }

    public void setDeptIdSet(@Nullable Set<Long> deptIdSet) {
        this.deptIdSet = deptIdSet == null ? new HashSet<>() : new HashSet<>(deptIdSet);
    }

    public void setMenuPermissions(@Nullable Set<String> menuPermissions) {
        this.menuPermissions = menuPermissions == null ? new HashSet<>() : new HashSet<>(menuPermissions);
    }

    public void setMenuIds(@Nullable Set<Long> menuIds) {
        this.menuIds = menuIds == null ? new HashSet<>() : new HashSet<>(menuIds);
    }

    private Long roleId;
    private String roleName;
    private @Nullable DataScopeEnum dataScope;
    private Set<Long> deptIdSet;
    private String roleKey;
    private Set<String> menuPermissions;
    private @Nullable Set<Long> menuIds;
}
