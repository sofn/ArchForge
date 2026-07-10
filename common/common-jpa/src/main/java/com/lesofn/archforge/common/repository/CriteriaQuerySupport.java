package com.lesofn.archforge.common.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaDelete;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.metamodel.SingularAttribute;
import java.util.List;

/**
 * Base class for custom repository implementations that use JPA Criteria API + Hibernate Static
 * Metamodel for type-safe queries.
 *
 * <p>
 * Subclasses are auto-instantiated by Spring Data JPA. The correct {@link EntityManager} is
 * injected based on the {@code entityManagerFactoryRef} configured in
 * {@code @EnableJpaRepositories}.
 *
 * @author sofn
 */
public abstract class CriteriaQuerySupport {

    @PersistenceContext(unitName = "user")
    protected EntityManager entityManager;

    protected CriteriaBuilder cb() {
        return entityManager.getCriteriaBuilder();
    }

    /**
     * {@code SELECT e FROM Entity e WHERE e.deleted = false ORDER BY orderAttr ASC}
     *
     * @param entityClass entity type
     * @param deletedAttr deleted attribute (from metamodel)
     * @param orderAttr attribute to order by (ascending)
     */
    protected <E> List<E> findByNotDeleted(
            Class<E> entityClass,
            SingularAttribute<? super E, Boolean> deletedAttr,
            SingularAttribute<? super E, ?> orderAttr) {
        CriteriaBuilder cb = cb();
        CriteriaQuery<E> cq = cb.createQuery(entityClass);
        Root<E> root = cq.from(entityClass);
        cq.select(root)
                .where(cb.equal(root.get(deletedAttr), false))
                .orderBy(cb.asc(root.get(orderAttr)));
        return entityManager.createQuery(cq).getResultList();
    }

    /**
     * {@code SELECT e FROM Entity e WHERE e.deleted = false AND e.status = activeValue ORDER BY
     * orderAttr ASC}
     *
     * @param entityClass entity type
     * @param deletedAttr deleted attribute (from metamodel)
     * @param statusAttr status attribute (from metamodel)
     * @param activeValue value that means "active" (e.g. 1, (short) 1)
     * @param orderAttr attribute to order by (ascending)
     */
    protected <E, S> List<E> findByActive(
            Class<E> entityClass,
            SingularAttribute<? super E, Boolean> deletedAttr,
            SingularAttribute<? super E, S> statusAttr,
            S activeValue,
            SingularAttribute<? super E, ?> orderAttr) {
        CriteriaBuilder cb = cb();
        CriteriaQuery<E> cq = cb.createQuery(entityClass);
        Root<E> root = cq.from(entityClass);
        cq.select(root)
                .where(
                        cb.equal(root.get(deletedAttr), false),
                        cb.equal(root.get(statusAttr), activeValue))
                .orderBy(cb.asc(root.get(orderAttr)));
        return entityManager.createQuery(cq).getResultList();
    }

    /**
     * {@code DELETE FROM Entity e WHERE e.attr = value}
     *
     * @return number of deleted rows
     */
    protected <E, V> int deleteByAttribute(
            Class<E> entityClass, SingularAttribute<? super E, V> attr, V value) {
        CriteriaBuilder cb = cb();
        CriteriaDelete<E> cd = cb.createCriteriaDelete(entityClass);
        Root<E> root = cd.from(entityClass);
        cd.where(cb.equal(root.get(attr), value));
        return entityManager.createQuery(cd).executeUpdate();
    }

    /** Execute a typed query and return its result list. */
    protected <T> List<T> execute(TypedQuery<T> query) {
        return query.getResultList();
    }
}
