package com.lesofn.archsmith.common.utils.query

import com.lesofn.archsmith.common.annotation.Query
import jakarta.persistence.criteria.CriteriaBuilder
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
        cb.like(_, _) >> pred
        cb.equal(_, _) >> pred
        cb.and(_ as Predicate[]) >> pred

        def crit = new Sample(username: "alice", status: 1, blurry: null, ignored: "x")

        when:
        QueryHelp.getPredicate(root, crit, cb)

        then:
        1 * cb.like(_, "%alice%")
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
        cb.like(_, _) >> pred
        cb.or(_ as Predicate[]) >> pred
        cb.and(_ as Predicate[]) >> pred

        def crit = new Sample(blurry: "x")

        when:
        QueryHelp.getPredicate(root, crit, cb)

        then:
        2 * cb.like(_, "%x%")
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
        0 * cb.like(_, _)
        0 * cb.equal(_, _)
        0 * cb.or(_ as Predicate[])
    }
}
