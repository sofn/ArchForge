package com.lesofn.archforge.common.utils.query;

import jakarta.persistence.metamodel.SingularAttribute;

/**
 * Alias-aware, type-safe JPQL expression factory using JPA Static Metamodel.
 *
 * <p>Unlike {@link SafeExpr} (which produces bare attribute names), {@code AliasExpr} prefixes
 * every path with the query alias, so the generated fragments can be embedded directly into JPQL.
 *
 * <p>Usage:
 *
 * <pre>{@code
 * AliasExpr o = AliasExpr.of("o");
 *
 * // "COUNT(DISTINCT o.customer)" instead of "COUNT(DISTINCT customer)"
 * o.countDistinct(Order_.customer);
 *
 * // "COALESCE(o.discountRate, 0)"
 * o.coalesce(Order_.discountRate, 0);
 *
 * // "UPPER(o.orderNo)"
 * o.upper(Order_.orderNo);
 * }</pre>
 *
 * @author sofn
 * @see SafeExpr
 */
public final class AliasExpr {

    private final String alias;

    private AliasExpr(String alias) {
        this.alias = alias;
    }

    /** Create an expression factory bound to the given JPQL alias. */
    public static AliasExpr of(String alias) {
        return new AliasExpr(alias);
    }

    // ==================== Path Helpers ====================

    /** Single-level path: {@code alias.attr} */
    public String path(SingularAttribute<?, ?> attr) {
        return alias + "." + attr.getName();
    }

    /** Nested path: {@code alias.first.second} */
    public String path(SingularAttribute<?, ?> first, SingularAttribute<?, ?> second) {
        return alias + "." + first.getName() + "." + second.getName();
    }

    // ==================== Aggregate Functions ====================

    /** {@code COUNT(alias.attr)} */
    public String count(SingularAttribute<?, ?> attr) {
        return "COUNT(" + path(attr) + ")";
    }

    /** {@code COUNT(DISTINCT alias.attr)} */
    public String countDistinct(SingularAttribute<?, ?> attr) {
        return "COUNT(DISTINCT " + path(attr) + ")";
    }

    /** {@code SUM(alias.attr)} */
    public <T extends Number> String sum(SingularAttribute<?, T> attr) {
        return "SUM(" + path(attr) + ")";
    }

    /** {@code MIN(alias.attr)} */
    public <T extends Comparable<T>> String min(SingularAttribute<?, T> attr) {
        return "MIN(" + path(attr) + ")";
    }

    /** {@code MAX(alias.attr)} */
    public <T extends Comparable<T>> String max(SingularAttribute<?, T> attr) {
        return "MAX(" + path(attr) + ")";
    }

    /** {@code AVG(alias.attr)} */
    public <T extends Number> String avg(SingularAttribute<?, T> attr) {
        return "AVG(" + path(attr) + ")";
    }

    // ==================== DISTINCT ====================

    /** {@code DISTINCT alias.attr} */
    public String distinct(SingularAttribute<?, ?> attr) {
        return "DISTINCT " + path(attr);
    }

    /** {@code DISTINCT alias.first.second} (nested path) */
    public String distinct(SingularAttribute<?, ?> first, SingularAttribute<?, ?> second) {
        return "DISTINCT " + path(first, second);
    }

    // ==================== NULL Handling ====================

    /** {@code COALESCE(alias.attr, defaultValue)} */
    public <T> String coalesce(SingularAttribute<?, T> attr, T defaultValue) {
        return "COALESCE(" + path(attr) + ", " + SafeExpr.formatLiteral(defaultValue) + ")";
    }

    /** {@code NULLIF(alias.attr, value)} */
    public <T> String nullif(SingularAttribute<?, T> attr, T value) {
        return "NULLIF(" + path(attr) + ", " + SafeExpr.formatLiteral(value) + ")";
    }

    // ==================== Conditional ====================

    /** {@code CASE WHEN alias.attr = whenValue THEN thenResult ELSE elseResult END} */
    public <T> String caseWhen(
            SingularAttribute<?, T> attr, T whenValue, Object thenResult, Object elseResult) {
        return String.format(
                "CASE WHEN %s = %s THEN %s ELSE %s END",
                path(attr),
                SafeExpr.formatLiteral(whenValue),
                SafeExpr.formatLiteral(thenResult),
                SafeExpr.formatLiteral(elseResult));
    }

    // ==================== String Functions ====================

    /** {@code UPPER(alias.attr)} */
    public String upper(SingularAttribute<?, String> attr) {
        return "UPPER(" + path(attr) + ")";
    }

    /** {@code LOWER(alias.attr)} */
    public String lower(SingularAttribute<?, String> attr) {
        return "LOWER(" + path(attr) + ")";
    }

    /** {@code CONCAT(alias.first, alias.second)} */
    public String concat(SingularAttribute<?, String> first, SingularAttribute<?, String> second) {
        return "CONCAT(" + path(first) + ", " + path(second) + ")";
    }
}
