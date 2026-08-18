package com.lesofn.archforge.infrastructure.security.datascope;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 数据权限注解。
 *
 * <p>
 * 标注后，AOP 会从当前登录用户角色中读取数据权限范围，并注入到请求上下文中；
 * 业务层可结合 {@link DataScopeContext} 与 JPA {@code Specification} 追加过滤条件。
 *
 * @author sofn
 */
@Target({
        ElementType.METHOD
})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DataPermission {

    /** 部门 ID 字段别名 */
    String deptAlias() default "deptId";

    /** 用户 ID 字段别名 */
    String userAlias() default "userId";
}
