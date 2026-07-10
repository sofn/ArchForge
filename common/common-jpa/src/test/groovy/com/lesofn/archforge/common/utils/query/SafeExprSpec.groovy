package com.lesofn.archforge.common.utils.query

import jakarta.persistence.metamodel.SingularAttribute
import spock.lang.Specification

class SafeExprSpec extends Specification {

    private SingularAttribute attr(String name) {
        def a = Mock(SingularAttribute)
        a.getName() >> name
        return a
    }

    // ==================== Aggregate Functions ====================

    def "count produces COUNT(attr)"() {
        expect:
        SafeExpr.count(attr("amount")) == "COUNT(amount)"
    }

    def "countDistinct produces COUNT(DISTINCT attr)"() {
        expect:
        SafeExpr.countDistinct(attr("customerId")) == "COUNT(DISTINCT customerId)"
    }

    def "sum produces SUM(attr)"() {
        expect:
        SafeExpr.sum(attr("amount")) == "SUM(amount)"
    }

    def "min produces MIN(attr)"() {
        expect:
        SafeExpr.min(attr("price")) == "MIN(price)"
    }

    def "max produces MAX(attr)"() {
        expect:
        SafeExpr.max(attr("price")) == "MAX(price)"
    }

    def "avg produces AVG(attr)"() {
        expect:
        SafeExpr.avg(attr("score")) == "AVG(score)"
    }

    // ==================== DISTINCT ====================

    def "distinct single attribute"() {
        expect:
        SafeExpr.distinct(attr("status")) == "DISTINCT status"
    }

    def "distinct nested path"() {
        expect:
        SafeExpr.distinct(attr("customer"), attr("name")) == "DISTINCT customer.name"
    }

    // ==================== NULL Handling ====================

    def "coalesce with numeric default"() {
        expect:
        SafeExpr.coalesce(attr("discountRate"), 0) == "COALESCE(discountRate, 0)"
    }

    def "coalesce with string default"() {
        expect:
        SafeExpr.coalesce(attr("remark"), "N/A") == "COALESCE(remark, 'N/A')"
    }

    def "coalesce with null default"() {
        expect:
        SafeExpr.coalesce(attr("field"), null) == "COALESCE(field, NULL)"
    }

    def "nullif produces NULLIF expression"() {
        expect:
        SafeExpr.nullif(attr("status"), 0) == "NULLIF(status, 0)"
    }

    // ==================== Conditional ====================

    def "caseWhen with string values"() {
        expect:
        SafeExpr.caseWhen(attr("status"), "ACTIVE", 1, 0) ==
                "CASE WHEN status = 'ACTIVE' THEN 1 ELSE 0 END"
    }

    def "caseWhen with numeric values"() {
        expect:
        SafeExpr.caseWhen(attr("type"), 1, "YES", "NO") ==
                "CASE WHEN type = 1 THEN 'YES' ELSE 'NO' END"
    }

    // ==================== String Functions ====================

    def "upper produces UPPER(attr)"() {
        expect:
        SafeExpr.upper(attr("orderNo")) == "UPPER(orderNo)"
    }

    def "lower produces LOWER(attr)"() {
        expect:
        SafeExpr.lower(attr("email")) == "LOWER(email)"
    }

    def "concat produces CONCAT(first, second)"() {
        expect:
        SafeExpr.concat(attr("firstName"), attr("lastName")) == "CONCAT(firstName, lastName)"
    }

    // ==================== Path Helpers ====================

    def "path single attribute"() {
        expect:
        SafeExpr.path(attr("userId")) == "userId"
    }

    def "path nested attributes"() {
        expect:
        SafeExpr.path(attr("customer"), attr("name")) == "customer.name"
    }

    // ==================== formatLiteral ====================

    def "formatLiteral handles null"() {
        expect:
        SafeExpr.formatLiteral(null) == "NULL"
    }

    def "formatLiteral handles String"() {
        expect:
        SafeExpr.formatLiteral("hello") == "'hello'"
    }

    def "formatLiteral handles Enum"() {
        expect:
        SafeExpr.formatLiteral(Thread.State.RUNNABLE) == "'RUNNABLE'"
    }

    def "formatLiteral handles Number"() {
        expect:
        SafeExpr.formatLiteral(42) == "42"
        SafeExpr.formatLiteral(3.14) == "3.14"
    }
}
