package com.lesofn.archforge.common.utils.query;

import com.lesofn.archforge.common.annotation.Query;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
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

    // Fix 2: ClassValue cache — parse field metadata once per criteria class
    private static final ClassValue<List<FieldMeta>> FIELD_CACHE =
            new ClassValue<>() {
                @Override
                protected List<FieldMeta> computeValue(Class<?> type) {
                    List<FieldMeta> metas = new ArrayList<>();
                    for (Field f : getAllFields(type, new ArrayList<>())) {
                        if (f.isSynthetic()) {
                            // Fix 7: skip synthetic fields ($jacocoData, etc.)
                            continue;
                        }
                        Query q = f.getAnnotation(Query.class);
                        if (q == null) {
                            continue;
                        }
                        f.setAccessible(true);
                        metas.add(new FieldMeta(f, q));
                    }
                    return List.copyOf(metas);
                }
            };

    public static <R, Q> Predicate getPredicate(
            Root<R> root, @Nullable Q criteria, CriteriaBuilder cb) {
        List<Predicate> predicates = new ArrayList<>();
        if (criteria == null) {
            return cb.and(predicates.toArray(new Predicate[0]));
        }
        Map<String, Join<R, ?>> joinCache = new HashMap<>();
        try {
            for (FieldMeta fm : FIELD_CACHE.get(criteria.getClass())) {
                Object value = fm.field().get(criteria);
                if (isEmpty(value)) {
                    continue;
                }
                Query q = fm.query();
                if (!q.blurry().isEmpty()) {
                    predicates.add(
                            buildBlurry(
                                    root,
                                    cb,
                                    q.blurry().split(","),
                                    value.toString(),
                                    q.ignoreCase()));
                    continue;
                }
                String attribute = q.propName().isEmpty() ? fm.field().getName() : q.propName();
                @Nullable Join<R, ?> join =
                        q.joinName().isEmpty() ? null : resolveJoin(root, joinCache, q);
                Class<?> fieldType = fm.field().getType();
                predicates.add(
                        buildPredicate(
                                root,
                                cb,
                                join,
                                attribute,
                                fieldType,
                                q.type(),
                                value,
                                q.ignoreCase()));
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
            Object value,
            boolean ignoreCase) {
        // Fix 6: FIND_IN_SET now goes through the same expression() path as other types
        Path<Object> path = expression(attribute, join, root);
        return switch (type) {
            case EQUAL -> cb.equal(path, value);
            case NOT_EQUAL -> cb.notEqual(path, value);
            // Fix 5: renamed — inclusive >=
            case GREATER_THAN_OR_EQUAL ->
                    cb.greaterThanOrEqualTo(
                            path.as((Class<Comparable>) fieldType), (Comparable) value);
            // Fix 5: renamed — inclusive <=
            case LESS_THAN_OR_EQUAL ->
                    cb.lessThanOrEqualTo(
                            path.as((Class<Comparable>) fieldType), (Comparable) value);
            // Fix 5: strict >
            case GREATER_THAN ->
                    cb.greaterThan(path.as((Class<Comparable>) fieldType), (Comparable) value);
            // Fix 5: strict <
            case LESS_THAN ->
                    cb.lessThan(path.as((Class<Comparable>) fieldType), (Comparable) value);
            // Fix 1 + Fix 4: LIKE with escape + optional ignoreCase
            case INNER_LIKE -> buildLike(cb, path, "%" + escapeLike(value) + "%", ignoreCase);
            case LEFT_LIKE -> buildLike(cb, path, "%" + escapeLike(value), ignoreCase);
            case RIGHT_LIKE -> buildLike(cb, path, escapeLike(value) + "%", ignoreCase);
            case IN -> path.in((Collection<?>) value);
            case NOT_IN -> path.in((Collection<?>) value).not();
            case BETWEEN -> {
                // Fix 3: throw instead of returning silent empty predicate
                List<?> bounds = (List<?>) value;
                if (bounds.size() != 2) {
                    throw new IllegalArgumentException(
                            "BETWEEN requires exactly 2 elements, got "
                                    + bounds.size()
                                    + " for attribute '"
                                    + attribute
                                    + "'");
                }
                Comparable lo = (Comparable) bounds.get(0);
                Comparable hi = (Comparable) bounds.get(1);
                yield cb.between(path.as((Class<Comparable>) lo.getClass()), lo, hi);
            }
            case IS_NULL -> cb.isNull(path);
            case NOT_NULL -> cb.isNotNull(path);
            case FIND_IN_SET ->
                    cb.greaterThan(
                            cb.function(
                                    "FIND_IN_SET",
                                    Integer.class,
                                    cb.literal(value.toString()),
                                    path),
                            0);
        };
    }

    // Fix 4: ignoreCase support for blurry
    private static <R> Predicate buildBlurry(
            Root<R> root,
            CriteriaBuilder cb,
            String[] attributes,
            String value,
            boolean ignoreCase) {
        // Fix 1: escape LIKE wildcards in blurry value
        String escaped = "%" + escapeLike(value) + "%";
        List<Predicate> ors = new ArrayList<>(attributes.length);
        for (String a : attributes) {
            Path<?> attrPath = root.get(a.trim());
            ors.add(buildLike(cb, attrPath, escaped, ignoreCase));
        }
        return cb.or(ors.toArray(new Predicate[0]));
    }

    // Fix 1: escape LIKE wildcards (%, _, \) in user input
    private static String escapeLike(Object value) {
        return value.toString().replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }

    // Fix 4: build a LIKE predicate, optionally case-insensitive via LOWER()
    private static Predicate buildLike(
            CriteriaBuilder cb, Path<?> path, String pattern, boolean ignoreCase) {
        Expression<String> expr = path.as(String.class);
        if (ignoreCase) {
            return cb.like(cb.lower(expr), pattern.toLowerCase(), '\\');
        }
        return cb.like(expr, pattern, '\\');
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

    // Fix 7: filtering of synthetic fields is done in FIELD_CACHE.computeValue
    static List<Field> getAllFields(@Nullable Class<?> type, List<Field> acc) {
        if (type == null || type == Object.class) {
            return acc;
        }
        for (Field f : type.getDeclaredFields()) {
            acc.add(f);
        }
        return getAllFields(type.getSuperclass(), acc);
    }

    /** Cached field metadata — parsed once per criteria class. */
    private record FieldMeta(Field field, Query query) {}
}
