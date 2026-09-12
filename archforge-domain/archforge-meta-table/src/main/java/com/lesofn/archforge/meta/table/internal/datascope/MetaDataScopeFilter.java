package com.lesofn.archforge.meta.table.internal.datascope;

import com.lesofn.archforge.meta.table.api.datascope.MetaDataScope;
import com.lesofn.archforge.meta.table.api.datascope.MetaDataScopeProvider;
import com.lesofn.archforge.meta.table.api.domain.MetaColumn;
import com.lesofn.archforge.meta.table.internal.ddl.SqlIdentifier;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Component;

/**
 * Translates the request's {@link MetaDataScope} into an {@code AND ...} SQL fragment for
 * meta-table row queries ({@code list}, {@code export}).
 *
 * <p>
 * Column mapping is flag-driven on {@link MetaColumn}: dept-scoped variants filter the first
 * {@code tenantColumn}-marked column; {@code ONLY_SELF} filters the first {@code ownerColumn}-
 * marked column, falling back to {@code creator_id}. A dept-scoped request against a table
 * with no tenant column degrades to {@code creator_id = current user} — never widens.
 */
@Component
@RequiredArgsConstructor
public class MetaDataScopeFilter {

    /** Appended when the scope cannot be satisfied (missing uid / empty dept set). */
    static final String DENY_ALL = " AND 1 = 0";

    private static final String UID_PARAM = "dsUid";
    private static final String DEPT_IDS_PARAM = "dsDeptIds";
    private static final String CREATOR_COLUMN = "creator_id";

    private final ObjectProvider<MetaDataScopeProvider> scopeProvider;

    /**
     * Returns the scope predicate fragment ({@code " AND ..."} or empty) and registers the
     * referenced named parameters on {@code params}.
     */
    public String buildClause(List<MetaColumn> columns, String mainAlias, MapSqlParameterSource params) {
        MetaDataScopeProvider provider = scopeProvider.getIfAvailable();
        MetaDataScope scope = provider == null ? MetaDataScope.all() : provider.current();
        return buildClause(columns, mainAlias, params, scope);
    }

    static String buildClause(List<MetaColumn> columns, String mainAlias, MapSqlParameterSource params,
            MetaDataScope scope) {
        return switch (scope.scope()) {
            case ONLY_SELF -> selfClause(columns, mainAlias, params, scope.userId());
            case SINGLE_DEPT, DEPT_TREE, CUSTOM_DEFINE -> deptClause(columns, mainAlias, params, scope);
            case ALL -> "";
        };
    }

    private static String deptClause(List<MetaColumn> columns, String mainAlias, MapSqlParameterSource params,
            MetaDataScope scope) {
        Optional<String> tenantColumn = markedColumn(columns, MetaColumn::isTenantColumn);
        if (tenantColumn.isEmpty()) {
            return selfClause(columns, mainAlias, params, scope.userId());
        }
        if (scope.deptIds().isEmpty()) {
            return DENY_ALL;
        }
        params.addValue(DEPT_IDS_PARAM, scope.deptIds());
        return " AND " + mainAlias + "." + SqlIdentifier.quote(tenantColumn.get()) + " IN (:" + DEPT_IDS_PARAM + ")";
    }

    private static String selfClause(List<MetaColumn> columns, String mainAlias, MapSqlParameterSource params,
            @Nullable Long userId) {
        if (userId == null) {
            return DENY_ALL;
        }
        String column = markedColumn(columns, MetaColumn::isOwnerColumn).orElse(CREATOR_COLUMN);
        params.addValue(UID_PARAM, userId);
        return " AND " + mainAlias + "." + SqlIdentifier.quote(column) + " = :" + UID_PARAM;
    }

    private static Optional<String> markedColumn(List<MetaColumn> columns, Predicate<MetaColumn> mark) {
        return columns.stream()
                .filter(mark)
                .min(Comparator.comparing(MetaColumn::getSort, Comparator.nullsLast(Comparator.naturalOrder())))
                .map(MetaColumn::getColumnCode);
    }
}
