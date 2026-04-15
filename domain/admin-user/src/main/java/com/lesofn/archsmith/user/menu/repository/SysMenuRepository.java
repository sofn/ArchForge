package com.lesofn.archsmith.user.menu.repository;

import com.lesofn.archsmith.user.domain.SysMenu;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * SysMenuRepository接口，定义Spring Data JPA方法 实际使用请注入SysMenuRepositoryImpl类，它同时支持Spring Data
 * JPA和QueryDSL
 */
@Repository
public interface SysMenuRepository
        extends JpaRepository<SysMenu, Long>,
                JpaSpecificationExecutor<SysMenu>,
                QuerydslPredicateExecutor<SysMenu>,
                SysMenuRepositoryCustom {

    List<SysMenu> findByParentId(Long parentId);

    List<SysMenu> findByParentIdOrderByMenuIdAsc(Long parentId);

    List<SysMenu> findByPermission(String permission);

    @Query(
            "SELECT m FROM SysMenu m WHERE m.deleted = false AND m.status = 1 ORDER BY m.parentId, m.menuId")
    List<SysMenu> findAllActiveMenus();

    @Query(
            "SELECT m FROM SysMenu m JOIN SysRoleMenu rm ON m.menuId = rm.menuId WHERE rm.roleId = :roleId AND m.deleted = false AND m.status = 1")
    List<SysMenu> findMenusByRoleId(@Param("roleId") Long roleId);
}
