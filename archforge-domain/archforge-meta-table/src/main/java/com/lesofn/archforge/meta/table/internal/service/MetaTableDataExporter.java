package com.lesofn.archforge.meta.table.internal.service;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lesofn.archforge.meta.table.api.domain.MetaColumn;
import com.lesofn.archforge.meta.table.api.domain.MetaColumnType;
import com.lesofn.archforge.meta.table.api.domain.MetaTable;
import com.lesofn.archforge.meta.table.api.enums.MetaDataFormat;
import com.lesofn.archforge.meta.table.api.errors.MetaTableErrorCode;
import com.lesofn.archforge.meta.table.api.errors.MetaTableException;
import com.lesofn.archforge.meta.table.api.service.MetaTableAdminService;
import com.lesofn.archforge.meta.table.internal.config.MetaTableTransferProperties;
import com.lesofn.archforge.meta.table.internal.ddl.SqlIdentifier;
import com.lesofn.archforge.meta.table.internal.validator.MetaTableValidator;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.sql.Array;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.dhatim.fastexcel.Workbook;
import org.dhatim.fastexcel.Worksheet;
import org.postgresql.util.PGobject;
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
    private final MetaTableTransferProperties transferProperties;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public void export(Long tableId, MetaDataFormat format, OutputStream out) {
        MetaTable table = metaTableAdminService.findById(tableId);
        List<MetaColumn> columns = metaTableAdminService.findColumns(tableId);
        enforceRowLimit(table);

        switch (format) {
            case EXCEL -> exportExcel(table, columns, out);
            case CSV -> exportCsv(table, columns, out);
            case JSON -> exportJson(table, columns, out);
            default -> throw new MetaTableException(MetaTableErrorCode.META_COLUMN_VALUE_INVALID, "不支持的导出格式: " + format);
        }
    }

    private void enforceRowLimit(MetaTable table) {
        Long total = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM " + SqlIdentifier.quote(table.physicalTableName()) + " WHERE deleted = 0",
                Map.of(), Long.class);
        long maxRows = transferProperties.getMaxExportRows();
        if (total != null && total > maxRows) {
            throw new MetaTableException("导出行数 " + total + " 超过上限 " + maxRows + "，请添加过滤条件缩小导出范围");
        }
    }

    private void exportExcel(MetaTable table, List<MetaColumn> columns, OutputStream out) {
        List<MetaColumn> visibleColumns = columns.stream()
                .filter(MetaColumn::isListVisibleColumn)
                .toList();
        List<String> headers = visibleColumns.stream()
                .map(MetaColumn::getColumnName)
                .toList();
        try (Workbook workbook = new Workbook(out, "ArchForge", "1.0")) {
            Worksheet worksheet = workbook.newWorksheet(table.getTableName());
            for (int c = 0; c < headers.size(); c++) {
                worksheet.value(0, c, headers.get(c));
            }
            int[] rowIndex = {
                    1
            };
            forEachRow(table, columns, row -> {
                int r = rowIndex[0]++;
                for (int c = 0; c < visibleColumns.size(); c++) {
                    String value = sanitizeCell(validator.formatValue(visibleColumns.get(c),
                            getExportValue(visibleColumns.get(c), row)));
                    if (!value.isEmpty()) {
                        worksheet.value(r, c, value);
                    }
                }
            });
        } catch (IOException e) {
            throw new MetaTableException(MetaTableErrorCode.META_COLUMN_VALUE_INVALID, "导出 Excel 失败");
        }
    }

    private void exportCsv(MetaTable table, List<MetaColumn> columns, OutputStream out) {
        List<MetaColumn> visibleColumns = columns.stream()
                .filter(MetaColumn::isListVisibleColumn)
                .toList();

        List<String> headers = visibleColumns.stream()
                .map(MetaColumn::getColumnName)
                .toList();

        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8));
                CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT.builder().setHeader(headers.toArray(
                        new String[0])).build())) {
            forEachRow(table, columns, row -> {
                List<String> values = visibleColumns.stream()
                        .map(c -> sanitizeCell(validator.formatValue(c, getExportValue(c, row))))
                        .toList();
                printer.printRecord(values);
            });
            printer.flush();
        } catch (IOException e) {
            throw new MetaTableException(MetaTableErrorCode.META_COLUMN_VALUE_INVALID, "导出 CSV 失败");
        }
    }

    private void exportJson(MetaTable table, List<MetaColumn> columns, OutputStream out) {
        try (JsonGenerator generator = objectMapper.getFactory().createGenerator(out, JsonEncoding.UTF8)) {
            generator.setCodec(objectMapper);
            generator.useDefaultPrettyPrinter();
            generator.writeStartArray();
            forEachRow(table, columns, row -> {
                Map<String, Object> item = new LinkedHashMap<>();
                for (MetaColumn column : columns) {
                    item.put(column.getColumnCode(), toJsonValue(row.get(column.getColumnCode())));
                }
                generator.writeObject(item);
            });
            generator.writeEndArray();
            generator.flush();
        } catch (IOException e) {
            throw new MetaTableException(MetaTableErrorCode.META_COLUMN_VALUE_INVALID, "导出 JSON 失败");
        }
    }

    private void forEachRow(MetaTable table, List<MetaColumn> columns, RowConsumer consumer) throws IOException {
        String physicalName = SqlIdentifier.quote(table.physicalTableName());
        String mainAlias = "main";
        List<String> selectColumns = ReferenceDisplayBuilder.buildSelectColumns(columns, mainAlias);
        List<String> joins = ReferenceDisplayBuilder.buildJoins(columns, mainAlias);
        String querySql = "SELECT " + String.join(", ", selectColumns) + " FROM " + physicalName + " " + mainAlias +
                " " + String.join(" ", joins) + " WHERE " + mainAlias + ".deleted = 0 AND " + mainAlias +
                ".id > :lastId ORDER BY " + mainAlias + ".id ASC LIMIT :chunkSize";
        int chunkSize = transferProperties.getExportChunkSize();
        long lastId = 0L;
        while (true) {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(querySql, Map.of("lastId", lastId, "chunkSize",
                    chunkSize));
            if (rows.isEmpty()) {
                break;
            }
            for (Map<String, Object> row : rows) {
                consumer.accept(row);
            }
            lastId = ((Number) rows.get(rows.size() - 1).get("id")).longValue();
            if (rows.size() < chunkSize) {
                break;
            }
        }
    }

    private Object getExportValue(MetaColumn column, Map<String, Object> row) {
        if (column.getDataType() == MetaColumnType.REFERENCE) {
            Object display = row.get(column.getColumnCode() + "_display");
            if (display != null) {
                return display;
            }
        }
        return row.get(column.getColumnCode());
    }

    private String sanitizeCell(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        char first = value.charAt(0);
        if (first == '=' || first == '+' || first == '-' || first == '@' || first == '\t' || first == '\r') {
            return "'" + value;
        }
        return value;
    }

    private Object toJsonValue(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number || value instanceof Boolean) {
            return value;
        }
        if (value instanceof PGobject pgObject) {
            return pgObject.getValue();
        }
        if (value instanceof Array sqlArray) {
            try {
                Object array = sqlArray.getArray();
                if (array instanceof Object[]) {
                    return Arrays.asList((Object[]) array);
                }
                return array.toString();
            } catch (SQLException e) {
                throw new MetaTableException(MetaTableErrorCode.META_COLUMN_VALUE_INVALID, "读取数组字段失败");
            }
        }
        return value.toString();
    }

    private interface RowConsumer {
        void accept(Map<String, Object> row) throws IOException;
    }
}
