package com.lesofn.archforge.server.admin.metatable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.lesofn.archforge.meta.table.api.dao.MetaColumnRepository;
import com.lesofn.archforge.meta.table.api.dao.MetaTableRepository;
import com.lesofn.archforge.meta.table.api.domain.MetaColumn;
import com.lesofn.archforge.meta.table.api.domain.MetaColumnType;
import com.lesofn.archforge.meta.table.api.domain.MetaTable;
import com.lesofn.archforge.meta.table.api.errors.MetaTableErrorCode;
import com.lesofn.archforge.meta.table.api.errors.MetaTableException;
import com.lesofn.archforge.meta.table.api.service.MetaTableAdminService;
import com.lesofn.archforge.meta.table.api.service.MetaTableCrudService;
import com.lesofn.archforge.server.admin.Application;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests against real PostgreSQL for P1 hardening: evolution pre-flight,
 * partial unique indexes, soft-delete re-insert and optimistic locking.
 *
 * @author sofn
 */
@SpringBootTest(
        classes = {
                Application.class, MetaTableSchemaEvolutionIntegrationTest.ProbeConfig.class
        },
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Tag("slow")
class MetaTableSchemaEvolutionIntegrationTest {

    private static final List<String> CODES = List.of(
            "p1itnotnull", "p1itbackfill", "p1itlossy", "p1itwiden", "p1ituq", "p1itreadd", "p1itlock");

    @Qualifier("metaTableJdbcTemplate")
    @Autowired
    private NamedParameterJdbcTemplate jdbc;

    @Autowired
    private MetaTableAdminService adminService;

    @Autowired
    private MetaTableCrudService crudService;

    @Autowired
    private MetaTableRepository repository;

    @Autowired
    private EvolutionProbe probe;

    @BeforeAll
    void cleanUpSeed() {
        CODES.forEach(this::cleanUp);
    }

    @TestConfiguration
    static class ProbeConfig {

        @Bean
        EvolutionProbe evolutionProbe(
                MetaTableRepository repository,
                MetaColumnRepository columnRepository,
                @Qualifier("metaTableJdbcTemplate") NamedParameterJdbcTemplate jdbcTemplate) {
            return new EvolutionProbe(repository, columnRepository, jdbcTemplate);
        }
    }

    static class EvolutionProbe {

        private final MetaTableRepository repository;
        private final MetaColumnRepository columnRepository;
        private final NamedParameterJdbcTemplate jdbcTemplate;

        EvolutionProbe(
                MetaTableRepository repository,
                MetaColumnRepository columnRepository,
                NamedParameterJdbcTemplate jdbcTemplate) {
            this.repository = repository;
            this.columnRepository = columnRepository;
            this.jdbcTemplate = jdbcTemplate;
        }

        /** Loads a managed entity, moves the row's version behind its back, then flushes. */
        @Transactional("metaTableTransactionManager")
        public void flushStale(long tableId) {
            MetaTable stale = repository.findById(tableId).orElseThrow();
            jdbcTemplate.getJdbcOperations().execute(
                    "UPDATE sys_meta_table SET version = version + 1 WHERE id = " + tableId);
            stale.setTableName(stale.getTableName() + "x");
            repository.saveAndFlush(stale);
        }

        public long countColumns(long tableId) {
            return columnRepository.findByTableIdAndDeletedFalseOrderBySortAsc(tableId).size();
        }
    }

    @Test
    void setNotNullWithoutDefaultIsRejectedWhenNullsExist() {
        Long id = createTable("p1itnotnull", stringColumn("city", null));
        crudService.insert(id, row(), 1L);

        List<MetaColumn> columns = adminService.findColumns(id);
        columns.forEach(c -> c.setRequired(true));

        MetaTableException exception = assertThrows(MetaTableException.class,
                () -> adminService.update(id, renameTo(adminService.findById(id)), columns, 1L));

        assertEquals(MetaTableErrorCode.META_TABLE_EVOLUTION_INVALID, errorCode(exception));
        assertEquals(Integer.valueOf(1), adminService.findById(id).getSchemaVersion(),
                "failed evolution must roll back entirely");
        assertEquals("YES", columnNullable("meta_p1itnotnull", "city"));
    }

    @Test
    void setNotNullWithDefaultBackfillsNullsThenAppliesConstraint() {
        Long id = createTable("p1itbackfill", stringColumn("grade", "A"));
        crudService.insert(id, row(), 1L);

        List<MetaColumn> columns = adminService.findColumns(id);
        columns.forEach(c -> c.setRequired(true));

        adminService.update(id, renameTo(adminService.findById(id)), columns, 1L);

        assertEquals("NO", columnNullable("meta_p1itbackfill", "grade"));
        Map<String, Object> stored = jdbc.queryForMap(
                "SELECT \"grade\" FROM \"meta_p1itbackfill\" WHERE id = 1", Map.of());
        assertEquals("A", stored.get("grade"));
    }

    @Test
    void incompatibleTypeChangeIsRejectedByLossPreflight() {
        Long id = createTable("p1itlossy", textColumn());
        crudService.insert(id, row("note", "abc"), 1L);

        List<MetaColumn> columns = adminService.findColumns(id);
        columns.forEach(c -> c.setDataType(MetaColumnType.INTEGER));

        MetaTableException exception = assertThrows(MetaTableException.class,
                () -> adminService.update(id, renameTo(adminService.findById(id)), columns, 1L));

        assertEquals(MetaTableErrorCode.META_TABLE_EVOLUTION_INVALID, errorCode(exception));
        assertEquals("text", columnDataType("meta_p1itlossy", "note"),
                "physical column must stay untouched after rejection");
    }

    @Test
    void compatibleWidenSucceedsWithData() {
        Long id = createTable("p1itwiden", stringColumn("note", null));
        crudService.insert(id, row("note", "hello"), 1L);

        List<MetaColumn> columns = adminService.findColumns(id);
        columns.forEach(c -> c.setLength(300));

        adminService.update(id, renameTo(adminService.findById(id)), columns, 1L);

        assertTrue(columnDataType("meta_p1itwiden", "note").startsWith("character varying"));
        Map<String, Object> stored = jdbc.queryForMap(
                "SELECT \"note\" FROM \"meta_p1itwiden\" WHERE id = 1", Map.of());
        assertEquals("hello", stored.get("note"));
    }

    @Test
    void partialUniqueIndexAllowsReinsertAfterSoftDelete() {
        Long id = createTable("p1ituq", uniqueStringColumn("tag"));
        Long first = crudService.insert(id, row("tag", "X"), 1L);
        assertTrue(crudService.softDelete(id, first, 1L));

        Long second = crudService.insert(id, row("tag", "X"), 1L);

        assertNotEquals(first, second);
        Integer duplicates = jdbc.getJdbcOperations().queryForObject(
                "SELECT COUNT(*) FROM \"meta_p1ituq\" WHERE \"tag\" = 'X' AND deleted = 0", Integer.class);
        assertEquals(1, duplicates);
    }

    @Test
    void droppedColumnCanBeReAddedWithSameCode() {
        Long id = createTable("p1itreadd", stringColumn("keep", null), stringColumn("legacy", null));

        List<MetaColumn> withoutLegacy = new ArrayList<>(adminService.findColumns(id));
        withoutLegacy.removeIf(c -> c.getColumnCode().equals("legacy"));
        adminService.update(id, renameTo(adminService.findById(id)), withoutLegacy, 1L);

        List<MetaColumn> withLegacyAgain = new ArrayList<>(adminService.findColumns(id));
        withLegacyAgain.add(stringColumn("legacy", null));
        adminService.update(id, renameTo(adminService.findById(id)), withLegacyAgain, 1L);

        assertEquals(2, probe.countColumns(id));
    }

    @Test
    void optimisticLockRejectsStaleFlushWithMappedMechanics() {
        Long id = createTable("p1itlock", stringColumn("note", null));

        assertThrows(ObjectOptimisticLockingFailureException.class, () -> probe.flushStale(id));
        assertEquals(Integer.valueOf(0), adminService.findById(id).getVersion(),
                "failed flush must not persist a version bump");
    }

    // ---------- helpers ----------

    private Long createTable(String code, MetaColumn... columns) {
        MetaTable table = new MetaTable();
        table.setTableCode(code);
        table.setTableName(code);
        table.setTablePrefix("meta_");
        List<MetaColumn> columnList = new ArrayList<>();
        for (MetaColumn column : columns) {
            columnList.add(column);
        }
        return adminService.create(table, columnList);
    }

    private MetaTable renameTo(MetaTable current) {
        MetaTable incoming = new MetaTable();
        incoming.setTableName(current.getTableName());
        return incoming;
    }

    private MetaColumn stringColumn(String code, String defaultValue) {
        MetaColumn column = baseColumn(code, MetaColumnType.STRING);
        column.setLength(50);
        column.setDefaultValue(defaultValue);
        return column;
    }

    private MetaColumn textColumn() {
        return baseColumn("note", MetaColumnType.TEXT);
    }

    private MetaColumn uniqueStringColumn(String code) {
        MetaColumn column = stringColumn(code, null);
        column.setUnique(true);
        return column;
    }

    private MetaColumn baseColumn(String code, MetaColumnType type) {
        MetaColumn column = new MetaColumn();
        column.setColumnCode(code);
        column.setColumnName(code);
        column.setDataType(type);
        return column;
    }

    private Map<String, Object> row(String key, Object value) {
        return Map.of(key, value);
    }

    private Map<String, Object> row() {
        return Map.of();
    }

    private String columnNullable(String physicalTable, String column) {
        return jdbc.queryForObject(
                "SELECT is_nullable FROM information_schema.columns " + "WHERE table_name = :t AND column_name = :c",
                Map.of("t", physicalTable, "c", column),
                String.class);
    }

    private String columnDataType(String physicalTable, String column) {
        return jdbc.queryForObject(
                "SELECT data_type FROM information_schema.columns " + "WHERE table_name = :t AND column_name = :c",
                Map.of("t", physicalTable, "c", column),
                String.class);
    }

    private MetaTableErrorCode errorCode(MetaTableException exception) {
        int nodeNum = exception.getErrorInfo().getCode() % 100;
        for (MetaTableErrorCode code : MetaTableErrorCode.values()) {
            if (code.getNodeNum() == nodeNum) {
                return code;
            }
        }
        throw new AssertionError("unknown error code " + exception.getErrorInfo().getCode());
    }

    private void cleanUp(String code) {
        List<Long> ids = jdbc.query(
                "SELECT id FROM sys_meta_table WHERE table_code = :code",
                Map.of("code", code),
                (rs, rowNum) -> rs.getLong(1));
        for (Long id : ids) {
            jdbc.getJdbcOperations().execute("DELETE FROM sys_meta_table_column WHERE table_id = " + id);
            jdbc.getJdbcOperations().execute("DELETE FROM sys_meta_table WHERE id = " + id);
        }
        jdbc.getJdbcOperations().execute("DROP TABLE IF EXISTS \"meta_" + code + "\"");
    }
}
