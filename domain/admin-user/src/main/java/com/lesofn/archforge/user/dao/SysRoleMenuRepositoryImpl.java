package com.lesofn.archforge.user.dao;

import com.lesofn.archforge.common.repository.CriteriaQuerySupport;
import com.lesofn.archforge.user.domain.SysRoleMenu;
import com.lesofn.archforge.user.domain.SysRoleMenu_;

/** Criteria API implementation — no raw JPQL strings. */
public class SysRoleMenuRepositoryImpl extends CriteriaQuerySupport
        implements SysRoleMenuRepositoryCustom {

    @Override
    public void deleteByRoleId(Long roleId) {
        deleteByAttribute(SysRoleMenu.class, SysRoleMenu_.roleId, roleId);
    }

    @Override
    public void deleteByMenuId(Long menuId) {
        deleteByAttribute(SysRoleMenu.class, SysRoleMenu_.menuId, menuId);
    }
}
