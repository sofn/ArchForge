package com.lesofn.archforge.common.utils.query;

import jakarta.persistence.metamodel.SingularAttribute;

/**
 * Type-safe JPQL expression builder using JPA Static Metamodel.
 *
 * <p>
 * Provides static helper methods for building JPQL expression fragments (aggregation, DISTINCT,
 * NULL handling, CASE-WHEN, string functions) without hard-coding field names as raw strings.
 *
 * <p>
 * Usage:
 *
 * <pre>
 * {@code
 * // Instead of "COUNT(DISTINCT o.customer)"
 * SafeExpr.countDistinct(Order_.customer);
 *
 * // Instead of "COALESCE(o.discountRate, 0)"
 * SafeExpr.coalesce(Order_.discountRate, 0);
 * }
 * </pre>
 *
 * <p>
 * For alias-aware expressions (e.g. {@code o.amount}), use {@link AliasExpr}.
 *
 * @author sofn
 * @see AliasExpr
 */
public final class SafeExpr {

    private SafeExpr() {
    }

    // ==================== Aggregate Functions ====================

    /** {@code COUNT(attr)} */
    public static String count(SingularAttribute<?, ?> attr) {
        return "COUNT(" + attr.getName() + ")";
    }

    /** {@code COUNT(DISTINCT attr)} */
    public static String countDistinct(SingularAttribute<?, ?> attr) {
        return "COUNT(DISTINCT " + attr.getName() + ")";
    }

    /** {@code SUM(attr)} */
    public static <T extends Number> String sum(SingularAttribute<?, T> attr) {
        return "SUM(" + attr.getName() + ")";
    }

    /** {@code MIN(attr)} */
    public static <T extends Comparable<T>> String min(SingularAttribute<?, T> attr) {
        return "MIN(" + attr.getName() + ")";
    }

    /** {@code MAX(attr)} */
    public static <T extends Comparable<T>> String max(SingularAttribute<?, T> attr) {
        return "MAX(" + attr.getName() + ")";
    }

    /** {@code AVG(attr)} */
    public static <T extends Number> String avg(SingularAttribute<?, T> attr) {
        return "AVG(" + attr.getName() + ")";
    }

    // ==================== DISTINCT ====================

    /** {@code DISTINCT attr} */
    public static String distinct(SingularAttribute<?, ?> attr) {
        return "DISTINCT " + attr.getName();
    }

    /** {@code DISTINCT first.second} (nested path) */
    public static String distinct(SingularAttribute<?, ?> first, SingularAttribute<?, ?> second) {
        return "DISTINCT " + first.getName() + "." + second.getName();
    }

    // ==================== NULL Handling ====================

    /** {@code COALESCE(attr, defaultValue)} */
    public static <T> String coalesce(SingularAttribute<?, T> attr, T defaultValue) {
        return "COALESCE(" + attr.getName() + ", " + formatLiteral(defaultValue) + ")";
    }

    /** {@code NULLIF(attr, value)} */
    public static <T> String nullif(SingularAttribute<?, T> attr, T value) {
        return "NULLIF(" + attr.getName() + ", " + formatLiteral(value) + ")";
    }

    // ==================== Conditional ====================

    /** {@code CASE WHEN attr = whenValue THEN thenResult ELSE elseResult END} */
    public static <T> String caseWhen(
            SingularAttribute<?, T> attr, T whenValue, Object thenResult, Object elseResult) {
        return String.format(
                "CASE WHEN %s = %s THEN %s ELSE %s END",
                attr.getName(),
                formatLiteral(whenValue),
                formatLiteral(thenResult),
                formatLiteral(elseResult));
    }

    // ==================== String Functions ====================

    /** {@code UPPER(attr)} */
    public static String upper(SingularAttribute<?, String> attr) {
        return "UPPER(" + attr.getName() + ")";
    }

    /** {@code LOWER(attr)} */
    public static String lower(SingularAttribute<?, String> attr) {
        return "LOWER(" + attr.getName() + ")";
    }

    /** {@code CONCAT(first, second)} */
    public static String concat(
            SingularAttribute<?, String> first, SingularAttribute<?, String> second) {
        return "CONCAT(" + first.getName() + ", " + second.getName() + ")";
    }

    // ==================== Path Helpers ====================

    /** Single-level path: {@code attr.getName()} */
    public static String path(SingularAttribute<?, ?> attr) {
        return attr.getName();
    }

    /** Nested path: {@code first.second} */
    public static String path(SingularAttribute<?, ?> first, SingularAttribute<?, ?> second) {
        return first.getName() + "." + second.getName();
    }

    /** Format a literal value for safe embedding in JPQL. */
    static String formatLiteral(Object value) {
        if (value == null) {
            return "NULL";
        }
        if (value instanceof String s) {
            return "'" + s + "'";
        }
        if (value instanceof Enum<?> e) {
            return "'" + e.name() + "'";
        }
        return value.toString();
    }
}
