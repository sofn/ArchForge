package com.lesofn.archforge.meta.table.internal.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lesofn.archforge.common.utils.excel.FastExcelUtil;
import com.lesofn.archforge.meta.table.api.domain.MetaColumn;
import com.lesofn.archforge.meta.table.api.domain.MetaTable;
import com.lesofn.archforge.meta.table.api.enums.MetaDataFormat;
import com.lesofn.archforge.meta.table.api.service.MetaTableAdminService;
import com.lesofn.archforge.meta.table.internal.ddl.SqlIdentifier;
import com.lesofn.archforge.meta.table.internal.exception.MetaTableErrorCode;
import com.lesofn.archforge.meta.table.internal.exception.MetaTableException;
import com.lesofn.archforge.meta.table.internal.validator.MetaTableValidator;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * 元表格数据导出器。
 */
@Component
@RequiredArgsConstructor
public class MetaTableDataExporter {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final MetaTableAdminService metaTableAdminService;
    private final MetaTableValidator validator;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public void export(Long tableId, MetaDataFormat format, OutputStream out) {
        MetaTable table = metaTableAdminService.findById(tableId);
        List<MetaColumn> columns = metaTableAdminService.findColumns(tableId);

        switch (format) {
            case EXCEL -> exportExcel(table, columns, out);
            case CSV -> exportCsv(table, columns, out);
            case JSON -> exportJson(table, columns, out);
            default -> throw new MetaTableException(MetaTableErrorCode.META_COLUMN_VALUE_INVALID, "不支持的导出格式: " + format);
        }
    }

    private void exportExcel(MetaTable table, List<MetaColumn> columns, OutputStream out) {
        List<MetaColumn> visibleColumns = columns.stream()
                .filter(MetaColumn::isListVisibleColumn)
                .toList();

        List<Map<String, Object>> rows = queryAllRows(table, columns);
        List<String> headers = visibleColumns.stream()
                .map(MetaColumn::getColumnName)
                .toList();
        List<List<String>> body = rows.stream()
                .map(row -> visibleColumns.stream()
                        .map(c -> validator.formatValue(c, row.get(c.getColumnCode())))
                        .collect(Collectors.toList()))
                .collect(Collectors.toList());

        try {
            FastExcelUtil.write(out, table.getTableName(), headers, body);
        } catch (IOException e) {
            throw new MetaTableException(MetaTableErrorCode.META_COLUMN_VALUE_INVALID, "导出 Excel 失败");
        }
    }

    private void exportCsv(MetaTable table, List<MetaColumn> columns, OutputStream out) {
        List<MetaColumn> visibleColumns = columns.stream()
                .filter(MetaColumn::isListVisibleColumn)
                .toList();

        List<Map<String, Object>> rows = queryAllRows(table, columns);

        List<String> headers = visibleColumns.stream()
                .map(MetaColumn::getColumnName)
                .toList();

        try (OutputStreamWriter writer = new OutputStreamWriter(out, StandardCharsets.UTF_8);
                CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT.builder().setHeader(headers.toArray(
                        new String[0])).build())) {
            for (Map<String, Object> row : rows) {
                List<String> values = visibleColumns.stream()
                        .map(c -> validator.formatValue(c, row.get(c.getColumnCode())))
                        .toList();
                printer.printRecord(values);
            }
            printer.flush();
        } catch (IOException e) {
            throw new MetaTableException(MetaTableErrorCode.META_COLUMN_VALUE_INVALID, "导出 CSV 失败");
        }
    }

    private void exportJson(MetaTable table, List<MetaColumn> columns, OutputStream out) {
        List<Map<String, Object>> rows = queryAllRows(table, columns);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            Map<String, Object> item = new LinkedHashMap<>();
            for (MetaColumn column : columns) {
                Object value = row.get(column.getColumnCode());
                item.put(column.getColumnCode(), toJsonValue(value));
            }
            result.add(item);
        }
        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(out, result);
        } catch (IOException e) {
            throw new MetaTableException(MetaTableErrorCode.META_COLUMN_VALUE_INVALID, "导出 JSON 失败");
        }
    }

    private List<Map<String, Object>> queryAllRows(MetaTable table, List<MetaColumn> columns) {
        String physicalName = SqlIdentifier.quote(table.physicalTableName());
        List<String> quotedColumns = new ArrayList<>();
        quotedColumns.add(SqlIdentifier.quote("id"));
        for (MetaColumn column : columns) {
            quotedColumns.add(SqlIdentifier.quote(column.getColumnCode()));
        }
        String querySql = "SELECT " + String.join(", ", quotedColumns) + " FROM " + physicalName +
                " WHERE deleted = 0 ORDER BY id DESC";
        return jdbcTemplate.queryForList(querySql, Map.of());
    }

    private Object toJsonValue(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number || value instanceof Boolean) {
            return value;
        }
        return value.toString();
    }
}
