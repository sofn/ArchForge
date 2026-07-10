package com.lesofn.archforge.common.repository

import jakarta.persistence.EntityManager
import jakarta.persistence.TypedQuery
import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.CriteriaDelete
import jakarta.persistence.criteria.CriteriaQuery
import jakarta.persistence.criteria.Order
import jakarta.persistence.criteria.Path
import jakarta.persistence.criteria.Predicate
import jakarta.persistence.criteria.Root
import jakarta.persistence.metamodel.SingularAttribute
import spock.lang.Specification

class CriteriaQuerySupportSpec extends Specification {

    EntityManager em = Mock()
    CriteriaBuilder cb = Mock()
    TestSupport support

    def setup() {
        em.getCriteriaBuilder() >> cb
        support = new TestSupport()
        support.entityManager = em
    }

    private SingularAttribute attr(String name) {
        def a = Mock(SingularAttribute)
        a.getName() >> name
        return a
    }

    // ==================== findByNotDeleted ====================

    def "findByNotDeleted builds correct criteria query"() {
        given:
        def cq = Mock(CriteriaQuery)
        def root = Mock(Root)
        def path = Mock(Path)
        def pred = Mock(Predicate)
        def order = Mock(Order)
        def typedQuery = Mock(TypedQuery)

        cb.createQuery(Object) >> cq
        cq.from(Object) >> root
        root.get(_ as SingularAttribute) >> path
        cb.equal(_, _) >> pred
        cb.asc(_) >> order

        cq.select(_) >> cq
        cq.where(_) >> cq
        cq.orderBy(_) >> cq
        em.createQuery(_ as CriteriaQuery) >> typedQuery
        typedQuery.getResultList() >> ["result"]

        when:
        def result = support.findByNotDeleted(Object, attr("deleted"), attr("sort"))

        then:
        result == ["result"]
    }

    // ==================== findByActive ====================

    def "findByActive builds criteria query with status filter"() {
        given:
        def cq = Mock(CriteriaQuery)
        def root = Mock(Root)
        def path = Mock(Path)
        def pred = Mock(Predicate)
        def order = Mock(Order)
        def typedQuery = Mock(TypedQuery)

        cb.createQuery(Object) >> cq
        cq.from(Object) >> root
        root.get(_ as SingularAttribute) >> path
        cb.equal(_, _) >> pred
        cb.asc(_) >> order

        cq.select(_) >> cq
        cq.where(_) >> cq
        cq.orderBy(_) >> cq
        em.createQuery(_ as CriteriaQuery) >> typedQuery
        typedQuery.getResultList() >> ["active"]

        when:
        def result = support.findByActive(Object, attr("deleted"), attr("status"), 1, attr("sort"))

        then:
        result == ["active"]
    }

    // ==================== deleteByAttribute ====================

    def "deleteByAttribute builds correct criteria delete"() {
        given:
        def cd = Mock(CriteriaDelete)
        def root = Mock(Root)
        def path = Mock(Path)
        def pred = Mock(Predicate)
        def query = Mock(jakarta.persistence.Query)

        cb.createCriteriaDelete(Object) >> cd
        cd.from(Object) >> root
        root.get(_ as SingularAttribute) >> path
        cb.equal(_, _) >> pred
        cd.where(_) >> cd
        em.createQuery(_ as CriteriaDelete) >> query
        query.executeUpdate() >> 5

        when:
        def count = support.deleteByAttribute(Object, attr("roleId"), 42L)

        then:
        count == 5
    }

    /** Minimal concrete subclass for testing. */
    static class TestSupport extends CriteriaQuerySupport {
    }
}
