package com.lesofn.appforge.user.domain.query;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.lesofn.appforge.user.domain.QSysMenu;

public class SysMenuPredicates {

    private static final QSysMenu qSysMenu = QSysMenu.sysMenu;

    public static Predicate buildQueryPredicate() {
        return qSysMenu.deleted.isFalse();
    }
    
    public static Predicate buildActiveMenusPredicate() {
        // 查询未删除且状态为1的菜单
        return qSysMenu.deleted.isFalse().and(qSysMenu.status.eq(1));
    }
    
    public static Predicate buildMenusByParentIdPredicate(Long parentId) {
        // 查询指定父ID的菜单
        return qSysMenu.parentId.eq(parentId).and(buildQueryPredicate());
    }
    
    public static Predicate buildMenusByPermissionPredicate(String permission) {
        // 查询指定权限的菜单
        return qSysMenu.permission.eq(permission).and(buildQueryPredicate());
    }
}