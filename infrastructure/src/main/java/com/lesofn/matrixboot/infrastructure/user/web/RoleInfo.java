package com.lesofn.matrixboot.infrastructure.user.web;

import com.google.common.collect.ImmutableSet;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.Set;

/**
 * @author valarchie
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoleInfo {

    public static final RoleInfo EMPTY_ROLE = new RoleInfo();
    public static final long ADMIN_ROLE_ID = -1;
    public static final String ADMIN_ROLE_KEY = "admin";
    public static final String ALL_PERMISSIONS = "*:*:*";

    public static final Set<String> ADMIN_PERMISSIONS = ImmutableSet.of(ALL_PERMISSIONS);


    public RoleInfo(Long roleId, String roleKey, DataScopeEnum dataScope, Set<Long> deptIdSet,
                    Set<String> menuPermissions, Set<Long> menuIds) {
        this.roleId = roleId;
        this.roleKey = roleKey;
        this.dataScope = dataScope;
        this.deptIdSet = deptIdSet;
        this.menuPermissions = menuPermissions != null ? menuPermissions : Collections.emptySet();
        this.menuIds = menuIds != null ? menuIds : Collections.emptySet();
    }


    private Long roleId;
    private String roleName;
    private DataScopeEnum dataScope;
    private Set<Long> deptIdSet;
    private String roleKey;
    private Set<String> menuPermissions;
    private Set<Long> menuIds;

}
