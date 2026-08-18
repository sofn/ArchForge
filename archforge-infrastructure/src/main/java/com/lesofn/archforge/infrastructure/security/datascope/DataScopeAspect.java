package com.lesofn.archforge.infrastructure.security.datascope;

import com.lesofn.archforge.infrastructure.auth.LoginContext;
import com.lesofn.archforge.infrastructure.auth.model.SystemLoginUser;
import com.lesofn.archforge.infrastructure.user.web.DataScopeEnum;
import com.lesofn.archforge.infrastructure.user.web.RoleInfo;
import java.util.HashSet;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 数据权限 AOP 切面。
 *
 * <p>
 * 在带有 {@link DataPermission} 注解的方法执行前，将当前登录用户的数据权限范围写入
 * {@link DataScopeContextHolder}；方法执行后清理。
 *
 * @author sofn
 */
@Slf4j
@Aspect
@Component
@Order(2)
public class DataScopeAspect {

    @Around("@annotation(dataPermission)")
    public Object around(ProceedingJoinPoint point, DataPermission dataPermission) throws Throwable {
        DataScopeContext context = buildContext(dataPermission);
        if (context != null) {
            DataScopeContextHolder.set(context);
        }
        try {
            return point.proceed();
        } finally {
            DataScopeContextHolder.clear();
        }
    }

    private DataScopeContext buildContext(DataPermission dataPermission) {
        SystemLoginUser user;
        try {
            user = LoginContext.getAdminUser();
        } catch (Exception e) {
            log.debug("No authenticated user, skip data scope: {}", e.getMessage());
            return null;
        }
        if (user == null) {
            return null;
        }

        DataScopeEnum dataScope = DataScopeEnum.ALL;
        java.util.Set<Long> customDeptIds = new HashSet<>();
        RoleInfo roleInfo = user.getRoleInfo();
        if (roleInfo != null && roleInfo.getDataScope() != null) {
            dataScope = roleInfo.getDataScope();
            if (roleInfo.getDeptIdSet() != null) {
                customDeptIds = new HashSet<>(roleInfo.getDeptIdSet());
            }
        }

        return DataScopeContext.builder()
                .dataScope(dataScope)
                .userId(user.getUserId())
                .deptId(user.getDeptId())
                .customDeptIds(customDeptIds)
                .deptAlias(dataPermission.deptAlias())
                .userAlias(dataPermission.userAlias())
                .build();
    }
}
