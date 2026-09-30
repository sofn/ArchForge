package com.lesofn.archforge.server.admin.metatable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.lesofn.archforge.common.persistence.testsupport.AbstractIntegrationTest;
import com.lesofn.archforge.meta.table.api.dao.MetaColumnRepository;
import com.lesofn.archforge.meta.table.api.dao.MetaTableRepository;
import com.lesofn.archforge.meta.table.api.definition.FsDefinitionSource;
import com.lesofn.archforge.meta.table.api.definition.MetaTableDefinitionCodec;
import com.lesofn.archforge.meta.table.api.definition.TableDefinition;
import com.lesofn.archforge.meta.table.api.domain.MetaColumn;
import com.lesofn.archforge.meta.table.api.domain.MetaColumnType;
import com.lesofn.archforge.meta.table.api.domain.MetaTable;
import com.lesofn.archforge.meta.table.api.service.MetaTableDefinitionService;
import com.lesofn.archforge.meta.table.api.service.MetaTableDefinitionService.SyncReport;
import com.lesofn.archforge.server.admin.Application;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.Statement;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.NestedExceptionUtils;

/**
 * DB ↔ YAML definition sync against real PostgreSQL: export fidelity, dry-run
 * diff, apply upsert, orphan-column safety and explicit removedColumns delete.
 */
@SpringBootTest(classes = {
        Application.class
}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Tag("slow")
class MetaTableDefinitionSyncIntegrationTest extends AbstractIntegrationTest {

    private static final Long SEED_CREATOR = 5L;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private MetaTableDefinitionService definitionService;

    @Autowired
    private MetaTableRepository tableRepository;

    @Autowired
    private MetaColumnRepository columnRepository;

    @TempDir
    Path dir;

    @BeforeEach
    void clean() {
        tableRepository.findAllByDeletedFalse().stream()
                .filter(t -> t.getTableCode().startsWith("defsync"))
                .forEach(t -> {
                    t.setDeleted(true);
                    tableRepository.save(t);
                });
    }

    private MetaTable seedTable(String code) {
        MetaTable table = new MetaTable();
        table.setTableCode(code);
        table.setTableName("同步测试");
        table.setDescription("def sync it");
        table.setTablePrefix("meta_");
        table.setStatus(1);
        table.setSchemaVersion(1);
        table.setCreatorId(SEED_CREATOR);
        table = tableRepository.save(table);

        MetaColumn title = new MetaColumn();
        title.setTableId(java.util.Objects.requireNonNull(table.getId()));
        title.setColumnCode("title");
        title.setColumnName("标题");
        title.setDataType(MetaColumnType.STRING);
        title.setLength(200);
        title.setRequired(true);
        title.setListVisible(true);
        title.setSort(1);
        title.setCreatorId(SEED_CREATOR);
        columnRepository.save(title);

        MetaColumn status = new MetaColumn();
        status.setTableId(java.util.Objects.requireNonNull(table.getId()));
        status.setColumnCode("status");
        status.setColumnName("状态");
        status.setDataType(MetaColumnType.INTEGER);
        status.setSort(2);
        status.setCreatorId(SEED_CREATOR);
        columnRepository.save(status);
        return table;
    }

    @Test
    void exportThenImportDryRunReportsNoDiff() throws Exception {
        seedTable("defsync_rt");

        List<Path> written = definitionService.exportTo(dir, "defsync_rt");
        assertEquals(1, written.size());
        Path file = written.get(0);
        assertTrue(Files.exists(file));

        TableDefinition parsed = MetaTableDefinitionCodec.fromYaml(Files.readString(file));
        assertEquals("defsync_rt", parsed.getTableCode());
        assertEquals(2, parsed.getColumns().size());
        assertEquals("title", parsed.getColumns().get(0).getColumnCode()); // sort order preserved

        // Round-trip: importing the just-exported file must produce no changes.
        SyncReport report = definitionService.syncFrom(dir, "defsync_rt", false);
        assertTrue(report.isEmpty(), "expected clean report, got: " + report.lines());
    }

    @Test
    void importApplyUpsertsChangedFieldsAndKeepsOrphans() throws Exception {
        MetaTable table = seedTable("defsync_up");
        definitionService.exportTo(dir, "defsync_up");

        // Mutate the file: rename a column display name + drop 'status' from file
        // (without removedColumns → must be reported orphan, not deleted).
        Path file = dir.resolve("defsync_up.yaml");
        TableDefinition def = MetaTableDefinitionCodec.fromYaml(Files.readString(file));
        def.getColumns().get(0).setColumnName("大标题");
        def.setColumns(def.getColumns().stream()
                .filter(c -> !c.getColumnCode().equals("status")).toList());
        Files.writeString(file, MetaTableDefinitionCodec.toYaml(def));

        SyncReport dryRun = definitionService.syncFrom(dir, "defsync_up", false);
        assertTrue(dryRun.changedColumns().contains("defsync_up.title"));
        assertTrue(dryRun.orphanColumns().contains("defsync_up.status"));
        assertTrue(dryRun.removedColumns().isEmpty());

        // Dry-run wrote nothing:
        MetaColumn titleCol = columnRepository
                .findByTableIdAndDeletedFalseOrderBySortAsc(java.util.Objects.requireNonNull(table.getId())).stream()
                .filter(c -> c.getColumnCode().equals("title")).findFirst().orElseThrow();
        assertEquals("标题", titleCol.getColumnName());

        SyncReport applied = definitionService.syncFrom(dir, "defsync_up", true);
        assertFalse(applied.isEmpty());

        List<MetaColumn> cols = columnRepository.findByTableIdAndDeletedFalseOrderBySortAsc(java.util.Objects.requireNonNull(
                table.getId()));
        assertEquals(2, cols.size()); // orphan kept, not deleted
        assertEquals("大标题", cols.get(0).getColumnName());
        assertTrue(cols.stream().anyMatch(c -> c.getColumnCode().equals("status")));
    }

    @Test
    void removedColumnsSoftDeletes() throws Exception {
        MetaTable table = seedTable("defsync_rm");
        definitionService.exportTo(dir, "defsync_rm");

        Path file = dir.resolve("defsync_rm.yaml");
        TableDefinition def = MetaTableDefinitionCodec.fromYaml(Files.readString(file));
        def.setColumns(def.getColumns().stream()
                .filter(c -> !c.getColumnCode().equals("status")).toList());
        def.setRemovedColumns(List.of("status"));
        Files.writeString(file, MetaTableDefinitionCodec.toYaml(def));

        SyncReport report = definitionService.syncFrom(dir, "defsync_rm", true);
        assertTrue(report.removedColumns().contains("defsync_rm.status"));
        assertTrue(report.orphanColumns().isEmpty());

        List<MetaColumn> cols = columnRepository.findByTableIdAndDeletedFalseOrderBySortAsc(java.util.Objects.requireNonNull(
                table.getId()));
        assertEquals(1, cols.size());
        assertEquals("title", cols.get(0).getColumnCode());
    }

    @Test
    void importCreatesNewTable() {
        String yaml = """
                tableCode: defsync_new
                tableName: 新表
                columns:
                  - columnCode: name
                    columnName: 名称
                    dataType: STRING
                    required: true
                """;
        try {
            Files.writeString(dir.resolve("defsync_new.yaml"), yaml);
        } catch (java.io.IOException e) {
            throw new java.io.UncheckedIOException(e);
        }

        SyncReport dry = definitionService.syncFrom(dir, "defsync_new", false);
        assertTrue(dry.createdTables().contains("defsync_new"));
        assertTrue(tableRepository.findByTableCodeAndDeletedFalse("defsync_new").isEmpty());

        definitionService.syncFrom(dir, "defsync_new", true);
        MetaTable created = tableRepository.findByTableCodeAndDeletedFalse("defsync_new").orElseThrow();
        assertEquals("meta_defsync_new", created.physicalTableName());
        List<MetaColumn> cols = columnRepository.findByTableIdAndDeletedFalseOrderBySortAsc(java.util.Objects.requireNonNull(
                created.getId()));
        assertEquals(1, cols.size());
        assertEquals("name", cols.get(0).getColumnCode());
    }

    @Test
    void reapplyingUnchangedDefinitionsWritesNothing() {
        MetaTable seeded = seedTable("defsync_idem");
        definitionService.exportTo(dir, "defsync_idem");
        Integer version = reload(seeded).getVersion();

        SyncReport first = definitionService.syncFrom(dir, "defsync_idem", true);
        SyncReport second = definitionService.syncFrom(dir, "defsync_idem", true);

        assertTrue(first.isEmpty(), "unchanged file must diff clean, got: " + first.lines());
        assertTrue(second.isEmpty(), "second apply must diff clean, got: " + second.lines());
        MetaTable after = reload(seeded);
        assertEquals(version, after.getVersion(), "no-op apply must not bump the optimistic-lock version");
        assertEquals(SEED_CREATOR, after.getCreatorId(), "audit fields must survive apply");
        assertNotNull(after.getCreateTime());
        columnRepository.findByTableIdAndDeletedFalseOrderBySortAsc(Objects.requireNonNull(seeded.getId()))
                .forEach(c -> assertEquals(SEED_CREATOR, c.getCreatorId(), "column audit wiped: " + c.getColumnCode()));
    }

    @Test
    void handWrittenPartialDefinitionConverges() throws Exception {
        write("defsync_min.yaml", minimal("defsync_min"));

        definitionService.syncFrom(dir, "defsync_min", true);
        SyncReport dryRun = definitionService.syncFrom(dir, "defsync_min", false);
        SyncReport reapply = definitionService.syncFrom(dir, "defsync_min", true);

        assertTrue(dryRun.isEmpty(), "absent keys are unmanaged — expected clean diff, got: " + dryRun.lines());
        assertTrue(reapply.isEmpty(), "re-apply must be a no-op, got: " + reapply.lines());
        MetaTable table = tableRepository.findByTableCodeAndDeletedFalse("defsync_min").orElseThrow();
        assertEquals(1, table.getStatus(), "DB default kept for a key the file does not carry");
    }

    @Test
    void invalidDefinitionRollsBackTheWholeSync() throws Exception {
        write("defsync_bad.yaml", """
                tableCode: defsync_bad
                tableName: 非法
                columns:
                  - columnCode: kind
                    columnName: 类型
                    dataType: ENUM
                """);
        write("defsync_good.yaml", minimal("defsync_good"));

        IllegalStateException e = assertThrows(IllegalStateException.class,
                () -> definitionService.syncFrom(dir, null, true));

        assertTrue(String.valueOf(e.getMessage()).contains("defsync_bad"), String.valueOf(e.getMessage()));
        assertTrue(tableRepository.findByTableCodeAndDeletedFalse("defsync_bad").isEmpty());
        assertTrue(tableRepository.findByTableCodeAndDeletedFalse("defsync_good").isEmpty(), "whole sync rolls back");
    }

    @Test
    void softDeletedTableCodeIsRevivedNotReinserted() throws Exception {
        // Same end state as MetaTableAdminServiceImpl.delete: table + columns soft-deleted.
        MetaTable seeded = seedTable("defsync_rev");
        List<MetaColumn> columns = columnRepository.findByTableIdAndDeletedFalseOrderBySortAsc(
                Objects.requireNonNull(seeded.getId()));
        columns.forEach(c -> c.setDeleted(true));
        columnRepository.saveAll(columns);
        seeded.setDeleted(true);
        tableRepository.save(seeded);
        write("defsync_rev.yaml", minimal("defsync_rev"));

        SyncReport report = definitionService.syncFrom(dir, "defsync_rev", true);

        assertTrue(report.createdTables().contains("defsync_rev"));
        MetaTable revived = tableRepository.findByTableCodeAndDeletedFalse("defsync_rev").orElseThrow();
        assertEquals(seeded.getId(), revived.getId());
        assertEquals(List.of("name"), columnRepository.findByTableIdAndDeletedFalseOrderBySortAsc(
                Objects.requireNonNull(revived.getId())).stream().map(MetaColumn::getColumnCode).toList());
    }

    @Test
    void fullSyncReportsDbTablesWithoutFileAsOrphans() {
        seedTable("defsync_orph");

        SyncReport report = definitionService.syncFrom(dir, null, false);

        assertTrue(report.orphanTables().contains("defsync_orph"), "got: " + report.lines());
        assertFalse(report.isEmpty());
    }

    @Test
    void materializeWaitsForTheApplyLockThenTimesOut() throws Exception {
        write("defsync_lock.yaml", minimal("defsync_lock"));
        FsDefinitionSource source = new FsDefinitionSource(dir);

        try (Connection holder = dataSource.getConnection()) {
            holder.setAutoCommit(false);
            try (Statement st = holder.createStatement()) {
                st.execute("SELECT pg_advisory_xact_lock(" + MetaTableDefinitionService.APPLY_LOCK_KEY + ")");
            }
            RuntimeException e = assertThrows(RuntimeException.class,
                    () -> definitionService.materialize(source, Duration.ofMillis(300)));
            assertTrue(String.valueOf(NestedExceptionUtils.getMostSpecificCause(e).getMessage()).contains("lock timeout"),
                    String.valueOf(e.getMessage()));
            holder.rollback();
        }
        assertTrue(tableRepository.findByTableCodeAndDeletedFalse("defsync_lock").isEmpty(), "timed-out apply wrote");

        definitionService.materialize(source, Duration.ofSeconds(10));
        assertTrue(tableRepository.findByTableCodeAndDeletedFalse("defsync_lock").isPresent());
    }

    @Test
    void duplicateTableCodeAcrossFilesFailsNamingBothFiles() throws Exception {
        write("dup_a.yaml", minimal("defsync_dup"));
        write("dup_b.yaml", minimal("defsync_dup"));

        IllegalStateException e = assertThrows(IllegalStateException.class,
                () -> definitionService.syncFrom(dir, null, false));

        String message = String.valueOf(e.getMessage());
        assertTrue(message.contains("dup_a.yaml") && message.contains("dup_b.yaml"), message);
    }

    @Test
    void unparseableFileFailsNamingTheFile() throws Exception {
        write("defsync_broken.yaml", "tableCode: [unclosed\n");

        IllegalStateException e = assertThrows(IllegalStateException.class,
                () -> definitionService.syncFrom(dir, null, false));

        assertTrue(String.valueOf(e.getMessage()).contains("defsync_broken.yaml"), String.valueOf(e.getMessage()));
    }

    private static String minimal(String code) {
        return "tableCode: " + code + "\n" + """
                tableName: 最小定义
                columns:
                  - columnCode: name
                    columnName: 名称
                    dataType: STRING
                    required: true
                """;
    }

    private void write(String fileName, String yaml) throws java.io.IOException {
        Files.writeString(dir.resolve(fileName), yaml);
    }

    private MetaTable reload(MetaTable table) {
        return tableRepository.findById(Objects.requireNonNull(table.getId())).orElseThrow();
    }
}
