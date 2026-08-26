package com.lesofn.archforge.meta.table.internal.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lesofn.archforge.meta.table.api.domain.MetaColumn;
import com.lesofn.archforge.meta.table.api.domain.MetaColumnType;
import com.lesofn.archforge.meta.table.api.domain.MetaTable;
import com.lesofn.archforge.meta.table.api.enums.MetaDataFormat;
import com.lesofn.archforge.meta.table.api.errors.MetaTableException;
import com.lesofn.archforge.meta.table.api.service.MetaTableAdminService;
import com.lesofn.archforge.meta.table.internal.config.MetaTableTransferProperties;
import com.lesofn.archforge.meta.table.internal.validator.MetaTableValidator;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.dhatim.fastexcel.reader.ReadableWorkbook;
import org.dhatim.fastexcel.reader.Row;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

/** Unit tests for export row caps, keyset chunked reads and formula-injection neutralization. */
class MetaTableDataExporterTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private NamedParameterJdbcTemplate jdbcTemplate;
    private MetaTableAdminService adminService;
    private MetaTableTransferProperties properties;
    private MetaTableDataExporter exporter;

    @BeforeEach
    void setUp() {
        jdbcTemplate = mock(NamedParameterJdbcTemplate.class);
        adminService = mock(MetaTableAdminService.class);
        properties = new MetaTableTransferProperties();
        exporter = new MetaTableDataExporter(jdbcTemplate, adminService, new MetaTableValidator(), properties);
        when(adminService.findById(1L)).thenReturn(stubTable());
        when(adminService.findColumns(1L)).thenReturn(nameColumns());
        when(jdbcTemplate.queryForObject(contains("COUNT(*)"), anyMap(), eq(Long.class)))
                .thenReturn(2L);
    }

    @Test
    void csvExportNeutralizesFormulaPrefixesAndUsesKeysetChunks() throws Exception {
        properties.setExportChunkSize(2);
        when(jdbcTemplate.queryForList(anyString(), anyMap()))
                .thenReturn(List.of(row(1L, "=cmd|/c calc"), row(2L, "+add")))
                .thenReturn(List.of(row(3L, "plain")));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        exporter.export(1L, MetaDataFormat.CSV, out);

        String csv = out.toString(StandardCharsets.UTF_8);
        List<String> lines = csv.lines().collect(Collectors.toList());
        assertEquals("名称", lines.get(0));
        assertTrue(lines.get(1).contains("'=cmd|/c calc"), csv);
        assertTrue(lines.get(2).contains("'+add"), csv);
        assertEquals("plain", lines.get(3));

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> paramCaptor = ArgumentCaptor.forClass(
                (Class<Map<String, Object>>) (Class<?>) Map.class);
        verify(jdbcTemplate, times(2)).queryForList(sqlCaptor.capture(), paramCaptor.capture());
        String sql = sqlCaptor.getValue();
        assertTrue(sql.contains("deleted = 0 AND main.id > :lastId"));
        assertTrue(sql.contains("ORDER BY main.id ASC LIMIT :chunkSize"));
        assertEquals(0L, paramCaptor.getAllValues().get(0).get("lastId"));
        assertEquals(2L, paramCaptor.getAllValues().get(1).get("lastId"));
    }

    @Test
    void excelExportStreamsSanitizedCells() throws Exception {
        when(jdbcTemplate.queryForList(anyString(), anyMap()))
                .thenReturn(List.of(row(1L, "@at"), row(2L, "-minus")))
                .thenReturn(List.of());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        exporter.export(1L, MetaDataFormat.EXCEL, out);

        try (ReadableWorkbook workbook = new ReadableWorkbook(new ByteArrayInputStream(out.toByteArray()))) {
            List<List<String>> grid;
            try (java.util.stream.Stream<Row> rows = workbook.getFirstSheet().openStream()) {
                grid = rows.map(r -> r.stream()
                        .map(c -> c == null ? "" : c.getRawValue())
                        .collect(Collectors.toList()))
                        .collect(Collectors.toList());
            }
            assertEquals(3, grid.size());
            assertEquals("名称", grid.get(0).get(0));
            assertEquals("'@at", grid.get(1).get(0));
            assertEquals("'-minus", grid.get(2).get(0));
        }
    }

    @Test
    void jsonExportStreamsArrayWithoutCellMutation() throws Exception {
        when(jdbcTemplate.queryForList(anyString(), anyMap()))
                .thenReturn(List.of(row(1L, "=keep")))
                .thenReturn(List.of());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        exporter.export(1L, MetaDataFormat.JSON, out);

        JsonNode root = objectMapper.readTree(out.toByteArray());
        assertTrue(root.isArray());
        assertEquals(1, root.size());
        assertEquals("=keep", root.get(0).get("name").asText());
        assertEquals(1L, root.get(0).get("id").asLong());
    }

    @Test
    void exportBeyondRowCapIsRejectedWithFilterHint() {
        when(jdbcTemplate.queryForObject(contains("COUNT(*)"), anyMap(), eq(Long.class)))
                .thenReturn(50_001L);

        MetaTableException e = assertThrows(
                MetaTableException.class,
                () -> exporter.export(1L, MetaDataFormat.CSV, new ByteArrayOutputStream()));

        assertTrue(e.getMessage().contains("超过上限 50000"));
        assertTrue(e.getMessage().contains("过滤条件"));
    }

    private MetaTable stubTable() {
        MetaTable table = new MetaTable();
        table.setTableCode("t1");
        table.setTableName("导出表");
        table.setTablePrefix("meta_");
        return table;
    }

    private List<MetaColumn> nameColumns() {
        MetaColumn id = new MetaColumn();
        id.setColumnCode("id");
        id.setColumnName("ID");
        id.setDataType(MetaColumnType.INTEGER);
        MetaColumn name = new MetaColumn();
        name.setColumnCode("name");
        name.setColumnName("名称");
        name.setDataType(MetaColumnType.STRING);
        name.setListVisible(true);
        List<MetaColumn> columns = new ArrayList<>();
        columns.add(id);
        columns.add(name);
        return columns;
    }

    private Map<String, Object> row(long id, String name) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", id);
        row.put("name", name);
        return row;
    }
}
