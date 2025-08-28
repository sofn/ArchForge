package com.lesofn.matrixboot.user.domain.query;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.lesofn.matrixboot.user.domain.QSysUser;

public class SysUserPredicates {

    private static final QSysUser qSysUser = QSysUser.sysUser;

    public static Predicate buildQueryPredicate(SysUserQuery query) {
        BooleanExpression predicate = qSysUser.deleted.isFalse();
        
        if (query.getUsername() != null && !query.getUsername().isEmpty()) {
            predicate = predicate.and(qSysUser.username.containsIgnoreCase(query.getUsername()));
        }
        
        if (query.getEmail() != null && !query.getEmail().isEmpty()) {
            predicate = predicate.and(qSysUser.email.containsIgnoreCase(query.getEmail()));
        }
        
        if (query.getPhoneNumber() != null && !query.getPhoneNumber().isEmpty()) {
            predicate = predicate.and(qSysUser.phoneNumber.containsIgnoreCase(query.getPhoneNumber()));
        }
        
        if (query.getEnabled() != null) {
            predicate = predicate.and(qSysUser.status.eq(query.getEnabled() ? 1 : 0));
        }
        
        return predicate;
    }
}