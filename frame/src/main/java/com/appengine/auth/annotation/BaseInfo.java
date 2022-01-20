package com.appengine.auth.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author x-spirit
 * @author sofn
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface BaseInfo {

    String desc() default "";

    ApiStatus status() default ApiStatus.PUBLIC;

    AuthType needAuth() default AuthType.REQUIRED;

    boolean needSSL() default false;
}
