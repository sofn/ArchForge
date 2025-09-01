package com.lesofn.appboot.user.dao;

import com.lesofn.appboot.user.domain.SysRoleMenu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SysRoleMenuRepository extends JpaRepository<SysRoleMenu, SysRoleMenu.SysRoleMenuId> {

    List<SysRoleMenu> findByRoleId(Long roleId);
    
    List<SysRoleMenu> findByMenuId(Long menuId);
    
    @Modifying
    @Query("DELETE FROM SysRoleMenu rm WHERE rm.roleId = :roleId")
    void deleteByRoleId(@Param("roleId") Long roleId);
    
    @Modifying
    @Query("DELETE FROM SysRoleMenu rm WHERE rm.menuId = :menuId")
    void deleteByMenuId(@Param("menuId") Long menuId);
}