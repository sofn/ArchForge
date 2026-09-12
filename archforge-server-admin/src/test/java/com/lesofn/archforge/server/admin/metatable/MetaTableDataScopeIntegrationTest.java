package com.lesofn.archforge.server.admin.metatable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.lesofn.archforge.common.auth.DataScopeEnum;
import com.lesofn.archforge.common.persistence.testsupport.AbstractIntegrationTest;
import com.lesofn.archforge.infrastructure.frame.context.RequestContext;
import com.lesofn.archforge.infrastructure.frame.context.ScopedValueContext;
import com.lesofn.archforge.infrastructure.security.datascope.DataScopeContext;
import com.lesofn.archforge.infrastructure.security.datascope.DataScopeContextHolder;
import com.lesofn.archforge.meta.table.api.datascope.MetaDataScope;
import com.lesofn.archforge.meta.table.api.datascope.MetaDataScopeProvider;
import com.lesofn.archforge.meta.table.api.domain.MetaColumn;
import com.lesofn.archforge.meta.table.api.domain.MetaColumnType;
import com.lesofn.archforge.meta.table.api.domain.MetaTable;
import com.lesofn.archforge.meta.table.api.dto.MetaDataQuery;
import com.lesofn.archforge.meta.table.api.dto.MetaPageResponse;
import com.lesofn.archforge.meta.table.api.enums.MetaDataFormat;
import com.lesofn.archforge.meta.table.api.service.MetaTableAdminService;
import com.lesofn.archforge.meta.table.api.service.MetaTableCrudService;
import com.lesofn.archforge.server.admin.Application;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * End-to-end data-scope tests for meta-table row queries: real PostgreSQL, real
 * {@link MetaDataScopeProviderImpl} wiring, scope injected through
 * {@link DataScopeContextHolder} inside a {@link ScopedValueContext} scope — mirroring what
 * {@code @DataPermission} does on the controller endpoints.
 */
@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Tag("slow")
class MetaTableDataScopeIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MetaTableAdminService adminService;

    @Autowired
    private MetaTableCrudService crudService;

    @Autowired
    private MetaDataScopeProvider scopeProvider;

    @Test
    void providerIsWired() {
        assertEquals(MetaDataScope.all(), scopeProvider.current());
    }

    @Test
    void allScopeReturnsEveryRow() throws Exception {
        Long tableId = deptScopedTable("dsitall");
        insertRows(tableId);

        List<Map<String, Object>> rows = listWithScope(tableId, scope(DataScopeEnum.ALL, 1L, null, null));

        assertEquals(3, rows.size());
    }

    @Test
    void singleDeptFiltersByTenantColumn() throws Exception {
        Long tableId = deptScopedTable("dsitsdept");
        insertRows(tableId);

        List<Map<String, Object>> rows = listWithScope(tableId,
                scope(DataScopeEnum.SINGLE_DEPT, 1L, 10L, null));

        assertEquals(2, rows.size());
        assertTrue(rows.stream()
                .allMatch(r -> ((Number) java.util.Objects.requireNonNull(r.get("dept_id"))).longValue() == 10L));
    }

    @Test
    void customDefineFiltersByDeptIdSet() throws Exception {
        Long tableId = deptScopedTable("dsitcdef");
        insertRows(tableId);

        List<Map<String, Object>> rows = listWithScope(tableId,
                scope(DataScopeEnum.CUSTOM_DEFINE, 1L, null, Set.of(10L, 20L)));

        assertEquals(3, rows.size());
    }

    @Test
    void customDefinePartialSetExcludesOtherDepts() throws Exception {
        Long tableId = deptScopedTable("dsitcdefp");
        insertRows(tableId);

        List<Map<String, Object>> rows = listWithScope(tableId,
                scope(DataScopeEnum.CUSTOM_DEFINE, 1L, null, Set.of(20L)));

        assertEquals(1, rows.size());
        assertEquals(20L,
                ((Number) java.util.Objects.requireNonNull(rows.get(0).get("dept_id"))).longValue());
    }

    @Test
    void deptScopeOnUnmarkedTableDegradesToCreator() throws Exception {
        Long tableId = plainTable("dsitplain");
        crudService.insert(tableId, Map.of("name", "mine"), 7L);
        crudService.insert(tableId, Map.of("name", "theirs"), 8L);

        List<Map<String, Object>> rows = listWithScope(tableId,
                scope(DataScopeEnum.SINGLE_DEPT, 7L, 10L, null));

        assertEquals(1, rows.size());
        assertEquals("mine", rows.get(0).get("name"));
    }

    @Test
    void onlySelfFallsBackToCreatorId() throws Exception {
        Long tableId = plainTable("dsitself");
        crudService.insert(tableId, Map.of("name", "mine"), 7L);
        crudService.insert(tableId, Map.of("name", "theirs"), 8L);

        List<Map<String, Object>> rows = listWithScope(tableId,
                scope(DataScopeEnum.ONLY_SELF, 7L, null, null));

        assertEquals(1, rows.size());
        assertEquals("mine", rows.get(0).get("name"));
    }

    @Test
    void onlySelfUsesOwnerColumnWhenMarked() throws Exception {
        Long tableId = ownerScopedTable("dsitown");
        crudService.insert(tableId, Map.of("salesman_id", 7, "name", "assigned"), 1L);
        crudService.insert(tableId, Map.of("salesman_id", 8, "name", "other"), 7L);

        List<Map<String, Object>> rows = listWithScope(tableId,
                scope(DataScopeEnum.ONLY_SELF, 7L, null, null));

        assertEquals(1, rows.size());
        assertEquals("assigned", rows.get(0).get("name"));
    }

    @Test
    void exportRespectsScope() throws Exception {
        Long tableId = deptScopedTable("dsitexp");
        insertRows(tableId);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ScopedValueContext.runInScope(new RequestContext("it-export"), () -> {
            DataScopeContextHolder.set(scope(DataScopeEnum.SINGLE_DEPT, 1L, 10L, null));
            crudService.export(tableId, MetaDataFormat.CSV, out);
        });

        String csv = out.toString(StandardCharsets.UTF_8);
        long dataLines = csv.lines().filter(l -> l.contains("r10a") || l.contains("r10b") || l.contains("r20"))
                .count();
        assertEquals(2, dataLines, csv);
    }

    // ---- fixtures ----

    private Long deptScopedTable(String code) {
        return createTable(code, tenantColumn("dept_id"), stringColumn("name"));
    }

    private Long ownerScopedTable(String code) {
        return createTable(code, ownerColumn("salesman_id"), stringColumn("name"));
    }

    private Long plainTable(String code) {
        return createTable(code, stringColumn("name"));
    }

    private Long createTable(String code, MetaColumn... columns) {
        MetaTable table = new MetaTable();
        table.setTableCode(code);
        table.setTableName(code);
        table.setTablePrefix("meta_");
        return adminService.create(table, new ArrayList<>(List.of(columns)));
    }

    private void insertRows(Long tableId) {
        crudService.insert(tableId, Map.of("dept_id", 10, "name", "r10a"), 1L);
        crudService.insert(tableId, Map.of("dept_id", 10, "name", "r10b"), 2L);
        crudService.insert(tableId, Map.of("dept_id", 20, "name", "r20"), 1L);
    }

    private static MetaColumn tenantColumn(String code) {
        MetaColumn column = baseColumn(code, MetaColumnType.INTEGER);
        column.setTenantColumn(true);
        return column;
    }

    private static MetaColumn ownerColumn(String code) {
        MetaColumn column = baseColumn(code, MetaColumnType.INTEGER);
        column.setOwnerColumn(true);
        return column;
    }

    private static MetaColumn stringColumn(String code) {
        MetaColumn column = baseColumn(code, MetaColumnType.STRING);
        column.setLength(50);
        return column;
    }

    private static MetaColumn baseColumn(String code, MetaColumnType type) {
        MetaColumn column = new MetaColumn();
        column.setColumnCode(code);
        column.setColumnName(code);
        column.setDataType(type);
        return column;
    }

    private static DataScopeContext scope(DataScopeEnum scope, Long userId, @Nullable Long deptId,
            @Nullable Set<Long> customDeptIds) {
        return DataScopeContext.builder()
                .dataScope(scope)
                .userId(userId)
                .deptId(deptId)
                .customDeptIds(customDeptIds == null ? Set.of() : customDeptIds)
                .build();
    }

    private List<Map<String, Object>> listWithScope(Long tableId, DataScopeContext ctx) throws Exception {
        List<List<Map<String, Object>>> holder = new ArrayList<>();
        ScopedValueContext.runInScope(new RequestContext("it-scope"), () -> {
            DataScopeContextHolder.set(ctx);
            MetaPageResponse<Map<String, Object>> page = crudService.list(tableId,
                    MetaDataQuery.of(Map.of(), 1, 50));
            holder.add(page.getList());
        });
        return holder.get(0);
    }
}
