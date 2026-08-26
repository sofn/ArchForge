package com.lesofn.archforge.meta.table.internal.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.lesofn.archforge.meta.table.api.domain.MetaColumn;
import com.lesofn.archforge.meta.table.api.domain.MetaColumnType;
import com.lesofn.archforge.meta.table.api.domain.MetaTable;
import com.lesofn.archforge.meta.table.api.dto.MetaDataQuery;
import com.lesofn.archforge.meta.table.api.dto.MetaPageResponse;
import com.lesofn.archforge.meta.table.api.errors.MetaTableException;
import com.lesofn.archforge.meta.table.api.service.MetaTableAdminService;
import com.lesofn.archforge.meta.table.internal.validator.MetaTableValidator;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

/** Regression tests for SQLi-safe JSON filters, sort whitelist and skip-count semantics. */
class MetaTableCrudServiceImplTest {

    private NamedParameterJdbcTemplate jdbcTemplate;
    private MetaTableAdminService metaTableAdminService;
    private MetaTableCrudServiceImpl service;

    @BeforeEach
    void setUp() {
        jdbcTemplate = mock(NamedParameterJdbcTemplate.class);
        metaTableAdminService = mock(MetaTableAdminService.class);
        service = new MetaTableCrudServiceImpl(jdbcTemplate, metaTableAdminService, mock(MetaTableValidator.class), mock(
                MetaTableDataInserter.class), mock(MetaTableDataExporter.class), mock(
                        MetaTableDataImporter.class));
    }

    @Test
    void singleSegmentJsonFilterEscapesSingleQuotesInKey() {
        stubTableWithJsonColumn();
        when(jdbcTemplate.queryForList(anyString(), any(MapSqlParameterSource.class)))
                .thenReturn(List.of());

        service.list(1L, MetaDataQuery.of(Map.of("extra.a'b OR 1=1--", "x"), 1, 10));

        assertEquals("a''b OR 1=1--", capturedJsonPathLiteral());
    }

    @Test
    void multiSegmentJsonFilterEscapesSingleQuotesInKey() {
        stubTableWithJsonColumn();
        when(jdbcTemplate.queryForList(anyString(), any(MapSqlParameterSource.class)))
                .thenReturn(List.of());

        service.list(1L, MetaDataQuery.of(Map.of("extra.a'.b'", "x"), 1, 10));

        String sql = capturedSql();
        int start = sql.indexOf("#>> ARRAY[");
        int end = sql.indexOf("] LIKE :", start);
        String literal = sql.substring(start + "#>> ARRAY[".length(), end);
        assertEquals("'a''', 'b'''", literal);
    }

    @Test
    void knownColumnSortIsWhitelistedAndQuoted() {
        stubTableWithJsonColumn();
        when(jdbcTemplate.queryForList(anyString(), any(MapSqlParameterSource.class)))
                .thenReturn(List.of());

        service.list(1L, new MetaDataQuery(Map.of(), 1, 10, "extra", "asc", false));

        assertTrue(capturedSql().contains("ORDER BY main.\"extra\" ASC"));
    }

    @Test
    void auditColumnSortIsAllowed() {
        stubTableWithJsonColumn();
        when(jdbcTemplate.queryForList(anyString(), any(MapSqlParameterSource.class)))
                .thenReturn(List.of());

        service.list(1L, new MetaDataQuery(Map.of(), 1, 10, "create_time", null, false));

        assertTrue(capturedSql().contains("ORDER BY main.\"create_time\" DESC"));
    }

    @Test
    void defaultSortRemainsIdDesc() {
        stubTableWithJsonColumn();
        when(jdbcTemplate.queryForList(anyString(), any(MapSqlParameterSource.class)))
                .thenReturn(List.of());

        service.list(1L, MetaDataQuery.of(Map.of(), 1, 10));

        assertTrue(capturedSql().contains("ORDER BY main.\"id\" DESC"));
    }

    @Test
    void unknownSortColumnRejected() {
        stubTableWithJsonColumn();

        assertThrows(MetaTableException.class,
                () -> service.list(1L, new MetaDataQuery(Map.of(), 1, 10, "secret_col", "ASC", false)));
    }

    @Test
    void injectedSortValueRejected() {
        stubTableWithJsonColumn();

        assertThrows(MetaTableException.class,
                () -> service.list(1L, new MetaDataQuery(Map.of(), 1, 10, "id; DROP TABLE x", "ASC", false)));
    }

    @Test
    void invalidOrderDirectionRejected() {
        stubTableWithJsonColumn();

        assertThrows(MetaTableException.class,
                () -> service.list(1L, new MetaDataQuery(Map.of(), 1, 10, "id", "DESC; DROP TABLE x", false)));
    }

    @Test
    void skipCountOmitsCountQueryAndReturnsSentinelTotal() {
        stubTableWithJsonColumn();
        when(jdbcTemplate.queryForList(anyString(), any(MapSqlParameterSource.class)))
                .thenReturn(List.of());

        MetaPageResponse<Map<String, Object>> result = service.list(1L, new MetaDataQuery(Map.of(), 2, 20, null, null, true));

        verify(jdbcTemplate, never()).queryForObject(anyString(), any(MapSqlParameterSource.class), eq(Long.class));
        assertEquals(-1L, result.getTotal());
    }

    @Test
    void countRunsByDefault() {
        stubTableWithJsonColumn();
        when(jdbcTemplate.queryForList(anyString(), any(MapSqlParameterSource.class)))
                .thenReturn(List.of());

        service.list(1L, MetaDataQuery.of(Map.of(), 1, 10));

        verify(jdbcTemplate).queryForObject(anyString(), any(MapSqlParameterSource.class), eq(Long.class));
    }

    /** Extracts the single-segment JSON path literal and asserts every quote inside it is doubled. */
    private String capturedJsonPathLiteral() {
        String sql = capturedSql();
        int start = sql.indexOf(" ->> '");
        int end = sql.indexOf("' LIKE :", start);
        String literal = sql.substring(start + " ->> '".length(), end);
        assertEquals(0, countUnescapedQuotes(literal), "raw single quote must not survive in literal: " + literal);
        return literal;
    }

    private static int countUnescapedQuotes(String literal) {
        int count = 0;
        for (int i = 0; i < literal.length(); i++) {
            if (literal.charAt(i) == '\'') {
                if (i + 1 < literal.length() && literal.charAt(i + 1) == '\'') {
                    i++;
                } else {
                    count++;
                }
            }
        }
        return count;
    }

    private String capturedSql() {
        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(jdbcTemplate).queryForList(sqlCaptor.capture(), any(MapSqlParameterSource.class));
        return sqlCaptor.getValue();
    }

    private void stubTableWithJsonColumn() {
        MetaTable table = new MetaTable();
        table.setTableCode("testtable");
        table.setTablePrefix("meta_");
        when(metaTableAdminService.findById(1L)).thenReturn(table);
        MetaColumn column = new MetaColumn();
        column.setColumnCode("extra");
        column.setDataType(MetaColumnType.JSON);
        column.setSearchable(true);
        when(metaTableAdminService.findColumns(1L)).thenReturn(List.of(column));
        when(jdbcTemplate.queryForObject(anyString(), any(MapSqlParameterSource.class), eq(Long.class)))
                .thenReturn(0L);
    }
}
