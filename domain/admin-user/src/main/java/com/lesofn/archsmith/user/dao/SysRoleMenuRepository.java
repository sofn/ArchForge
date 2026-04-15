package com.lesofn.archsmith.user.dao;

import com.lesofn.archsmith.user.domain.SysRoleMenu;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SysRoleMenuRepository
        extends JpaRepository<SysRoleMenu, SysRoleMenu.SysRoleMenuId> {

    List<SysRoleMenu> findByRoleId(Long roleId);

    List<SysRoleMenu> findByMenuId(Long menuId);

    @Modifying
    @Query("DELETE FROM SysRoleMenu rm WHERE rm.roleId = :roleId")
    void deleteByRoleId(@Param("roleId") Long roleId);

    @Modifying
    @Query("DELETE FROM SysRoleMenu rm WHERE rm.menuId = :menuId")
    void deleteByMenuId(@Param("menuId") Long menuId);
}
