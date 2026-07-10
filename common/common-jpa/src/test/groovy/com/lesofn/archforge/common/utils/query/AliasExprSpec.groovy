package com.lesofn.archforge.common.utils.query

import jakarta.persistence.metamodel.SingularAttribute
import spock.lang.Specification

class AliasExprSpec extends Specification {

    private SingularAttribute attr(String name) {
        def a = Mock(SingularAttribute)
        a.getName() >> name
        return a
    }

    // ==================== Factory ====================

    def "of creates AliasExpr with given alias"() {
        given:
        def o = AliasExpr.of("o")

        expect:
        o.path(attr("amount")) == "o.amount"
    }

    // ==================== Path Helpers ====================

    def "path single attribute includes alias prefix"() {
        given:
        def o = AliasExpr.of("order")

        expect:
        o.path(attr("status")) == "order.status"
    }

    def "path nested attributes includes alias prefix"() {
        given:
        def o = AliasExpr.of("o")

        expect:
        o.path(attr("customer"), attr("name")) == "o.customer.name"
    }

    // ==================== Aggregate Functions ====================

    def "count with alias"() {
        given:
        def o = AliasExpr.of("o")

        expect:
        o.count(attr("amount")) == "COUNT(o.amount)"
    }

    def "countDistinct with alias"() {
        given:
        def o = AliasExpr.of("o")

        expect:
        o.countDistinct(attr("customerId")) == "COUNT(DISTINCT o.customerId)"
    }

    def "sum with alias"() {
        given:
        def o = AliasExpr.of("o")

        expect:
        o.sum(attr("amount")) == "SUM(o.amount)"
    }

    def "min with alias"() {
        given:
        def o = AliasExpr.of("o")

        expect:
        o.min(attr("price")) == "MIN(o.price)"
    }

    def "max with alias"() {
        given:
        def o = AliasExpr.of("o")

        expect:
        o.max(attr("price")) == "MAX(o.price)"
    }

    def "avg with alias"() {
        given:
        def o = AliasExpr.of("o")

        expect:
        o.avg(attr("score")) == "AVG(o.score)"
    }

    // ==================== DISTINCT ====================

    def "distinct single attribute with alias"() {
        given:
        def o = AliasExpr.of("o")

        expect:
        o.distinct(attr("status")) == "DISTINCT o.status"
    }

    def "distinct nested path with alias"() {
        given:
        def o = AliasExpr.of("o")

        expect:
        o.distinct(attr("customer"), attr("name")) == "DISTINCT o.customer.name"
    }

    // ==================== NULL Handling ====================

    def "coalesce with alias and numeric default"() {
        given:
        def o = AliasExpr.of("o")

        expect:
        o.coalesce(attr("discountRate"), 0) == "COALESCE(o.discountRate, 0)"
    }

    def "coalesce with alias and string default"() {
        given:
        def o = AliasExpr.of("o")

        expect:
        o.coalesce(attr("remark"), "N/A") == "COALESCE(o.remark, 'N/A')"
    }

    def "nullif with alias"() {
        given:
        def o = AliasExpr.of("o")

        expect:
        o.nullif(attr("status"), 0) == "NULLIF(o.status, 0)"
    }

    // ==================== Conditional ====================

    def "caseWhen with alias"() {
        given:
        def o = AliasExpr.of("o")

        expect:
        o.caseWhen(attr("status"), "PAID", 1, 0) ==
                "CASE WHEN o.status = 'PAID' THEN 1 ELSE 0 END"
    }

    // ==================== String Functions ====================

    def "upper with alias"() {
        given:
        def o = AliasExpr.of("o")

        expect:
        o.upper(attr("orderNo")) == "UPPER(o.orderNo)"
    }

    def "lower with alias"() {
        given:
        def o = AliasExpr.of("o")

        expect:
        o.lower(attr("email")) == "LOWER(o.email)"
    }

    def "concat with alias"() {
        given:
        def o = AliasExpr.of("o")

        expect:
        o.concat(attr("firstName"), attr("lastName")) == "CONCAT(o.firstName, o.lastName)"
    }

    // ==================== Different Aliases ====================

    def "multiple aliases produce independent paths"() {
        given:
        def o = AliasExpr.of("o")
        def u = AliasExpr.of("u")

        expect:
        o.path(attr("amount")) == "o.amount"
        u.path(attr("username")) == "u.username"
        o.count(attr("id")) == "COUNT(o.id)"
        u.count(attr("id")) == "COUNT(u.id)"
    }
}
