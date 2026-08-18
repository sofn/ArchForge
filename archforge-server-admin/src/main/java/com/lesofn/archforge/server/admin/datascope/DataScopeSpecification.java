package com.lesofn.archforge.server.admin.datascope;

import com.lesofn.archforge.infrastructure.security.datascope.DataScopeContext;
import com.lesofn.archforge.infrastructure.user.web.DataScopeEnum;
import com.lesofn.archforge.user.api.domain.SysDept;
import com.lesofn.archforge.user.api.domain.SysUser;
import com.lesofn.archforge.user.api.service.SysDeptService;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

/**
 * 数据权限 JPA Specification 构造器。
 *
 * <p>
 * 根据 {@link DataScopeContext} 将部门 / 本人过滤条件追加到已有的 {@link Specification} 上。
 *
 * @author sofn
 */
@Component
@RequiredArgsConstructor
public class DataScopeSpecification {

    private final SysDeptService deptService;

    /**
     * 将数据权限条件追加到基础查询条件中。
     *
     * @param base 基础 Specification，可为 null
     * @param context 数据权限上下文
     * @return 组合后的 Specification
     */
    public Specification<SysUser> apply(Specification<SysUser> base, @Nullable DataScopeContext context) {
        if (context == null || context.getDataScope() == null || context.getDataScope() == DataScopeEnum.ALL) {
            return base;
        }
        return (root, query, cb) -> {
            Predicate basePredicate = base == null ? cb.conjunction() : base.toPredicate(root, query, cb);
            Predicate scopePredicate = buildScopePredicate(root, cb, context);
            return cb.and(basePredicate, scopePredicate);
        };
    }

    private Predicate buildScopePredicate(Root<SysUser> root, CriteriaBuilder cb, DataScopeContext context) {
        String deptAlias = context.getDeptAlias() == null ? "deptId" : context.getDeptAlias();
        String userAlias = context.getUserAlias() == null ? "id" : context.getUserAlias();
        Path<Long> deptPath = root.get(deptAlias);
        Path<Long> userPath = root.get(userAlias);

        DataScopeEnum scope = context.getDataScope();
        return switch (scope) {
            case SINGLE_DEPT -> {
                if (context.getDeptId() == null) {
                    yield denyAll(cb);
                }
                yield cb.equal(deptPath, context.getDeptId());
            }
            case DEPT_TREE -> {
                if (context.getDeptId() == null) {
                    yield denyAll(cb);
                }
                Set<Long> deptIds = resolveDeptTree(context.getDeptId());
                yield deptPath.in(deptIds);
            }
            case CUSTOM_DEFINE -> {
                Set<Long> deptIds = context.getCustomDeptIds();
                if (deptIds == null || deptIds.isEmpty()) {
                    yield denyAll(cb);
                }
                yield deptPath.in(deptIds);
            }
            case ONLY_SELF -> {
                if (context.getUserId() == null) {
                    yield denyAll(cb);
                }
                yield cb.equal(userPath, context.getUserId());
            }
            case ALL -> cb.conjunction();
            default -> denyAll(cb);
        };
    }

    private Predicate denyAll(CriteriaBuilder cb) {
        return cb.equal(cb.literal(1), 0);
    }

    private Set<Long> resolveDeptTree(Long rootDeptId) {
        List<SysDept> allDepts = deptService.findAllActiveDepts();
        Map<Long, List<Long>> children = allDepts.stream()
                .filter(d -> d.getParentId() != null)
                .collect(Collectors.groupingBy(
                        SysDept::getParentId,
                        Collectors.mapping(SysDept::getDeptId, Collectors.toList())));

        Set<Long> result = new HashSet<>();
        Queue<Long> queue = new ArrayDeque<>();
        queue.add(rootDeptId);
        while (!queue.isEmpty()) {
            Long current = queue.poll();
            if (result.contains(current)) {
                continue;
            }
            result.add(current);
            List<Long> childIds = children.getOrDefault(current, new ArrayList<>());
            for (Long childId : childIds) {
                if (!result.contains(childId)) {
                    queue.add(childId);
                }
            }
        }
        return result;
    }
}
