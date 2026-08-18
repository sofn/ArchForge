package com.lesofn.archforge.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares that a field on a query-criteria DTO contributes a JPA Predicate when processed by
 * {@code QueryHelp}.
 *
 * <p>
 * Usage:
 *
 * <pre>
 * {
 *     &#64;code
 *     &#64;Data
 *     public class SysUserQueryRequest {
 *         &#64;Query(type = Query.Type.INNER_LIKE)
 *         private String username;
 *
 *         &#64;Query(blurry = "username,email,nickname")
 *         private String blurry;
 *
 *         &#64;Query(type = Query.Type.BETWEEN)
 *         private List<LocalDateTime> createTime;
 *
 *         &#64;Query(propName = "id", type = Query.Type.IN, joinName = "dept")
 *         private Set<Long> deptIds;
 *     }
 * }
 * </pre>
 *
 * @author sofn
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Query {

    /** Persistent attribute name. Defaults to the field name. */
    String propName() default "";

    /** Comparison operator. */
    Type type() default Type.EQUAL;

    /**
     * Join target attribute name. Supports nested paths via {@code >} separator (e.g. {@code
     * "dept>parent"}). Empty means no join, query directly on the root.
     */
    String joinName() default "";

    /** Join kind — defaults to LEFT. */
    Join join() default Join.LEFT;

    /**
     * Comma-separated list of attribute names. When non-empty, the value is wrapped in a {@code (a
     * LIKE %v% OR b LIKE %v% OR ...)} disjunction. Only valid for {@code String} fields; {@link
     * #type()} is ignored.
     */
    String blurry() default "";

    /** When {@code true}, LIKE comparisons use {@code LOWER()} for case-insensitive matching. */
    boolean ignoreCase() default false;

    enum Type {
        EQUAL,
        NOT_EQUAL,
        /** Inclusive {@code >=}. */
        GREATER_THAN_OR_EQUAL,
        /** Inclusive {@code <=}. */
        LESS_THAN_OR_EQUAL,
        /** Strict {@code >}. */
        GREATER_THAN,
        /** Strict {@code <}. */
        LESS_THAN,
        /** {@code %v%}. */
        INNER_LIKE,
        /** {@code %v}. */
        LEFT_LIKE,
        /** {@code v%}. */
        RIGHT_LIKE,
        IN,
        NOT_IN,
        /** Value must be a {@code List} of size 2 — {@code [lo, hi]}. */
        BETWEEN,
        NOT_NULL,
        IS_NULL,
        /** {@code FIND_IN_SET(value, attribute) > 0} (MySQL/PostgreSQL with extension). */
        FIND_IN_SET
    }

    enum Join {
        LEFT,
        RIGHT,
        INNER
    }
}
