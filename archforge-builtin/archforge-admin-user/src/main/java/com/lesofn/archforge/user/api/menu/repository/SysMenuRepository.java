package com.lesofn.archforge.user.api.menu.repository;

import com.lesofn.archforge.user.api.domain.SysMenu;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/** SysMenuRepository接口，定义Spring Data JPA方法 */
@Repository
public interface SysMenuRepository extends JpaRepository<SysMenu, Long>, JpaSpecificationExecutor<SysMenu>, SysMenuRepositoryCustom {

    List<SysMenu> findByParentId(Long parentId);

    List<SysMenu> findByParentIdOrderByMenuIdAsc(Long parentId);

    List<SysMenu> findByPermission(String permission);
}
