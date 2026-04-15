package com.lesofn.archsmith.user.menu.repository;

import com.lesofn.archsmith.user.domain.SysMenu;
import java.util.List;
import org.springframework.stereotype.Repository;

/**
 * SysMenuRepository接口，定义Spring Data JPA方法 实际使用请注入SysMenuRepositoryImpl类，它同时支持Spring Data
 * JPA和QueryDSL
 */
@Repository
public interface SysMenuRepositoryCustom {

    List<SysMenu> selectMenuListByUserId(Long userId);

    List<SysMenu> findMenusByRoleId(Long roleId);
}
