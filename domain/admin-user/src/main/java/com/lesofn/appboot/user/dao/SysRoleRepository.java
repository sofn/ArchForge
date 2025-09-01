package com.lesofn.appboot.user.dao;

import com.lesofn.appboot.user.domain.SysMenu;
import com.lesofn.appboot.user.domain.SysRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SysRoleRepository extends JpaRepository<SysRole, Long>, JpaSpecificationExecutor<SysRole>, QuerydslPredicateExecutor<SysRole> {

    SysRole findByRoleKey(String roleKey);
    
    SysRole findByRoleName(String roleName);
    
    boolean existsByRoleKey(String roleKey);
    
    boolean existsByRoleName(String roleName);
    
    @Query("SELECT r FROM SysRole r WHERE r.deleted = false AND r.status = 1 ORDER BY r.roleSort")
    List<SysRole> findAllActiveRoles();

    List<SysRole> queryByRoleId(Long roleId);

    /**
     * 根据角色ID查询对应的菜单权限
     *
     * @param roleId 角色ID
     * @return 权限列表
     */
    @Query(value = "SELECT m.* "
            + "FROM sys_menu m "
            + " LEFT JOIN sys_role_menu rm ON m.menu_id = rm.menu_id "
            + " LEFT JOIN sys_role r ON r.role_id = rm.role_id "
            + "WHERE m.status = 1 AND m.deleted = 0 "
            + " AND r.status = 1 AND r.deleted = 0 "
            + " AND r.role_id = :roleId", nativeQuery = true)
    List<SysMenu> getMenuListByRoleId(Long roleId);
}