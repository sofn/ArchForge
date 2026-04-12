package com.lesofn.appforge.user.menu.repository;

import com.lesofn.appforge.user.domain.SysMenu;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * SysMenuRepository接口，定义Spring Data JPA方法
 * 实际使用请注入SysMenuRepositoryImpl类，它同时支持Spring Data JPA和QueryDSL
 */
@Repository
public interface SysMenuRepositoryCustom {


    List<SysMenu> selectMenuListByUserId(Long userId);

    List<SysMenu> findMenusByRoleId(Long roleId);
}