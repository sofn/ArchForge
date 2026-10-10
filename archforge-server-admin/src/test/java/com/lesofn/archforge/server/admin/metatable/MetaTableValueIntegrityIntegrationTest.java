package com.lesofn.archforge.server.admin.metatable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lesofn.archforge.common.persistence.testsupport.AbstractIntegrationTest;
import com.lesofn.archforge.meta.table.api.domain.MetaColumn;
import com.lesofn.archforge.meta.table.api.domain.MetaColumnType;
import com.lesofn.archforge.meta.table.api.domain.MetaTable;
import com.lesofn.archforge.meta.table.api.errors.MetaTableErrorCode;
import com.lesofn.archforge.meta.table.api.errors.MetaTableException;
import com.lesofn.archforge.meta.table.api.service.MetaTableAdminService;
import com.lesofn.archforge.meta.table.api.service.MetaTableCrudService;
import com.lesofn.archforge.server.admin.Application;
import com.lesofn.archforge.user.api.domain.dict.SysDictItem;
import com.lesofn.archforge.user.api.domain.dict.SysDictType;
import com.lesofn.archforge.user.api.service.dict.SysDictService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.postgresql.util.PGobject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

/** Row values through the real write path (validator → JDBC → PostgreSQL): what is accepted and how it is stored. */
@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Tag("slow")
class MetaTableValueIntegrityIntegrationTest extends AbstractIntegrationTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final AtomicLong SEQ = new AtomicLong(System.currentTimeMillis() % 100_000);

    @Qualifier("metaTableJdbcTemplate")
    @Autowired
    private NamedParameterJdbcTemplate jdbc;

    @Autowired
    private MetaTableAdminService adminService;

    @Autowired
    private MetaTableCrudService crudService;

    @Autowired
    private SysDictService dictService;

    private final List<Long> createdTables = new ArrayList<>();

    @AfterEach
    void cleanUp() {
        for (Long id : createdTables) {
            try {
                adminService.delete(id, true);
            } catch (RuntimeException ignored) {
                // best effort; must not hide the test's own failure
            }
        }
        createdTables.clear();
    }

    /** A List bound to a JSONB column used to reach PostgreSQL as "[a, b]" — invalid JSON, a 500. */
    @Test
    void jsonAndGeoColumnsStoreArraysAndObjects() throws Exception {
        MetaTable table = createTable("vijson" + SEQ.incrementAndGet(), column("tags", MetaColumnType.JSON),
                column("loc", MetaColumnType.GEO));

        Long id = crudService.insert(table.getTableCode(), Map.of("tags", List.of("a", "b"), "loc", Map.of("lat", 31.2,
                "lng", 121.5)), 1L);

        assertEquals(MAPPER.readTree("[\"a\",\"b\"]"), json(table, "tags", id));
        assertEquals(MAPPER.readTree("{\"lat\":31.2,\"lng\":121.5}"), json(table, "loc", id));
    }

    /** An ENUM column whose dictionary does not exist used to accept any value (fail-open). */
    @Test
    void enumColumnsWithoutTheirDictionaryAcceptNothing() {
        MetaColumn status = column("st", MetaColumnType.ENUM);
        status.setDictCode("vi_missing_" + SEQ.incrementAndGet());
        status.setLength(32);
        MetaTable table = createTable("vienum" + SEQ.incrementAndGet(), status);

        MetaTableException e = assertThrows(MetaTableException.class, () -> crudService.insert(table.getTableCode(), Map
                .of("st", "anything"), 1L));
        assertEquals(MetaTableErrorCode.META_COLUMN_VALUE_INVALID.getCode(), e.getErrorInfo().getCode());
    }

    @Test
    void enumValuesMustComeFromTheDictionary() {
        String dictCode = "vi_dict_" + SEQ.incrementAndGet();
        SysDictType type = new SysDictType();
        type.setDictCode(dictCode);
        type.setDictName(dictCode);
        type.setStatus(1);
        type.setSort(0);
        dictService.saveTypeWithItems(type, List.of(item("A"), item("B")));
        MetaColumn status = column("st", MetaColumnType.ENUM);
        status.setDictCode(dictCode);
        status.setLength(32);
        MetaTable table = createTable("vienumok" + SEQ.incrementAndGet(), status);

        Long id = crudService.insert(table.getTableCode(), Map.of("st", "A"), 1L);

        assertTrue(id > 0);
        MetaTableException e = assertThrows(MetaTableException.class, () -> crudService.insert(table.getTableCode(), Map
                .of("st", "xx"), 1L));
        assertEquals(MetaTableErrorCode.META_COLUMN_VALUE_INVALID.getCode(), e.getErrorInfo().getCode());
    }

    /** A unique violation was a 500 whose detail carried the constraint, the key value and the SQL state. */
    @Test
    void duplicateUniqueValuesAreAValidationErrorNotAServerError() {
        MetaColumn code = column("code", MetaColumnType.STRING);
        code.setLength(64);
        code.setUnique(true);
        code.setColumnName("编码");
        MetaTable table = createTable("viuniq" + SEQ.incrementAndGet(), code);
        crudService.insert(table.getTableCode(), Map.of("code", "X-1"), 1L);

        MetaTableException e = assertThrows(MetaTableException.class, () -> crudService.insert(table.getTableCode(), Map
                .of("code", "X-1"), 1L));

        assertEquals(MetaTableErrorCode.META_COLUMN_VALUE_INVALID.getCode(), e.getErrorInfo().getCode());
        assertTrue(String.valueOf(e.getMessage()).contains("编码"), e.getMessage());
    }

    private JsonNode json(MetaTable table, String column, Long id) throws Exception {
        Object raw = jdbc.queryForObject("SELECT \"" + column + "\" FROM \"" + table.physicalTableName() + "\" WHERE id = :id",
                Map.of("id", id), Object.class);
        return MAPPER.readTree(raw instanceof PGobject pg ? pg.getValue() : String.valueOf(raw));
    }

    private static SysDictItem item(String code) {
        SysDictItem item = new SysDictItem();
        item.setItemCode(code);
        item.setItemLabel(code);
        item.setSort(0);
        item.setStatus(1);
        return item;
    }

    private MetaTable createTable(String code, MetaColumn... cols) {
        List<MetaColumn> columns = new ArrayList<>(Arrays.asList(cols));
        for (int i = 0; i < columns.size(); i++) {
            columns.get(i).setSort(i + 1);
        }
        MetaTable table = new MetaTable();
        table.setTableCode(code);
        table.setTableName(code);
        table.setStatus(1);
        Long id = adminService.create(table, columns);
        createdTables.add(id);
        return Objects.requireNonNull(adminService.findById(id));
    }

    private static MetaColumn column(String code, MetaColumnType type) {
        MetaColumn column = new MetaColumn();
        column.setColumnCode(code);
        column.setColumnName(code);
        column.setDataType(type);
        column.setRequired(false);
        column.setNullable(true);
        column.setSearchable(false);
        column.setListVisible(true);
        column.setDeleted(false);
        return column;
    }
}
