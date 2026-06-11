package com.lesofn.archsmith.common.utils.query;

import com.lesofn.archsmith.common.annotation.Query;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;

/**
 * Reflects {@code @Query}-annotated fields on a criteria DTO and builds a JPA Predicate suitable
 * for use inside a Spring Data {@code Specification} lambda.
 *
 * <p>Usage:
 *
 * <pre>{@code
 * Specification<SysUser> spec = (root, q, cb) -> QueryHelp.getPredicate(root, criteria, cb);
 * Page<SysUser> page = repo.findAll(spec, pageable);
 * }</pre>
 *
 * <p>Empty values (null, blank string, empty collection, empty array) are skipped — non-blank
 * values contribute exactly one predicate, joined via {@code AND}.
 *
 * @author sofn
 */
@Slf4j
public final class QueryHelp {

    private QueryHelp() {}

    public static <R, Q> Predicate getPredicate(
            Root<R> root, @Nullable Q criteria, CriteriaBuilder cb) {
        List<Predicate> predicates = new ArrayList<>();
        if (criteria == null) {
            return cb.and(predicates.toArray(new Predicate[0]));
        }
        Map<String, Join<R, ?>> joinCache = new HashMap<>();
        try {
            for (Field field : getAllFields(criteria.getClass(), new ArrayList<>())) {
                Query q = field.getAnnotation(Query.class);
                if (q == null) {
                    continue;
                }
                field.setAccessible(true);
                Object value = field.get(criteria);
                if (isEmpty(value)) {
                    continue;
                }
                if (!q.blurry().isEmpty()) {
                    predicates.add(buildBlurry(root, cb, q.blurry().split(","), value.toString()));
                    continue;
                }
                String attribute = q.propName().isEmpty() ? field.getName() : q.propName();
                @Nullable Join<R, ?> join =
                        q.joinName().isEmpty() ? null : resolveJoin(root, joinCache, q);
                Class<?> fieldType = field.getType();
                predicates.add(
                        buildPredicate(root, cb, join, attribute, fieldType, q.type(), value));
            }
        } catch (ReflectiveOperationException e) {
            log.error("QueryHelp reflection failure on {}", criteria.getClass(), e);
        }
        return cb.and(predicates.toArray(new Predicate[0]));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static <R> Predicate buildPredicate(
            Root<R> root,
            CriteriaBuilder cb,
            @Nullable Join<R, ?> join,
            String attribute,
            Class<?> fieldType,
            Query.Type type,
            Object value) {
        Path<Object> path = expression(attribute, join, root);
        return switch (type) {
            case EQUAL -> cb.equal(path, value);
            case NOT_EQUAL -> cb.notEqual(path, value);
            case GREATER_THAN ->
                    cb.greaterThanOrEqualTo(
                            path.as((Class<Comparable>) fieldType), (Comparable) value);
            case LESS_THAN ->
                    cb.lessThanOrEqualTo(
                            path.as((Class<Comparable>) fieldType), (Comparable) value);
            case LESS_THAN_NQ ->
                    cb.lessThan(path.as((Class<Comparable>) fieldType), (Comparable) value);
            case INNER_LIKE -> cb.like(path.as(String.class), "%" + value + "%");
            case LEFT_LIKE -> cb.like(path.as(String.class), "%" + value);
            case RIGHT_LIKE -> cb.like(path.as(String.class), value + "%");
            case IN -> path.in((Collection<?>) value);
            case NOT_IN -> path.in((Collection<?>) value).not();
            case IS_NULL -> cb.isNull(path);
            case NOT_NULL -> cb.isNotNull(path);
            case BETWEEN -> {
                List<?> bounds = (List<?>) value;
                if (bounds.size() != 2) {
                    yield cb.and();
                }
                Comparable lo = (Comparable) bounds.get(0);
                Comparable hi = (Comparable) bounds.get(1);
                yield cb.between(path.as((Class<Comparable>) lo.getClass()), lo, hi);
            }
            case FIND_IN_SET ->
                    cb.greaterThan(
                            cb.function(
                                    "FIND_IN_SET",
                                    Integer.class,
                                    cb.literal(value.toString()),
                                    root.get(attribute)),
                            0);
        };
    }

    private static <R> Predicate buildBlurry(
            Root<R> root, CriteriaBuilder cb, String[] attributes, String value) {
        List<Predicate> ors = new ArrayList<>(attributes.length);
        for (String a : attributes) {
            ors.add(cb.like(root.get(a.trim()).as(String.class), "%" + value + "%"));
        }
        return cb.or(ors.toArray(new Predicate[0]));
    }

    @SuppressWarnings("unchecked")
    private static <R> Join<R, ?> resolveJoin(
            Root<R> root, Map<String, Join<R, ?>> cache, Query q) {
        Join<R, ?> existing = cache.get(q.joinName());
        if (existing != null) {
            return existing;
        }
        JoinType jt =
                switch (q.join()) {
                    case LEFT -> JoinType.LEFT;
                    case RIGHT -> JoinType.RIGHT;
                    case INNER -> JoinType.INNER;
                };
        Join<R, ?> join = null;
        for (String name : q.joinName().split(">")) {
            join = (join == null) ? root.join(name, jt) : (Join<R, ?>) join.join(name, jt);
        }
        cache.put(q.joinName(), join);
        return join;
    }

    @SuppressWarnings("unchecked")
    private static <R> Path<Object> expression(
            String attribute, @Nullable Join<R, ?> join, Root<R> root) {
        return (Path<Object>) (join == null ? root.get(attribute) : join.get(attribute));
    }

    private static boolean isEmpty(@Nullable Object v) {
        return switch (v) {
            case null -> true;
            case CharSequence cs -> cs.toString().trim().isEmpty();
            case Collection<?> c -> c.isEmpty();
            case Object[] a -> a.length == 0;
            default -> false;
        };
    }

    static List<Field> getAllFields(@Nullable Class<?> type, List<Field> acc) {
        if (type == null || type == Object.class) {
            return acc;
        }
        for (Field f : type.getDeclaredFields()) {
            acc.add(f);
        }
        return getAllFields(type.getSuperclass(), acc);
    }
}
