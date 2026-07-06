package com.lesofn.archforge.user.dao;

import com.lesofn.archforge.user.domain.SysRoleMenu;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SysRoleMenuRepository extends JpaRepository<SysRoleMenu, SysRoleMenu.SysRoleMenuId>, SysRoleMenuRepositoryCustom {

    List<SysRoleMenu> findByRoleId(Long roleId);

    List<SysRoleMenu> findByMenuId(Long menuId);
}
