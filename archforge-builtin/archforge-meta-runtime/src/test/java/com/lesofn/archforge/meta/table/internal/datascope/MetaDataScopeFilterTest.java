package com.lesofn.archforge.meta.table.internal.datascope;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.lesofn.archforge.common.auth.DataScopeEnum;
import com.lesofn.archforge.meta.table.api.datascope.MetaDataScope;
import com.lesofn.archforge.meta.table.api.datascope.MetaDataScopeProvider;
import com.lesofn.archforge.meta.table.api.domain.MetaColumn;
import com.lesofn.archforge.meta.table.api.errors.MetaTableException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

/**
 * Clause matrix for {@link MetaDataScopeFilter#buildClause}: scope kind x marked columns.
 */
class MetaDataScopeFilterTest {

    private final MapSqlParameterSource params = new MapSqlParameterSource();

    @Test
    void allScopeAppendsNothing() {
        String clause = MetaDataScopeFilter.buildClause(List.of(), "main", params, MetaDataScope.all());
        assertEquals("", clause);
    }

    @Test
    void onlySelfUsesOwnerColumnWhenMarked() {
        List<MetaColumn> columns = List.of(
                column("name", 1),
                column("salesman_id", 2, false, true));

        String clause = MetaDataScopeFilter.buildClause(columns, "main", params,
                new MetaDataScope(DataScopeEnum.ONLY_SELF, 42L, Set.of()));

        assertEquals(" AND main.\"salesman_id\" = :dsUid", clause);
        assertEquals(42L, params.getValue("dsUid"));
    }

    @Test
    void onlySelfFallsBackToCreatorId() {
        List<MetaColumn> columns = List.of(column("name", 1));

        String clause = MetaDataScopeFilter.buildClause(columns, "main", params,
                new MetaDataScope(DataScopeEnum.ONLY_SELF, 7L, Set.of()));

        assertEquals(" AND main.\"creator_id\" = :dsUid", clause);
        assertEquals(7L, params.getValue("dsUid"));
    }

    @Test
    void onlySelfWithNullUserIdDeniesAll() {
        String clause = MetaDataScopeFilter.buildClause(List.of(), "main", params,
                new MetaDataScope(DataScopeEnum.ONLY_SELF, null, Set.of()));

        assertEquals(MetaDataScopeFilter.DENY_ALL, clause);
    }

    @Test
    void singleDeptFiltersTenantColumn() {
        List<MetaColumn> columns = List.of(
                column("name", 1),
                column("dept_id", 2, true, false));

        String clause = MetaDataScopeFilter.buildClause(columns, "main", params,
                new MetaDataScope(DataScopeEnum.SINGLE_DEPT, 1L, Set.of(10L)));

        assertEquals(" AND main.\"dept_id\" IN (:dsDeptIds)", clause);
        assertEquals(Set.of(10L), params.getValue("dsDeptIds"));
    }

    @Test
    void deptTreeFiltersTenantColumn() {
        List<MetaColumn> columns = List.of(column("dept_id", 1, true, false));

        String clause = MetaDataScopeFilter.buildClause(columns, "main", params,
                new MetaDataScope(DataScopeEnum.DEPT_TREE, 1L, Set.of(10L, 11L, 12L)));

        assertEquals(" AND main.\"dept_id\" IN (:dsDeptIds)", clause);
        assertEquals(Set.of(10L, 11L, 12L), params.getValue("dsDeptIds"));
    }

    @Test
    void customDefineFiltersTenantColumn() {
        List<MetaColumn> columns = List.of(column("dept_id", 1, true, false));

        String clause = MetaDataScopeFilter.buildClause(columns, "main", params,
                new MetaDataScope(DataScopeEnum.CUSTOM_DEFINE, 1L, Set.of(5L, 6L)));

        assertEquals(" AND main.\"dept_id\" IN (:dsDeptIds)", clause);
    }

    @Test
    void deptScopeWithoutTenantColumnDegradesToCreator() {
        List<MetaColumn> columns = List.of(column("name", 1));

        String clause = MetaDataScopeFilter.buildClause(columns, "main", params,
                new MetaDataScope(DataScopeEnum.DEPT_TREE, 9L, Set.of(10L)));

        assertEquals(" AND main.\"creator_id\" = :dsUid", clause);
        assertEquals(9L, params.getValue("dsUid"));
    }

    @Test
    void deptScopeWithEmptyDeptIdsDeniesAll() {
        List<MetaColumn> columns = List.of(column("dept_id", 1, true, false));

        String clause = MetaDataScopeFilter.buildClause(columns, "main", params,
                new MetaDataScope(DataScopeEnum.CUSTOM_DEFINE, 1L, Set.of()));

        assertEquals(MetaDataScopeFilter.DENY_ALL, clause);
    }

    @Test
    void multipleTenantColumnsPickLowestSort() {
        List<MetaColumn> columns = List.of(
                column("dept_b", 5, true, false),
                column("dept_a", 2, true, false));

        String clause = MetaDataScopeFilter.buildClause(columns, "main", params,
                new MetaDataScope(DataScopeEnum.SINGLE_DEPT, 1L, Set.of(10L)));

        assertEquals(" AND main.\"dept_a\" IN (:dsDeptIds)", clause);
    }

    @Test
    void absentProviderBehavesAsAll() {
        @SuppressWarnings("unchecked")
        ObjectProvider<MetaDataScopeProvider> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(null);

        MetaDataScopeFilter filter = new MetaDataScopeFilter(provider);
        String clause = filter.buildClause(List.of(column("dept_id", 1, true, false)), "main", params);

        assertEquals("", clause);
    }

    @Test
    void providerResultDrivesClause() {
        MetaDataScopeProvider impl = () -> new MetaDataScope(DataScopeEnum.ONLY_SELF, 3L, Set.of());
        @SuppressWarnings("unchecked")
        ObjectProvider<MetaDataScopeProvider> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(impl);

        MetaDataScopeFilter filter = new MetaDataScopeFilter(provider);
        String clause = filter.buildClause(List.of(), "main", params);

        assertEquals(" AND main.\"creator_id\" = :dsUid", clause);
    }

    // ---- checkRowInScope (insert/import: strict, absent marked value is out of scope) ----

    @Test
    void insertRejectsForeignOwnerValue() {
        List<MetaColumn> columns = List.of(column("salesman_id", 1, false, true));
        MetaDataScope scope = new MetaDataScope(DataScopeEnum.ONLY_SELF, 7L, Set.of());

        assertThrows(MetaTableException.class,
                () -> MetaDataScopeFilter.checkRowInScope(columns, Map.of("salesman_id", 8), scope));
    }

    @Test
    void insertAcceptsOwnOwnerValue() {
        List<MetaColumn> columns = List.of(column("salesman_id", 1, false, true));
        MetaDataScope scope = new MetaDataScope(DataScopeEnum.ONLY_SELF, 7L, Set.of());

        assertDoesNotThrow(
                () -> MetaDataScopeFilter.checkRowInScope(columns, Map.of("salesman_id", 7), scope));
    }

    @Test
    void insertAcceptsNumericStringOwnerValue() {
        List<MetaColumn> columns = List.of(column("salesman_id", 1, false, true));
        MetaDataScope scope = new MetaDataScope(DataScopeEnum.ONLY_SELF, 7L, Set.of());

        assertDoesNotThrow(
                () -> MetaDataScopeFilter.checkRowInScope(columns, Map.of("salesman_id", "7"), scope));
    }

    @Test
    void insertRejectsMissingOwnerValue() {
        List<MetaColumn> columns = List.of(column("salesman_id", 1, false, true), column("name", 2));
        MetaDataScope scope = new MetaDataScope(DataScopeEnum.ONLY_SELF, 7L, Set.of());

        assertThrows(MetaTableException.class,
                () -> MetaDataScopeFilter.checkRowInScope(columns, Map.of("name", "x"), scope));
    }

    @Test
    void insertWithoutOwnerMarkPasses() {
        List<MetaColumn> columns = List.of(column("name", 1));
        MetaDataScope scope = new MetaDataScope(DataScopeEnum.ONLY_SELF, 7L, Set.of());

        assertDoesNotThrow(() -> MetaDataScopeFilter.checkRowInScope(columns, Map.of("name", "x"), scope));
    }

    @Test
    void insertRejectsForeignTenantValue() {
        List<MetaColumn> columns = List.of(column("dept_id", 1, true, false));
        MetaDataScope scope = new MetaDataScope(DataScopeEnum.SINGLE_DEPT, 1L, Set.of(10L));

        assertThrows(MetaTableException.class,
                () -> MetaDataScopeFilter.checkRowInScope(columns, Map.of("dept_id", 20), scope));
    }

    @Test
    void insertAcceptsTenantValueInsideCustomSet() {
        List<MetaColumn> columns = List.of(column("dept_id", 1, true, false));
        MetaDataScope scope = new MetaDataScope(DataScopeEnum.CUSTOM_DEFINE, 1L, Set.of(10L, 20L));

        assertDoesNotThrow(
                () -> MetaDataScopeFilter.checkRowInScope(columns, Map.of("dept_id", 20), scope));
    }

    @Test
    void insertWithoutTenantMarkPassesForDeptScope() {
        List<MetaColumn> columns = List.of(column("name", 1));
        MetaDataScope scope = new MetaDataScope(DataScopeEnum.DEPT_TREE, 1L, Set.of(10L));

        assertDoesNotThrow(() -> MetaDataScopeFilter.checkRowInScope(columns, Map.of("name", "x"), scope));
    }

    @Test
    void allScopeAcceptsAnyRow() {
        List<MetaColumn> columns = List.of(column("dept_id", 1, true, false));

        assertDoesNotThrow(
                () -> MetaDataScopeFilter.checkRowInScope(columns, Map.of("dept_id", 99), MetaDataScope.all()));
    }

    // ---- checkUpdatedRow (update: only provided marked values are checked) ----

    @Test
    void updateSkipsUntouchedTenantColumn() {
        List<MetaColumn> columns = List.of(column("dept_id", 1, true, false), column("name", 2));
        MetaDataScope scope = new MetaDataScope(DataScopeEnum.SINGLE_DEPT, 1L, Set.of(10L));

        assertDoesNotThrow(
                () -> MetaDataScopeFilter.checkUpdatedRow(columns, Map.of("name", "x"), scope));
    }

    @Test
    void updateRejectsTenantValueMovedOutOfScope() {
        List<MetaColumn> columns = List.of(column("dept_id", 1, true, false));
        MetaDataScope scope = new MetaDataScope(DataScopeEnum.DEPT_TREE, 1L, Set.of(10L, 11L));

        assertThrows(MetaTableException.class,
                () -> MetaDataScopeFilter.checkUpdatedRow(columns, Map.of("dept_id", 99), scope));
    }

    @Test
    void updateAcceptsTenantValueInsideScope() {
        List<MetaColumn> columns = List.of(column("dept_id", 1, true, false));
        MetaDataScope scope = new MetaDataScope(DataScopeEnum.DEPT_TREE, 1L, Set.of(10L, 11L));

        assertDoesNotThrow(
                () -> MetaDataScopeFilter.checkUpdatedRow(columns, Map.of("dept_id", 11), scope));
    }

    private static MetaColumn column(String code, int sort) {
        return column(code, sort, false, false);
    }

    private static MetaColumn column(String code, int sort, boolean tenant, boolean owner) {
        MetaColumn column = new MetaColumn();
        column.setColumnCode(code);
        column.setSort(sort);
        column.setTenantColumn(tenant);
        column.setOwnerColumn(owner);
        return column;
    }
}
