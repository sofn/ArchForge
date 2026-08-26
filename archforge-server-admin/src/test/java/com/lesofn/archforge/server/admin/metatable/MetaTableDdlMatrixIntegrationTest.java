package com.lesofn.archforge.server.admin.metatable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lesofn.archforge.meta.table.api.domain.MetaColumn;
import com.lesofn.archforge.meta.table.api.domain.MetaColumnType;
import com.lesofn.archforge.meta.table.api.domain.MetaTable;
import com.lesofn.archforge.meta.table.api.dto.MetaDataQuery;
import com.lesofn.archforge.meta.table.api.service.MetaTableAdminService;
import com.lesofn.archforge.meta.table.api.service.MetaTableCrudService;
import com.lesofn.archforge.server.admin.Application;
import java.math.BigDecimal;
import java.sql.Array;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.postgresql.util.PGobject;

/**
 * DDL 回归矩阵：17 种字段类型 × 建表物理类型断言 + 数据写入读回 + 关键 ALTER 场景，
 * 全部跑在 Testcontainers PostgreSQL + Flyway 真实库上。
 *
 * @author sofn
 */
@SpringBootTest(
        classes = {
                Application.class
        },
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Tag("slow")
class MetaTableDdlMatrixIntegrationTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final AtomicLong SEQ = new AtomicLong(System.currentTimeMillis() % 100_000);

    @Qualifier("metaTableJdbcTemplate")
    @Autowired
    private org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate jdbc;

    @Autowired
    private MetaTableAdminService adminService;

    @Autowired
    private MetaTableCrudService crudService;

    private final List<Long> createdTables = new ArrayList<>();
    private Long referenceTargetTableId;
    private Long referenceTargetRowId;

    @AfterEach
    void cleanUp() {
        for (Long id : createdTables) {
            try {
                adminService.delete(id, true);
            } catch (RuntimeException ignored) {
                // 尽力清理，避免掩盖用例本身的失败
            }
        }
        createdTables.clear();
        referenceTargetTableId = null;
        referenceTargetRowId = null;
    }

    @ParameterizedTest(name = "{index} {0}")
    @EnumSource(MetaColumnType.class)
    void createTableRoundTripsRepresentativeValue(MetaColumnType type) {
        ensureReferenceTarget();

        MetaColumn column = column("payload", type);
        applyTypeConfig(type, column);
        Object expected = representativeValue(type);

        MetaTable table = createTable("ddlmtx" + SEQ.incrementAndGet(), "矩阵-" + type, column);

        assertPhysicalType(table.physicalTableName(), type);

        Long rowId = crudService.insert(table.getId(), Map.of("payload", expected), 1L);
        Object actual = readScalar(table.physicalTableName(), "payload", rowId);
        assertRoundTrip(type, expected, actual);

        if (type == MetaColumnType.REFERENCE) {
            List<Map<String, Object>> rows = crudService.list(table.getId(), MetaDataQuery.of(Map.of(), 1, 10)).getList();
            assertEquals("T-001", rows.get(0).get("payload_display"));
        }
    }

    @Test
    void renameColumnPreservesData() {
        MetaTable table = createTable("ddlmtrn" + SEQ.incrementAndGet(), "改名矩阵", column("origin", MetaColumnType.STRING));
        Long rowId = crudService.insert(table.getId(), Map.of("origin", "keep-me"), 1L);

        updateColumns(table, columns -> columns.get(0).setColumnCode("renamed"));

        assertFalse(physicalColumnExists(table.physicalTableName(), "origin"));
        assertTrue(physicalColumnExists(table.physicalTableName(), "renamed"));
        assertEquals("keep-me", readScalar(table.physicalTableName(), "renamed", rowId));
    }

    @Test
    void wideningVarcharToTextPreservesData() {
        MetaColumn narrow = column("payload", MetaColumnType.STRING);
        narrow.setLength(50);
        MetaTable table = createTable("ddlmtww" + SEQ.incrementAndGet(), "加宽矩阵", narrow);
        Long rowId = crudService.insert(table.getId(), Map.of("payload", "加宽保留 widen"), 1L);

        updateColumns(table, columns -> {
            columns.get(0).setDataType(MetaColumnType.TEXT);
            columns.get(0).setLength(null);
        });

        assertEquals("text", physicalDataType(table.physicalTableName(), "payload"));
        assertEquals("加宽保留 widen", readScalar(table.physicalTableName(), "payload", rowId));
    }

    @Test
    void lossyTextToIntegerAlterFailsAtomically() {
        MetaTable table = createTable("ddlmtls" + SEQ.incrementAndGet(), "收窄矩阵", column("payload", MetaColumnType.TEXT));
        Long rowId = crudService.insert(table.getId(), Map.of("payload", "abc-not-numeric"), 1L);
        Integer versionBefore = adminService.findById(table.getId()).getSchemaVersion();

        assertThrows(RuntimeException.class, () -> updateColumns(table, columns -> columns.get(0).setDataType(
                MetaColumnType.INTEGER)));

        assertEquals("text", physicalDataType(table.physicalTableName(), "payload"), "physical type must not change");
        assertEquals(MetaColumnType.TEXT, adminService.findColumns(table.getId()).get(0).getDataType(),
                "metadata type must not change");
        assertEquals(versionBefore, adminService.findById(table.getId()).getSchemaVersion());
        assertEquals("abc-not-numeric", readScalar(table.physicalTableName(), "payload", rowId));
    }

    @Test
    void setAndUnsetDefaultAppliesToNewRows() {
        MetaTable table = createTable("ddlmtx1" + SEQ.incrementAndGet(), "默认值矩阵", column("payload", MetaColumnType.STRING));

        updateColumns(table, columns -> columns.get(0).setDefaultValue("dft"));
        String def = physicalColumnDefault(table.physicalTableName(), "payload");
        assertTrue(def != null && def.contains("dft"));

        Long withDefault = insertEmptyRow(table);
        assertEquals("dft", readScalar(table.physicalTableName(), "payload", withDefault));

        updateColumns(table, columns -> columns.get(0).setDefaultValue(null));
        assertNull(physicalColumnDefault(table.physicalTableName(), "payload"));

        Long withoutDefault = insertEmptyRow(table);
        assertNull(readScalar(table.physicalTableName(), "payload", withoutDefault));
    }

    @Test
    void nullableToggleOnPopulatedColumn() {
        MetaTable table = createTable("ddlmtnl" + SEQ.incrementAndGet(), "空值矩阵", column("payload", MetaColumnType.STRING));
        crudService.insert(table.getId(), Map.of("payload", "x"), 1L);

        updateColumns(table, columns -> columns.get(0).setRequired(true));
        assertEquals("NO", physicalIsNullable(table.physicalTableName(), "payload"));

        updateColumns(table, columns -> columns.get(0).setRequired(false));
        assertEquals("YES", physicalIsNullable(table.physicalTableName(), "payload"));
    }

    private void ensureReferenceTarget() {
        if (referenceTargetRowId != null) {
            return;
        }
        MetaTable target = createTable("ddlmtxtgt" + SEQ.incrementAndGet(), "引用目标", column("code", MetaColumnType.STRING));
        referenceTargetRowId = crudService.insert(target.getId(), Map.of("code", "T-001"), 1L);
        referenceTargetTableId = target.getId();
    }

    private void assertPhysicalType(String physicalName, MetaColumnType type) {
        String dataType = physicalDataType(physicalName, "payload");
        assertEquals(expectedPgType(type), dataType.toLowerCase(), () -> type + " physical type mismatch");

        switch (type) {
            case STRING, ENUM -> assertEquals(255,
                    jdbc.getJdbcOperations().queryForObject(
                            "SELECT character_maximum_length FROM information_schema.columns " +
                                    "WHERE table_name = ? AND column_name = 'payload'",
                            Integer.class, physicalName));
            case DECIMAL -> {
                assertEquals(10, jdbc.getJdbcOperations().queryForObject(
                        "SELECT numeric_precision FROM information_schema.columns " +
                                "WHERE table_name = ? AND column_name = 'payload'",
                        Integer.class, physicalName));
                assertEquals(2, jdbc.getJdbcOperations().queryForObject(
                        "SELECT numeric_scale FROM information_schema.columns " +
                                "WHERE table_name = ? AND column_name = 'payload'",
                        Integer.class, physicalName));
            }
            default -> {
            }
        }
    }

    private String expectedPgType(MetaColumnType type) {
        return switch (type) {
            case STRING, ENUM -> "character varying";
            case TEXT -> "text";
            case INTEGER, FILE, IMAGE, REFERENCE -> "bigint";
            case DECIMAL -> "numeric";
            case BOOLEAN -> "boolean";
            case DATE -> "date";
            case DATETIME -> "timestamp without time zone";
            case TIMESTAMPTZ -> "timestamp with time zone";
            case JSON, GEO, MULTI_IMAGE -> "jsonb";
            case UUID -> "uuid";
            case ARRAY -> "array";
        };
    }

    private Object representativeValue(MetaColumnType type) {
        return switch (type) {
            case STRING -> "hello矩阵";
            case TEXT -> "多行文本\n第二行";
            case INTEGER -> 42L;
            case DECIMAL -> new BigDecimal("123.45");
            case BOOLEAN -> true;
            case DATE -> LocalDate.of(2026, 8, 26);
            case DATETIME -> "2026-08-26 10:15:30";
            case ENUM -> "A";
            case JSON -> "{\"a\":{\"b\":[1,2,{\"c\":true}]}}";
            case FILE -> 101L;
            case IMAGE -> 102L;
            case MULTI_IMAGE -> List.of("/a.png", "/b.png");
            case UUID -> "3f2504e0-4f89-11d3-9a0c-0305e82c3301";
            case TIMESTAMPTZ -> "2026-08-26T10:15:30+08:00";
            case ARRAY -> List.of("x", "y");
            case GEO -> "{\"lat\":31.23,\"lng\":121.47}";
            case REFERENCE -> referenceTargetRowId;
        };
    }

    private void assertRoundTrip(MetaColumnType type, Object expected, Object actual) {
        switch (type) {
            case STRING, TEXT, ENUM -> assertEquals(expected, actual);
            case INTEGER, FILE, IMAGE, REFERENCE -> assertEquals(((Number) expected).longValue(), ((Number) actual)
                    .longValue());
            case DECIMAL -> assertEquals(0, new BigDecimal(actual.toString()).compareTo((BigDecimal) expected));
            case BOOLEAN -> assertEquals(Boolean.TRUE, actual);
            case DATE -> assertEquals(expected, ((java.sql.Date) actual).toLocalDate());
            case DATETIME -> assertEquals(LocalDateTime.parse(expected.toString().replace(' ', 'T')), ((Timestamp) actual)
                    .toLocalDateTime());
            case JSON, GEO -> assertEquals(jsonNode(expected), jsonNode(pgValue(actual)));
            case MULTI_IMAGE -> assertEquals(jsonNode(expected), jsonNode(pgValue(actual)));
            case UUID -> assertEquals(UUID.fromString(expected.toString()), actual instanceof UUID u ? u
                    : UUID.fromString(actual.toString()));
            case TIMESTAMPTZ -> {
                java.time.Instant actualInstant = actual instanceof OffsetDateTime odt ? odt.toInstant()
                        : ((Timestamp) actual).toInstant();
                assertEquals(OffsetDateTime.parse(expected.toString()).toInstant(), actualInstant);
            }
            case ARRAY -> {
                try {
                    assertEquals(List.of("x", "y"), Arrays.asList((Object[]) ((Array) actual).getArray()));
                } catch (java.sql.SQLException e) {
                    throw new IllegalStateException(e);
                }
            }
        }
    }

    private Object pgValue(Object raw) {
        return raw instanceof PGobject pg ? pg.getValue() : raw;
    }

    private com.fasterxml.jackson.databind.JsonNode jsonNode(Object value) {
        try {
            return value instanceof String s ? MAPPER.readTree(s) : MAPPER.valueToTree(value);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private void applyTypeConfig(MetaColumnType type, MetaColumn column) {
        switch (type) {
            case ARRAY -> column.setArrayElementType("STRING");
            case ENUM -> {
                column.setDictCode("ddlmtx_dict");
                column.setLength(255);
            }
            case STRING -> column.setLength(255);
            case DECIMAL -> {
                column.setPrecision(10);
                column.setScale(2);
            }
            case REFERENCE -> {
                MetaTable target = adminService.findById(referenceTargetTableId);
                column.setReferenceTable(target.physicalTableName());
                column.setReferenceColumn("id");
                column.setDisplayExpression("ref.code");
            }
            default -> {
            }
        }
    }

    private MetaTable createTable(String code, String name, MetaColumn... cols) {
        List<MetaColumn> columns = new ArrayList<>(Arrays.asList(cols));
        for (int i = 0; i < columns.size(); i++) {
            MetaColumn column = columns.get(i);
            column.setSort(i + 1);
        }
        MetaTable table = new MetaTable();
        table.setTableCode(code);
        table.setTableName(name);
        table.setStatus(1);
        Long id = adminService.create(table, columns);
        createdTables.add(id);
        return adminService.findById(id);
    }

    private void updateColumns(MetaTable table, Consumer<List<MetaColumn>> mutator) {
        List<MetaColumn> columns = new ArrayList<>(adminService.findColumns(table.getId()));
        mutator.accept(columns);
        adminService.update(table.getId(), adminService.findById(table.getId()), columns, 1L);
    }

    private MetaColumn column(String code, MetaColumnType type) {
        MetaColumn column = new MetaColumn();
        column.setColumnCode(code);
        column.setColumnName(type.name());
        column.setDataType(type);
        column.setRequired(false);
        column.setNullable(true);
        column.setSearchable(false);
        column.setListVisible(true);
        column.setDeleted(false);
        return column;
    }

    private Long insertEmptyRow(MetaTable table) {
        return crudService.insert(table.getId(), Map.of(), 1L);
    }

    private boolean physicalColumnExists(String physicalName, String columnName) {
        Integer count = jdbc.getJdbcOperations().queryForObject(
                "SELECT COUNT(*) FROM information_schema.columns WHERE table_name = ? AND column_name = ?",
                Integer.class, physicalName, columnName);
        return count != null && count > 0;
    }

    private String physicalDataType(String physicalName, String columnName) {
        return jdbc.getJdbcOperations().queryForObject(
                "SELECT data_type FROM information_schema.columns WHERE table_name = ? AND column_name = ?",
                String.class, physicalName, columnName);
    }

    private String physicalColumnDefault(String physicalName, String columnName) {
        return jdbc.getJdbcOperations().queryForObject(
                "SELECT column_default FROM information_schema.columns WHERE table_name = ? AND column_name = ?",
                String.class, physicalName, columnName);
    }

    private String physicalIsNullable(String physicalName, String columnName) {
        return jdbc.getJdbcOperations().queryForObject(
                "SELECT is_nullable FROM information_schema.columns WHERE table_name = ? AND column_name = ?",
                String.class, physicalName, columnName);
    }

    private Object readScalar(String physicalName, String columnName, long rowId) {
        return jdbc.getJdbcOperations().queryForObject(
                "SELECT \"" + columnName + "\" FROM \"" + physicalName + "\" WHERE id = ?",
                Object.class, rowId);
    }
}
