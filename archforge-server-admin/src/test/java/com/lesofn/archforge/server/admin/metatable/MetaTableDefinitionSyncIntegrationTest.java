package com.lesofn.archforge.server.admin.metatable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.lesofn.archforge.common.persistence.testsupport.AbstractIntegrationTest;
import com.lesofn.archforge.meta.table.api.dao.MetaColumnRepository;
import com.lesofn.archforge.meta.table.api.dao.MetaTableRepository;
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
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * DB ↔ YAML definition sync against real PostgreSQL: export fidelity, dry-run
 * diff, apply upsert, orphan-column safety and explicit removedColumns delete.
 */
@SpringBootTest(classes = {
        Application.class
}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Tag("slow")
class MetaTableDefinitionSyncIntegrationTest extends AbstractIntegrationTest {

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
        columnRepository.save(title);

        MetaColumn status = new MetaColumn();
        status.setTableId(java.util.Objects.requireNonNull(table.getId()));
        status.setColumnCode("status");
        status.setColumnName("状态");
        status.setDataType(MetaColumnType.INTEGER);
        status.setSort(2);
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
}
