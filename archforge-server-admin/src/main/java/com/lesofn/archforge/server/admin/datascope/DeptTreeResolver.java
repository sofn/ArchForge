package com.lesofn.archforge.server.admin.datascope;

import com.lesofn.archforge.user.api.domain.SysDept;
import com.lesofn.archforge.user.api.service.SysDeptService;
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
import org.springframework.stereotype.Component;

/**
 * 部门树展开器：给定根部门，BFS 收集其与全部子孙部门的 id 集合。
 * {@link DataScopeSpecification} 与 {@link MetaDataScopeProviderImpl} 共用。
 */
@Component
@RequiredArgsConstructor
public class DeptTreeResolver {

    private final SysDeptService deptService;

    /**
     * 展开部门子树。
     *
     * @param rootDeptId 根部门 id，null 时返回空集
     * @return 根部门 + 全部子孙部门 id
     */
    public Set<Long> resolveTree(@Nullable Long rootDeptId) {
        if (rootDeptId == null) {
            return Set.of();
        }
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
