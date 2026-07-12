package com.lesofn.archforge.user.api.dao;

/** Type-safe query methods for SysRoleMenu using JPA Criteria API + Hibernate Metamodel. */
public interface SysRoleMenuRepositoryCustom {

    void deleteByRoleId(Long roleId);

    void deleteByMenuId(Long menuId);
}
