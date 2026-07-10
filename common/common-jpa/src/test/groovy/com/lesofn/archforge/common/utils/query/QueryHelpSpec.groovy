package com.lesofn.archforge.common.utils.query

import com.lesofn.archforge.common.annotation.Query
import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.Expression
import jakarta.persistence.criteria.Path
import jakarta.persistence.criteria.Predicate
import jakarta.persistence.criteria.Root
import spock.lang.Specification

class QueryHelpSpec extends Specification {

    static class Sample {
        @Query(type = Query.Type.INNER_LIKE)
        String username
        @Query
        Integer status
        @Query(blurry = "username,email")
        String blurry
        String ignored
    }

    static class BetweenSample {
        @Query(type = Query.Type.BETWEEN)
        List<Integer> createTime
    }

    // Fix 4: ignoreCase support
    static class IgnoreCaseSample {
        @Query(type = Query.Type.INNER_LIKE, ignoreCase = true)
        String username
    }

    // Fix 4: blurry with ignoreCase
    static class IgnoreCaseBlurrySample {
        @Query(blurry = "username,email", ignoreCase = true)
        String blurry
    }

    // Fix 5: renamed comparison types
    static class ComparisonSample {
        @Query(type = Query.Type.GREATER_THAN_OR_EQUAL)
        Integer minAge
        @Query(type = Query.Type.LESS_THAN_OR_EQUAL)
        Integer maxAge
        @Query(type = Query.Type.GREATER_THAN)
        Integer strictGt
        @Query(type = Query.Type.LESS_THAN)
        Integer strictLt
    }

    def "null criteria returns conjunction of an empty list"() {
        given:
        def root = Mock(Root)
        def cb = Mock(CriteriaBuilder)
        def stubPred = Mock(Predicate)

        when:
        def p = QueryHelp.getPredicate(root, null, cb)

        then:
        1 * cb.and(_ as Predicate[]) >> stubPred
        p != null
    }

    def "only non-null annotated fields contribute predicates"() {
        given:
        def root = Mock(Root)
        def path = Mock(Path)
        def cb = Mock(CriteriaBuilder)
        def pred = Mock(Predicate)
        root.get(_) >> path
        path.as(_) >> path
        cb.like(_, _, _) >> pred
        cb.equal(_, _) >> pred
        cb.and(_ as Predicate[]) >> pred

        def crit = new Sample(username: "alice", status: 1, blurry: null, ignored: "x")

        when:
        QueryHelp.getPredicate(root, crit, cb)

        then:
        // Fix 1: LIKE now uses escape char (3-arg cb.like)
        1 * cb.like(_, "%alice%", '\\' as char)
        1 * cb.equal(_, 1)
    }

    def "blurry value expands to OR of LIKE per field"() {
        given:
        def root = Mock(Root)
        def path = Mock(Path)
        def cb = Mock(CriteriaBuilder)
        def pred = Mock(Predicate)
        root.get(_) >> path
        path.as(_) >> path
        cb.like(_, _, _) >> pred
        cb.or(_ as Predicate[]) >> pred
        cb.and(_ as Predicate[]) >> pred

        def crit = new Sample(blurry: "x")

        when:
        QueryHelp.getPredicate(root, crit, cb)

        then:
        2 * cb.like(_, "%x%", '\\' as char)
        1 * cb.or(_ as Predicate[])
    }

    def "BETWEEN with two-element list builds a between predicate"() {
        given:
        def root = Mock(Root)
        def path = Mock(Path)
        def cb = Mock(CriteriaBuilder)
        def pred = Mock(Predicate)
        root.get(_) >> path
        path.as(_) >> path
        cb.between(_, _, _) >> pred
        cb.and(_ as Predicate[]) >> pred

        def crit = new BetweenSample(createTime: [1, 5])

        when:
        QueryHelp.getPredicate(root, crit, cb)

        then:
        1 * cb.between(_, 1, 5)
    }

    def "blank string criteria field is treated as empty"() {
        given:
        def root = Mock(Root)
        def cb = Mock(CriteriaBuilder)
        cb.and(_ as Predicate[]) >> Mock(Predicate)

        def crit = new Sample(username: "  ", blurry: "")

        when:
        QueryHelp.getPredicate(root, crit, cb)

        then:
        // No like/equal interactions because all values are empty/blank.
        0 * cb.like(_, _, _)
        0 * cb.equal(_, _)
        0 * cb.or(_ as Predicate[])
    }

    // ==================== Fix 1: LIKE wildcard escaping ====================

    def "LIKE value containing % is escaped"() {
        given:
        def root = Mock(Root)
        def path = Mock(Path)
        def cb = Mock(CriteriaBuilder)
        def pred = Mock(Predicate)
        root.get(_) >> path
        path.as(_) >> path
        cb.like(_, _, _) >> pred
        cb.and(_ as Predicate[]) >> pred

        def crit = new Sample(username: "%admin%")

        when:
        QueryHelp.getPredicate(root, crit, cb)

        then:
        1 * cb.like(_, "%\\%admin\\%%", '\\' as char)
    }

    def "LIKE value containing _ is escaped"() {
        given:
        def root = Mock(Root)
        def path = Mock(Path)
        def cb = Mock(CriteriaBuilder)
        def pred = Mock(Predicate)
        root.get(_) >> path
        path.as(_) >> path
        cb.like(_, _, _) >> pred
        cb.and(_ as Predicate[]) >> pred

        def crit = new Sample(username: "a_b")

        when:
        QueryHelp.getPredicate(root, crit, cb)

        then:
        1 * cb.like(_, "%a\\_b%", '\\' as char)
    }

    def "blurry value containing % is escaped"() {
        given:
        def root = Mock(Root)
        def path = Mock(Path)
        def cb = Mock(CriteriaBuilder)
        def pred = Mock(Predicate)
        root.get(_) >> path
        path.as(_) >> path
        cb.like(_, _, _) >> pred
        cb.or(_ as Predicate[]) >> pred
        cb.and(_ as Predicate[]) >> pred

        def crit = new Sample(blurry: "100%")

        when:
        QueryHelp.getPredicate(root, crit, cb)

        then:
        2 * cb.like(_, "%100\\%%", '\\' as char)
    }

    // ==================== Fix 3: BETWEEN validation ====================

    def "BETWEEN with wrong size list throws IllegalArgumentException"() {
        given:
        def root = Mock(Root)
        def path = Mock(Path)
        def cb = Mock(CriteriaBuilder)
        root.get(_) >> path
        path.as(_) >> path
        cb.and(_ as Predicate[]) >> Mock(Predicate)

        def crit = new BetweenSample(createTime: [1, 2, 3])

        when:
        QueryHelp.getPredicate(root, crit, cb)

        then:
        thrown(IllegalArgumentException)
    }

    def "BETWEEN with single element throws IllegalArgumentException"() {
        given:
        def root = Mock(Root)
        def path = Mock(Path)
        def cb = Mock(CriteriaBuilder)
        root.get(_) >> path
        path.as(_) >> path
        cb.and(_ as Predicate[]) >> Mock(Predicate)

        def crit = new BetweenSample(createTime: [1])

        when:
        QueryHelp.getPredicate(root, crit, cb)

        then:
        thrown(IllegalArgumentException)
    }

    // ==================== Fix 4: ignoreCase ====================

    def "INNER_LIKE with ignoreCase uses LOWER()"() {
        given:
        def root = Mock(Root)
        def path = Mock(Path)
        def lowerExpr = Mock(Expression)
        def cb = Mock(CriteriaBuilder)
        def pred = Mock(Predicate)
        root.get(_) >> path
        path.as(_) >> path
        cb.and(_ as Predicate[]) >> pred

        def crit = new IgnoreCaseSample(username: "Alice")

        when:
        QueryHelp.getPredicate(root, crit, cb)

        then:
        // ignoreCase wraps the path in LOWER() and lowercases the pattern
        1 * cb.lower(_) >> lowerExpr
        1 * cb.like(lowerExpr, "%alice%", _ as char) >> pred
    }

    def "blurry with ignoreCase uses LOWER()"() {
        given:
        def root = Mock(Root)
        def path = Mock(Path)
        def lowerExpr = Mock(Expression)
        def cb = Mock(CriteriaBuilder)
        def pred = Mock(Predicate)
        root.get(_) >> path
        path.as(_) >> path
        cb.or(_ as Predicate[]) >> pred
        cb.and(_ as Predicate[]) >> pred

        def crit = new IgnoreCaseBlurrySample(blurry: "Admin")

        when:
        QueryHelp.getPredicate(root, crit, cb)

        then:
        // ignoreCase wraps each blurry field path in LOWER()
        2 * cb.lower(_) >> lowerExpr
        2 * cb.like(lowerExpr, "%admin%", _ as char) >> pred
    }

    // ==================== Fix 5: renamed comparison types ====================

    def "GREATER_THAN_OR_EQUAL produces inclusive >= predicate"() {
        given:
        def root = Mock(Root)
        def path = Mock(Path)
        def cb = Mock(CriteriaBuilder)
        def pred = Mock(Predicate)
        root.get(_) >> path
        path.as(_) >> path
        cb.greaterThanOrEqualTo(_, _) >> pred
        cb.and(_ as Predicate[]) >> pred

        def crit = new ComparisonSample(minAge: 18)

        when:
        QueryHelp.getPredicate(root, crit, cb)

        then:
        1 * cb.greaterThanOrEqualTo(_, 18)
    }

    def "LESS_THAN_OR_EQUAL produces inclusive <= predicate"() {
        given:
        def root = Mock(Root)
        def path = Mock(Path)
        def cb = Mock(CriteriaBuilder)
        def pred = Mock(Predicate)
        root.get(_) >> path
        path.as(_) >> path
        cb.lessThanOrEqualTo(_, _) >> pred
        cb.and(_ as Predicate[]) >> pred

        def crit = new ComparisonSample(maxAge: 65)

        when:
        QueryHelp.getPredicate(root, crit, cb)

        then:
        1 * cb.lessThanOrEqualTo(_, 65)
    }

    def "GREATER_THAN produces strict > predicate"() {
        given:
        def root = Mock(Root)
        def path = Mock(Path)
        def cb = Mock(CriteriaBuilder)
        def pred = Mock(Predicate)
        root.get(_) >> path
        path.as(_) >> path
        cb.greaterThan(_, _ as Comparable) >> pred
        cb.and(_ as Predicate[]) >> pred

        def crit = new ComparisonSample(strictGt: 100)

        when:
        QueryHelp.getPredicate(root, crit, cb)

        then:
        1 * cb.greaterThan(_, 100 as Comparable)
    }

    def "LESS_THAN produces strict < predicate"() {
        given:
        def root = Mock(Root)
        def path = Mock(Path)
        def cb = Mock(CriteriaBuilder)
        def pred = Mock(Predicate)
        root.get(_) >> path
        path.as(_) >> path
        cb.lessThan(_, _ as Comparable) >> pred
        cb.and(_ as Predicate[]) >> pred

        def crit = new ComparisonSample(strictLt: 0)

        when:
        QueryHelp.getPredicate(root, crit, cb)

        then:
        1 * cb.lessThan(_, 0 as Comparable)
    }

    // ==================== Fix 7: synthetic fields ====================

    def "getAllFields collects fields from class hierarchy"() {
        expect:
        def fields = QueryHelp.getAllFields(Sample, [])
        fields*.name.containsAll(["username", "status", "blurry", "ignored"])
    }
}
