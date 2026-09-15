package com.lesofn.archforge.meta.table.internal.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.lesofn.archforge.meta.table.api.dao.MetaColumnRepository;
import com.lesofn.archforge.meta.table.api.dao.MetaTableRepository;
import com.lesofn.archforge.meta.table.api.domain.MetaColumn;
import com.lesofn.archforge.meta.table.api.domain.MetaTable;
import com.lesofn.archforge.meta.table.api.dto.TableImportPreview;
import com.lesofn.archforge.meta.table.api.errors.MetaTableException;
import com.lesofn.archforge.meta.table.internal.introspect.PostgresSchemaIntrospector;
import com.lesofn.archforge.meta.table.internal.introspect.PostgresSchemaIntrospector.ColumnInfo;
import com.lesofn.archforge.meta.table.internal.introspect.PostgresSchemaIntrospector.IndexScan;
import java.util.List;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

/** Compatibility matrix for {@link MetaTableImportServiceImpl}. */
class MetaTableImportServiceImplTest {

    private PostgresSchemaIntrospector introspector;
    private MetaTableRepository metaTableRepository;
    private MetaColumnRepository metaColumnRepository;
    private MetaTableImportServiceImpl service;

    @BeforeEach
    void setUp() {
        introspector = mock(PostgresSchemaIntrospector.class);
        metaTableRepository = mock(MetaTableRepository.class);
        metaColumnRepository = mock(MetaColumnRepository.class);
        service = new MetaTableImportServiceImpl(introspector, metaTableRepository, metaColumnRepository);
    }

    // ---- fixtures ----

    private static ColumnInfo auditCol(String name, String udt, boolean nullable, boolean identity,
            @Nullable String columnDefault, int pos) {
        return new ColumnInfo(name, udt, nullable, identity, columnDefault, null, null, null, pos, null);
    }

    private static ColumnInfo col(String name, String udt, boolean nullable, @Nullable String columnDefault,
            @Nullable Integer charLen, @Nullable Integer precision, @Nullable Integer scale, int pos) {
        return new ColumnInfo(name, udt, nullable, false, columnDefault, charLen, precision, scale, pos, null);
    }

    /** 一张完全合规的表：id identity PK + 全审计列 + 一个 varchar 业务列。 */
    private static List<ColumnInfo> compatibleColumns() {
        return List.of(
                auditCol("id", "int8", false, true, null, 1),
                auditCol("creator_id", "int8", true, false, null, 2),
                auditCol("create_time", "timestamp", true, false, null, 3),
                auditCol("updater_id", "int8", true, false, null, 4),
                auditCol("update_time", "timestamp", true, false, null, 5),
                auditCol("deleted", "int4", false, false, "0", 6),
                col("name", "varchar", true, null, 64, null, null, 7),
                col("amount", "numeric", false, null, null, 18, 2, 8));
    }

    private void stubTable(String tableName, List<ColumnInfo> columns, List<String> pk) {
        when(introspector.tableExists(tableName)).thenReturn(true);
        when(introspector.listColumns(tableName)).thenReturn(columns);
        when(introspector.primaryKeyColumns(tableName)).thenReturn(pk);
        when(introspector.scanIndexes(tableName))
                .thenReturn(new IndexScan(List.of(), List.of()));
        when(introspector.listTables()).thenReturn(
                List.of(new PostgresSchemaIntrospector.TableInfo(tableName, (String) null, 0)));
        when(metaTableRepository.findAllByDeletedFalse()).thenReturn(List.of());
    }

    // ---- cases ----

    @Test
    void compatibleTablePassesAllChecks() {
        stubTable("legacy_order", compatibleColumns(), List.of("id"));

        TableImportPreview preview = service.preview("legacy_order");

        assertTrue(preview.isCompatible());
        assertTrue(preview.getReasons().isEmpty());
        assertEquals(8, preview.getColumns().size());
        TableImportPreview.PreviewColumn nameCol = preview.getColumns().get(6);
        assertFalse(nameCol.isAudit());
        assertEquals("STRING", nameCol.getDataType());
        assertEquals(64, nameCol.getLength());
        TableImportPreview.PreviewColumn amountCol = preview.getColumns().get(7);
        assertEquals("DECIMAL", amountCol.getDataType());
        assertTrue(amountCol.isRequired());
        assertTrue(preview.getColumns().get(0).isAudit());
    }

    @Test
    void missingDeletedColumnRejected() {
        List<ColumnInfo> columns = compatibleColumns().stream()
                .filter(c -> !"deleted".equals(c.name()))
                .toList();
        stubTable("no_deleted", columns, List.of("id"));

        TableImportPreview preview = service.preview("no_deleted");

        assertFalse(preview.isCompatible());
        assertTrue(preview.getReasons().stream().anyMatch(r -> r.contains("deleted")));
    }

    @Test
    void nonIdPrimaryKeyRejected() {
        List<ColumnInfo> columns = compatibleColumns().stream()
                .map(c -> "id".equals(c.name())
                        ? new ColumnInfo("order_id", c.udtName(), c.nullable(), c.identity(), c.columnDefault(), c.charLen(), c
                                .precision(), c.scale(), c.position(), c.comment())
                        : c)
                .toList();
        stubTable("pk_named_other", columns, List.of("order_id"));

        TableImportPreview preview = service.preview("pk_named_other");

        assertFalse(preview.isCompatible());
        assertTrue(preview.getReasons().stream().anyMatch(r -> r.contains("id")));
    }

    @Test
    void idWithoutDefaultRejected() {
        List<ColumnInfo> columns = compatibleColumns().stream()
                .map(c -> "id".equals(c.name())
                        ? new ColumnInfo("id", "int8", false, false, null, null, null, null, 1, null)
                        : c)
                .toList();
        stubTable("no_id_default", columns, List.of("id"));

        TableImportPreview preview = service.preview("no_id_default");

        assertFalse(preview.isCompatible());
        assertTrue(preview.getReasons().stream().anyMatch(r -> r.contains("默认值")));
    }

    @Test
    void unsupportedColumnTypeRejected() {
        List<ColumnInfo> columns = new java.util.ArrayList<>(compatibleColumns());
        columns.add(col("payload", "bytea", true, null, null, null, null, 9));
        stubTable("has_bytea", columns, List.of("id"));

        TableImportPreview preview = service.preview("has_bytea");

        assertFalse(preview.isCompatible());
        assertTrue(preview.getReasons().stream().anyMatch(r -> r.contains("bytea")));
        assertNotNull(preview.getColumns().get(8).getWarning());
    }

    @Test
    void reservedPrefixRejected() {
        stubTable("sys_thing", compatibleColumns(), List.of("id"));

        TableImportPreview preview = service.preview("sys_thing");

        assertFalse(preview.isCompatible());
        assertTrue(preview.getReasons().stream().anyMatch(r -> r.contains("平台保留")));
    }

    @Test
    void platformPrefixedTableRejected() {
        // meta_ 是 meta-table 自家前缀，validateTableCode 放行 —— 黑名单是唯一防线
        stubTable("meta_trap", compatibleColumns(), List.of("id"));

        TableImportPreview preview = service.preview("meta_trap");

        assertFalse(preview.isCompatible());
        assertTrue(preview.getReasons().stream().anyMatch(r -> r.contains("平台保留")));
    }

    @Test
    void importTableGuardRejectsPlatformTable() {
        // 结构完全合规但命中黑名单 —— importTable 的独立守卫必须 fail-fast
        stubTable("flyway_x", compatibleColumns(), List.of("id"));

        assertThrows(MetaTableException.class,
                () -> service.importTable("flyway_x", null, null, 1L));
    }

    @Test
    void alreadyRegisteredRejected() {
        stubTable("meta_dup", compatibleColumns(), List.of("id"));
        MetaTable existing = new MetaTable();
        existing.setTableCode("dup");
        existing.setTablePrefix("meta_");
        when(metaTableRepository.findAllByDeletedFalse()).thenReturn(List.of(existing));

        TableImportPreview preview = service.preview("meta_dup");

        assertFalse(preview.isCompatible());
        assertTrue(preview.getReasons().stream().anyMatch(r -> r.contains("已注册")));
    }

    @Test
    void missingTableThrowsNotExists() {
        when(introspector.tableExists("ghost")).thenReturn(false);

        assertThrows(MetaTableException.class, () -> service.preview("ghost"));
    }

    @Test
    void importRejectsIncompatibleTable() {
        List<ColumnInfo> columns = compatibleColumns().stream()
                .filter(c -> !"deleted".equals(c.name()))
                .toList();
        stubTable("bad_table", columns, List.of("id"));

        assertThrows(MetaTableException.class,
                () -> service.importTable("bad_table", null, null, 1L));
    }

    @Test
    void importSavesTableWithEmptyPrefixAndSkipsAuditColumns() {
        stubTable("legacy_order", compatibleColumns(), List.of("id"));
        MetaTable saved = new MetaTable();
        saved.setId(42L);
        when(metaTableRepository.save(org.mockito.ArgumentMatchers.any(MetaTable.class))).thenReturn(saved);

        Long id = service.importTable("legacy_order", "历史订单", null, 9L);

        assertEquals(42L, id);
        ArgumentCaptor<MetaTable> tableCaptor = ArgumentCaptor.forClass(MetaTable.class);
        verify(metaTableRepository).save(tableCaptor.capture());
        assertEquals("legacy_order", tableCaptor.getValue().getTableCode());
        assertEquals("", tableCaptor.getValue().getTablePrefix());
        assertEquals("legacy_order", tableCaptor.getValue().physicalTableName());
        assertEquals("历史订单", tableCaptor.getValue().getTableName());

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<MetaColumn>> colCaptor = ArgumentCaptor.forClass(List.class);
        verify(metaColumnRepository).saveAll(colCaptor.capture());
        List<MetaColumn> savedCols = colCaptor.getValue();
        assertEquals(2, savedCols.size());
        assertEquals("name", savedCols.get(0).getColumnCode());
        assertEquals("amount", savedCols.get(1).getColumnCode());
        assertTrue(savedCols.get(1).getRequired());
        assertNull(savedCols.get(0).getTenantColumn());
    }

    @Test
    void singleColumnUniqueIndexMapsToUniqueFlag() {
        stubTable("uniq_table", compatibleColumns(), List.of("id"));
        when(introspector.scanIndexes("uniq_table"))
                .thenReturn(new IndexScan(List.of(
                        new PostgresSchemaIntrospector.IndexInfo(true, "name")), List.of("idx_combo")));

        TableImportPreview preview = service.preview("uniq_table");

        TableImportPreview.PreviewColumn nameCol = preview.getColumns().get(6);
        assertTrue(nameCol.isUnique());
        assertTrue(nameCol.isIndexed());
        assertEquals(List.of("idx_combo"), preview.getCompositeIndexes());
    }

    @Test
    void tableWithoutBusinessColumnsRejected() {
        List<ColumnInfo> auditOnly = compatibleColumns().subList(0, 6);
        stubTable("audit_only", auditOnly, List.of("id"));

        TableImportPreview preview = service.preview("audit_only");

        assertFalse(preview.isCompatible());
        assertTrue(preview.getReasons().stream().anyMatch(r -> r.contains("业务列")));
    }
}
