package com.lesofn.archforge.user.api.service;

import java.util.List;

public interface PermissionMatrixService {

    /** Builds the permission menu tree for the matrix view. */
    List<PermissionMenuNode> menuTree();

    /** Lists menu ids granted to a role. */
    List<Long> rolePermissions(Long roleId);

    /** Replaces the role's granted menus. */
    void saveRolePermissions(Long roleId, List<Long> menuIds);
}
