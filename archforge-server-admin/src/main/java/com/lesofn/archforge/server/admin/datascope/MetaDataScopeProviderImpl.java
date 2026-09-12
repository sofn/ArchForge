package com.lesofn.archforge.server.admin.datascope;

import com.lesofn.archforge.common.auth.DataScopeEnum;
import com.lesofn.archforge.infrastructure.security.datascope.DataScopeContext;
import com.lesofn.archforge.infrastructure.security.datascope.DataScopeContextHolder;
import com.lesofn.archforge.meta.table.api.datascope.MetaDataScope;
import com.lesofn.archforge.meta.table.api.datascope.MetaDataScopeProvider;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * {@link MetaDataScopeProvider} 的 admin 实现：读取 {@code @DataPermission} 切面写入的
 * {@link DataScopeContext}，把部门类范围解析为已展开的部门 id 集合。
 */
@Component
@RequiredArgsConstructor
public class MetaDataScopeProviderImpl implements MetaDataScopeProvider {

    private final DeptTreeResolver deptTreeResolver;

    @Override
    public MetaDataScope current() {
        DataScopeContext context = DataScopeContextHolder.get();
        if (context == null || context.getDataScope() == null) {
            return MetaDataScope.all();
        }
        Long userId = context.getUserId();
        return switch (context.getDataScope()) {
            case ALL -> MetaDataScope.all();
            case ONLY_SELF -> new MetaDataScope(DataScopeEnum.ONLY_SELF, userId, Set.of());
            case SINGLE_DEPT -> new MetaDataScope(DataScopeEnum.SINGLE_DEPT, userId, context.getDeptId() != null ? Set.of(
                    context.getDeptId()) : Set.of());
            case DEPT_TREE -> new MetaDataScope(DataScopeEnum.DEPT_TREE, userId, deptTreeResolver.resolveTree(context
                    .getDeptId()));
            case CUSTOM_DEFINE -> new MetaDataScope(DataScopeEnum.CUSTOM_DEFINE, userId, context.getCustomDeptIds());
        };
    }
}
