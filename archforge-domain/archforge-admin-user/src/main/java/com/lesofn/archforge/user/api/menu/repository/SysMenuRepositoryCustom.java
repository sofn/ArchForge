package com.lesofn.archforge.user.api.menu.repository;

import com.lesofn.archforge.user.api.domain.SysMenu;
import java.util.List;

/** Type-safe query methods for {@link SysMenu} using JPA Criteria API + Hibernate Metamodel. */
public interface SysMenuRepositoryCustom {

    List<SysMenu> findAllActiveMenus();

    List<SysMenu> findMenusByRoleId(Long roleId);

    List<SysMenu> selectMenuListByUserId(Long userId);
}
