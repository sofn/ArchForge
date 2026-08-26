package com.lesofn.archforge.meta.table.internal.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.lesofn.archforge.meta.table.api.domain.MetaColumn;
import com.lesofn.archforge.meta.table.api.domain.MetaColumnType;
import com.lesofn.archforge.meta.table.api.domain.MetaTable;
import com.lesofn.archforge.meta.table.api.dto.ImportResponse;
import com.lesofn.archforge.meta.table.api.enums.MetaDataFormat;
import com.lesofn.archforge.meta.table.api.errors.MetaTableException;
import com.lesofn.archforge.meta.table.api.service.MetaTableAdminService;
import com.lesofn.archforge.meta.table.internal.config.MetaTableTransferProperties;
import com.lesofn.archforge.meta.table.internal.validator.MetaTableValidator;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

/** Unit tests for import limits, batching and the per-run reference existence cache. */
class MetaTableDataImporterTest {

    private NamedParameterJdbcTemplate jdbcTemplate;
    private MetaTableAdminService adminService;
    private MetaTableTransferProperties properties;
    private MetaTableDataImporter importer;

    @BeforeEach
    void setUp() {
        jdbcTemplate = mock(NamedParameterJdbcTemplate.class);
        adminService = mock(MetaTableAdminService.class);
        properties = new MetaTableTransferProperties();
        importer = new MetaTableDataImporter(adminService, jdbcTemplate, new MetaTableValidator(), properties);
    }

    @Test
    void csvValidRowsAreBatchedIntoSingleInsert() {
        ImportResponse response = importCsv("""
                name,age
                a,1
                b,2
                c,3
                """);

        assertEquals(3, response.getTotal());
        assertEquals(3, response.getSuccess());
        assertEquals(0, response.getFailed());

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<SqlParameterSource[]> batchCaptor = ArgumentCaptor.forClass(SqlParameterSource[].class);
        verify(jdbcTemplate).batchUpdate(sqlCaptor.capture(), batchCaptor.capture());
        assertTrue(sqlCaptor.getValue().startsWith("INSERT INTO \"meta_t1\""));
        assertEquals(3, batchCaptor.getValue().length);
        MapSqlParameterSource first = (MapSqlParameterSource) batchCaptor.getValue()[0];
        assertEquals("a", first.getValue("name"));
        assertEquals(1L, first.getValue("age"));
        assertEquals(0, first.getValue("deleted"));
    }

    @Test
    void csvInvalidRowIsRecordedWhileValidRowsStillBatch() {
        ImportResponse response = importCsv("""
                name,age
                a,1
                b,not-a-number
                c,3
                """);

        assertEquals(3, response.getTotal());
        assertEquals(2, response.getSuccess());
        assertEquals(1, response.getFailed());
        assertTrue(response.getErrors().get(0).startsWith("第 3 行: "), response.getErrors().toString());
        assertTrue(response.getErrors().get(0).contains("10408"));

        ArgumentCaptor<SqlParameterSource[]> batchCaptor = ArgumentCaptor.forClass(SqlParameterSource[].class);
        verify(jdbcTemplate).batchUpdate(anyString(), batchCaptor.capture());
        assertEquals(2, batchCaptor.getValue().length);
    }

    @Test
    void oversizedFileIsRejectedBeforeParse() {
        properties.setMaxFileBytes(16);

        MetaTableException e = assertThrows(
                MetaTableException.class,
                () -> importCsv("name,age\naaaa,11111\nbbbb,22222\n"));

        assertTrue(e.getMessage().contains("大小上限 16"));
        verify(jdbcTemplate, times(0)).batchUpdate(anyString(), any(SqlParameterSource[].class));
    }

    @Test
    void rowCountBeyondLimitIsRejectedWithProcessedCount() {
        properties.setMaxImportRows(2);

        MetaTableException e = assertThrows(
                MetaTableException.class,
                () -> importCsv("""
                        name,age
                        a,1
                        b,2
                        c,3
                        """));

        assertTrue(e.getMessage().contains("已处理 2 行"));
    }

    @Test
    void errorListIsCappedAndFlaggedTruncated() {
        properties.setMaxErrorList(2);

        ImportResponse response = importCsv("""
                name,age
                a,bad
                b,bad
                c,bad
                d,bad
                e,bad
                """);

        assertEquals(5, response.getTotal());
        assertEquals(5, response.getFailed());
        assertEquals(2, response.getErrors().size());
        assertTrue(response.isErrorTruncated());
    }

    @Test
    void duplicateReferenceValuesHitDatabaseOnlyOnce() {
        when(jdbcTemplate.queryForObject(anyString(), anyMap(), eq(Integer.class)))
                .thenReturn(1);

        ImportResponse response = importCsv(refColumns(), """
                ref,name
                7,a
                7,b
                """);

        assertEquals(2, response.getSuccess());
        verify(jdbcTemplate, times(1)).queryForObject(anyString(), anyMap(), eq(Integer.class));

        ArgumentCaptor<SqlParameterSource[]> batchCaptor = ArgumentCaptor.forClass(SqlParameterSource[].class);
        verify(jdbcTemplate).batchUpdate(anyString(), batchCaptor.capture());
        assertEquals(7L, ((MapSqlParameterSource) batchCaptor.getValue()[0]).getValue("ref"));
    }

    @Test
    void missingReferenceFailsEveryDependentRow() {
        when(jdbcTemplate.queryForObject(anyString(), anyMap(), eq(Integer.class)))
                .thenReturn(0);

        ImportResponse response = importCsv(refColumns(), """
                ref,name
                7,a
                8,b
                """);

        assertEquals(0, response.getSuccess());
        assertEquals(2, response.getFailed());
        assertTrue(response.getErrors().get(0).contains("引用的记录不存在: 7"));
    }

    @Test
    void jsonArrayPayloadImportsAndNonArrayRootIsRejected() {
        ImportResponse response = importJson("""
                [{"name":"a","age":1},{"name":"b","age":2}]
                """);
        assertEquals(2, response.getSuccess());

        assertThrows(MetaTableException.class, () -> importJson("{\"name\":\"a\"}"));
    }

    private ImportResponse importCsv(List<MetaColumn> columns, String csv) {
        return doImport(MetaDataFormat.CSV, columns, csv);
    }

    private ImportResponse importJson(List<MetaColumn> columns, String json) {
        return doImport(MetaDataFormat.JSON, columns, json);
    }

    private ImportResponse doImport(MetaDataFormat format, List<MetaColumn> columns, String payload) {
        when(adminService.findById(1L)).thenReturn(stubTable());
        when(adminService.findColumns(1L)).thenReturn(columns);
        return importer.importData(
                1L,
                format,
                new ByteArrayInputStream(payload.getBytes(StandardCharsets.UTF_8)),
                9L);
    }

    private ImportResponse importCsv(String csv) {
        return importCsv(basicColumns(), csv);
    }

    private ImportResponse importJson(String json) {
        return importJson(basicColumns(), json);
    }

    private MetaTable stubTable() {
        MetaTable table = new MetaTable();
        table.setTableCode("t1");
        table.setTablePrefix("meta_");
        return table;
    }

    private List<MetaColumn> basicColumns() {
        MetaColumn name = column("name", "名称", MetaColumnType.STRING);
        MetaColumn age = column("age", "年龄", MetaColumnType.INTEGER);
        List<MetaColumn> columns = new ArrayList<>();
        columns.add(name);
        columns.add(age);
        return columns;
    }

    private List<MetaColumn> refColumns() {
        MetaColumn ref = column("ref", "引用", MetaColumnType.REFERENCE);
        ref.setReferenceTable("meta_target");
        ref.setReferenceColumn("id");
        MetaColumn name = column("name", "名称", MetaColumnType.STRING);
        List<MetaColumn> columns = new ArrayList<>();
        columns.add(ref);
        columns.add(name);
        return columns;
    }

    private MetaColumn column(String code, String name, MetaColumnType type) {
        MetaColumn column = new MetaColumn();
        column.setColumnCode(code);
        column.setColumnName(name);
        column.setDataType(type);
        return column;
    }
}
