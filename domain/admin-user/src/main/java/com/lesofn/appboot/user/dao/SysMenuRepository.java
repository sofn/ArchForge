package com.lesofn.appboot.user.dao;

import com.lesofn.appboot.user.domain.SysMenu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SysMenuRepository extends JpaRepository<SysMenu, Long>, JpaSpecificationExecutor<SysMenu>, QuerydslPredicateExecutor<SysMenu> {

    List<SysMenu> findByParentId(Long parentId);
    
    List<SysMenu> findByParentIdOrderByMenuIdAsc(Long parentId);
    
    List<SysMenu> findByPermission(String permission);
    
    @Query("SELECT m FROM SysMenu m WHERE m.deleted = false AND m.status = 1 ORDER BY m.parentId, m.menuId")
    List<SysMenu> findAllActiveMenus();
    
    @Query("SELECT m FROM SysMenu m JOIN SysRoleMenu rm ON m.menuId = rm.menuId WHERE rm.roleId = :roleId AND m.deleted = false AND m.status = 1")
    List<SysMenu> findMenusByRoleId(@Param("roleId") Long roleId);
}