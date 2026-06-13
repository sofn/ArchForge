package com.lesofn.archforge.user.dao;

import com.lesofn.archforge.user.domain.SysMenu;
import com.lesofn.archforge.user.domain.SysRole;
import java.util.List;

/** Type-safe query methods for {@link SysRole} using JPA Criteria API + Hibernate Metamodel. */
public interface SysRoleRepositoryCustom {

    List<SysRole> findAllActiveRoles();

    List<SysMenu> getMenuListByRoleId(Long roleId);
}
