package com.lesofn.archforge.meta.table.internal.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.lesofn.archforge.meta.table.api.domain.MetaColumn;
import com.lesofn.archforge.meta.table.api.domain.MetaTable;
import com.lesofn.archforge.meta.table.api.dto.ImportResult;
import com.lesofn.archforge.meta.table.api.enums.MetaDataFormat;
import com.lesofn.archforge.meta.table.api.service.MetaTableAdminService;
import com.lesofn.archforge.meta.table.internal.exception.MetaTableErrorCode;
import com.lesofn.archforge.meta.table.internal.exception.MetaTableException;
import com.lesofn.archforge.meta.table.internal.validator.MetaTableValidator;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;

/**
 * 元表格数据导入器。
 */
@Component
@RequiredArgsConstructor
public class MetaTableDataImporter {

    private final MetaTableAdminService metaTableAdminService;
    private final MetaTableDataInserter inserter;
    private final MetaTableValidator validator;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ImportResult importData(Long tableId, MetaDataFormat format, InputStream in, Long currentUid) {
        if (format == MetaDataFormat.CSV) {
            return importCsv(tableId, in, currentUid);
        }
        if (format == MetaDataFormat.JSON) {
            return importJson(tableId, in, currentUid);
        }
        throw new MetaTableException(MetaTableErrorCode.META_COLUMN_VALUE_INVALID, "不支持的导入格式: " + format);
    }

    private ImportResult importCsv(Long tableId, InputStream in, Long currentUid) {
        MetaTable table = metaTableAdminService.findById(tableId);
        List<MetaColumn> columns = metaTableAdminService.findColumns(tableId);

        ImportContext ctx = new ImportContext(table, columns, currentUid);

        try (InputStreamReader reader = new InputStreamReader(in, StandardCharsets.UTF_8);
                CSVParser parser = new CSVParser(reader, CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true)
                        .build())) {
            Map<String, MetaColumn> headerToColumn = buildHeaderMapping(columns, parser.getHeaderNames());
            int rowNum = 1;
            for (CSVRecord record : parser) {
                rowNum++;
                Map<String, Object> row = new HashMap<>();
                for (String header : parser.getHeaderNames()) {
                    MetaColumn column = headerToColumn.get(normalizeHeader(header));
                    if (column == null) {
                        continue;
                    }
                    String value = record.get(header);
                    if (value != null && !value.isEmpty()) {
                        row.put(column.getColumnCode(), value.trim());
                    }
                }
                processRow(ctx, row, rowNum);
            }
        } catch (IOException e) {
            throw new MetaTableException(MetaTableErrorCode.META_COLUMN_VALUE_INVALID, "读取 CSV 失败: " + e.getMessage());
        }

        return ImportResult.of(ctx.total, ctx.success, ctx.errors);
    }

    private ImportResult importJson(Long tableId, InputStream in, Long currentUid) {
        MetaTable table = metaTableAdminService.findById(tableId);
        List<MetaColumn> columns = metaTableAdminService.findColumns(tableId);
        ImportContext ctx = new ImportContext(table, columns, currentUid);

        try {
            JsonNode root = objectMapper.readTree(in);
            if (!root.isArray()) {
                throw new MetaTableException(MetaTableErrorCode.META_COLUMN_VALUE_INVALID, "JSON 导入数据必须是数组");
            }
            int rowNum = 0;
            for (JsonNode node : root) {
                rowNum++;
                if (!node.isObject()) {
                    ctx.errors.add("第 " + rowNum + " 行: 不是对象");
                    continue;
                }
                Map<String, Object> row = new HashMap<>();
                ObjectNode objectNode = (ObjectNode) node;
                for (Map.Entry<String, JsonNode> entry : objectNode.properties()) {
                    row.put(entry.getKey(), jsonNodeToValue(entry.getValue()));
                }
                processRow(ctx, row, rowNum);
            }
        } catch (IOException e) {
            throw new MetaTableException(MetaTableErrorCode.META_COLUMN_VALUE_INVALID, "读取 JSON 失败: " + e.getMessage());
        }

        return ImportResult.of(ctx.total, ctx.success, ctx.errors);
    }

    private void processRow(ImportContext ctx, Map<String, Object> row, int rowNum) {
        ctx.total++;
        try {
            validator.validateValues(row, ctx.columns, true);
            inserter.insert(ctx.table, ctx.columns, row, ctx.currentUid);
            ctx.success++;
        } catch (MetaTableException e) {
            ctx.errors.add("第 " + rowNum + " 行: " + e.getMessage());
        } catch (Exception e) {
            ctx.errors.add("第 " + rowNum + " 行: " + (e.getCause() != null ? e.getCause().getMessage() : e.getMessage()));
        }
    }

    private Object jsonNodeToValue(JsonNode node) {
        if (node.isNull()) {
            return null;
        }
        if (node.isBoolean()) {
            return node.asBoolean();
        }
        if (node.isNumber()) {
            return node.numberValue();
        }
        if (node.isTextual()) {
            return node.asText();
        }
        return node.toString();
    }

    private Map<String, MetaColumn> buildHeaderMapping(List<MetaColumn> columns, List<String> headers) {
        Map<String, MetaColumn> byCode = new HashMap<>();
        Map<String, MetaColumn> byName = new HashMap<>();
        for (MetaColumn column : columns) {
            byCode.put(normalizeHeader(column.getColumnCode()), column);
            if (column.getColumnName() != null) {
                byName.put(normalizeHeader(column.getColumnName()), column);
            }
        }

        Map<String, MetaColumn> result = new HashMap<>();
        for (String header : headers) {
            String key = normalizeHeader(header);
            MetaColumn column = byCode.get(key);
            if (column == null) {
                column = byName.get(key);
            }
            if (column != null) {
                result.put(key, column);
            }
        }
        return result;
    }

    private String normalizeHeader(String header) {
        return header == null ? "" : header.trim().toLowerCase(Locale.ROOT);
    }

    private static class ImportContext {
        final MetaTable table;
        final List<MetaColumn> columns;
        final Long currentUid;
        int total;
        int success;
        final List<String> errors = new ArrayList<>();

        ImportContext(MetaTable table, List<MetaColumn> columns, Long currentUid) {
            this.table = table;
            this.columns = columns;
            this.currentUid = currentUid;
        }
    }
}
