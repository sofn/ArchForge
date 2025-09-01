package com.lesofn.appboot.user.domain.query;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.lesofn.appboot.user.domain.QSysRole;

public class SysRolePredicates {

    private static final QSysRole qSysRole = QSysRole.sysRole;

    public static Predicate buildQueryPredicate() {
        // 默认只查询未删除的角色
        return qSysRole.deleted.isFalse();
    }
    
    public static Predicate buildActiveRolesPredicate() {
        // 查询未删除且状态为1的角色
        return qSysRole.deleted.isFalse().and(qSysRole.status.eq((short) 1));
    }
}