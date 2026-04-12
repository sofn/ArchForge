package com.lesofn.appforge.user.menu.repository;

import com.lesofn.appforge.user.domain.QSysMenu;
import com.lesofn.appforge.user.domain.QSysRoleMenu;
import com.lesofn.appforge.user.domain.QSysUser;
import com.lesofn.appforge.user.domain.SysMenu;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * SysMenuRepository实现类，同时支持Spring Data JPA和QueryDSL
 * 上层只需要使用这个类即可
 */
@Repository
@RequiredArgsConstructor
public class SysMenuRepositoryImpl implements SysMenuRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<SysMenu> selectMenuListByUserId(Long userId) {
        QSysMenu menu = QSysMenu.sysMenu;
        QSysRoleMenu roleMenu = QSysRoleMenu.sysRoleMenu;
        QSysUser user = QSysUser.sysUser;

        return queryFactory
                .selectDistinct(menu)
                .from(menu)
                .leftJoin(roleMenu).on(menu.menuId.eq(roleMenu.menuId))
                .leftJoin(user).on(roleMenu.roleId.eq(user.roleId))
                .where(user.userId.eq(userId)
                        .and(menu.status.eq(1))
                        .and(menu.deleted.eq(false))
                )
                .orderBy(menu.parentId.asc())
                .fetch();
    }

    @Override
    public List<SysMenu> findMenusByRoleId(Long roleId) {
        QSysMenu menu = QSysMenu.sysMenu;
        QSysRoleMenu roleMenu = QSysRoleMenu.sysRoleMenu;

        return queryFactory
                .selectDistinct(menu)
                .from(menu)
                .join(roleMenu).on(menu.menuId.eq(roleMenu.menuId))
                .where(roleMenu.roleId.eq(roleId)
                        .and(menu.status.eq(1))
                        .and(menu.deleted.eq(false))
                )
                .orderBy(menu.parentId.asc(), menu.menuId.asc())
                .fetch();
    }
}