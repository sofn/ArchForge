package com.lesofn.archforge.meta.table.internal.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.lesofn.archforge.meta.table.api.domain.MetaColumn;
import com.lesofn.archforge.meta.table.api.domain.MetaColumnType;
import com.lesofn.archforge.meta.table.api.domain.MetaTable;
import com.lesofn.archforge.meta.table.api.service.MetaTableAdminService;
import com.lesofn.archforge.meta.table.internal.validator.MetaTableValidator;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

/** Regression tests proving JSON path filter keys cannot break out of the SQL string literal. */
class MetaTableCrudServiceImplTest {

    private NamedParameterJdbcTemplate jdbcTemplate;
    private MetaTableAdminService metaTableAdminService;
    private MetaTableCrudServiceImpl service;

    @BeforeEach
    void setUp() {
        jdbcTemplate = mock(NamedParameterJdbcTemplate.class);
        metaTableAdminService = mock(MetaTableAdminService.class);
        service = new MetaTableCrudServiceImpl(jdbcTemplate, metaTableAdminService, mock(MetaTableValidator.class), mock(
                MetaTableDataInserter.class), mock(MetaTableDataExporter.class), mock(MetaTableDataImporter.class));
    }

    @Test
    void singleSegmentJsonFilterEscapesSingleQuotesInKey() {
        stubTableWithJsonColumn();
        when(jdbcTemplate.queryForList(anyString(), any(MapSqlParameterSource.class)))
                .thenReturn(List.of());

        service.list(1L, Map.of("extra.a'b OR 1=1--", "x"), 1, 10);

        assertEquals("a''b OR 1=1--", capturedJsonPathLiteral());
    }

    @Test
    void multiSegmentJsonFilterEscapesSingleQuotesInKey() {
        stubTableWithJsonColumn();
        when(jdbcTemplate.queryForList(anyString(), any(MapSqlParameterSource.class)))
                .thenReturn(List.of());

        service.list(1L, Map.of("extra.a'.b'", "x"), 1, 10);

        String sql = capturedSql();
        int start = sql.indexOf("#>> ARRAY[");
        int end = sql.indexOf("] LIKE :", start);
        String literal = sql.substring(start + "#>> ARRAY[".length(), end);
        assertEquals("'a''', 'b'''", literal);
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
