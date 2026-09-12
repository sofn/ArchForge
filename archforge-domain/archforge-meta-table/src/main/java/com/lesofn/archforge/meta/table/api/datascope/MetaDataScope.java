package com.lesofn.archforge.meta.table.api.datascope;

import com.lesofn.archforge.common.auth.DataScopeEnum;
import java.util.Set;
import org.jspecify.annotations.Nullable;

/**
 * Effective data scope for meta-table row queries, resolved by the hosting application.
 *
 * <p>
 * {@code deptIds} is the fully expanded department id set for the dept-scoped variants
 * ({@code SINGLE_DEPT}, {@code DEPT_TREE}, {@code CUSTOM_DEFINE}) — tree expansion happens
 * in the provider implementation, this record only carries the result.
 */
public record MetaDataScope(DataScopeEnum scope, @Nullable Long userId, Set<Long> deptIds) {

    public MetaDataScope {
        java.util.Objects.requireNonNull(scope, "scope");
        deptIds = deptIds == null ? Set.of() : Set.copyOf(deptIds);
    }

    /** No data-scope restriction. */
    public static MetaDataScope all() {
        return new MetaDataScope(DataScopeEnum.ALL, null, Set.of());
    }
}
