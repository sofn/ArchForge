package com.lesofn.archforge.meta.table.internal.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.lesofn.archforge.meta.table.api.domain.MetaColumn;
import com.lesofn.archforge.meta.table.api.domain.MetaColumnType;
import com.lesofn.archforge.meta.table.api.domain.MetaTable;
import com.lesofn.archforge.meta.table.api.dto.ImportResponse;
import com.lesofn.archforge.meta.table.api.enums.MetaDataFormat;
import com.lesofn.archforge.meta.table.api.errors.MetaTableErrorCode;
import com.lesofn.archforge.meta.table.api.errors.MetaTableException;
import com.lesofn.archforge.meta.table.api.service.MetaTableAdminService;
import com.lesofn.archforge.meta.table.internal.config.MetaTableTransferProperties;
import com.lesofn.archforge.meta.table.internal.ddl.SqlIdentifier;
import com.lesofn.archforge.meta.table.internal.validator.MetaTableValidator;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * 元表格数据导入器。
 */
@Component
@RequiredArgsConstructor
public class MetaTableDataImporter {

    private static final int READ_BUFFER_SIZE = 8192;

    private final MetaTableAdminService metaTableAdminService;
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final MetaTableValidator validator;
    private final MetaTableTransferProperties transferProperties;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ImportResponse importData(Long tableId, MetaDataFormat format, InputStream in, Long currentUid) {
        byte[] payload = readPayload(in);
        if (format == MetaDataFormat.CSV) {
            return importCsv(tableId, payload, currentUid);
        }
        if (format == MetaDataFormat.JSON) {
            return importJson(tableId, payload, currentUid);
        }
        throw new MetaTableException(MetaTableErrorCode.META_COLUMN_VALUE_INVALID, "不支持的导入格式: " + format);
    }

    private byte[] readPayload(InputStream in) {
        long maxBytes = transferProperties.getMaxFileBytes();
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] chunk = new byte[READ_BUFFER_SIZE];
        try (in) {
            int read;
            while ((read = in.read(chunk)) != -1) {
                if (buffer.size() + read > maxBytes) {
                    throw new MetaTableException("导入文件超过大小上限 " + maxBytes + " 字节");
                }
                buffer.write(chunk, 0, read);
            }
        } catch (IOException e) {
            throw new MetaTableException(MetaTableErrorCode.META_COLUMN_VALUE_INVALID, "读取导入文件失败: " + e.getMessage());
        }
        return buffer.toByteArray();
    }

    private ImportResponse importCsv(Long tableId, byte[] payload, Long currentUid) {
        ImportContext ctx = newContext(tableId, currentUid);
        try (InputStreamReader reader = new InputStreamReader(new ByteArrayInputStream(payload), StandardCharsets.UTF_8);
                CSVParser parser = new CSVParser(reader, CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true)
                        .build())) {
            Map<String, MetaColumn> headerToColumn = buildHeaderMapping(ctx.columns, parser.getHeaderNames());
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
        return finish(ctx);
    }

    private ImportResponse importJson(Long tableId, byte[] payload, Long currentUid) {
        ImportContext ctx = newContext(tableId, currentUid);
        try {
            JsonNode root = objectMapper.readTree(new ByteArrayInputStream(payload));
            if (!root.isArray()) {
                throw new MetaTableException(MetaTableErrorCode.META_COLUMN_VALUE_INVALID, "JSON 导入数据必须是数组");
            }
            int rowNum = 0;
            for (JsonNode node : root) {
                rowNum++;
                if (!node.isObject()) {
                    recordError(ctx, rowNum, "不是对象");
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
        return finish(ctx);
    }

    private ImportContext newContext(Long tableId, Long currentUid) {
        MetaTable table = metaTableAdminService.findById(tableId);
        List<MetaColumn> columns = metaTableAdminService.findColumns(tableId);
        return new ImportContext(table, columns, currentUid, transferProperties);
    }

    private void processRow(ImportContext ctx, Map<String, Object> row, int rowNum) {
        ctx.total++;
        if (ctx.total > ctx.maxRows) {
            throw new MetaTableException("导入行数超过上限 " + ctx.maxRows + "，已处理 " + ctx.maxRows + " 行，请拆分文件后重试");
        }
        try {
            validator.validateValues(row, ctx.columns, true);
            checkReferences(ctx, row);
            ctx.pending.add(row);
        } catch (MetaTableException e) {
            recordError(ctx, rowNum, e.getMessage());
        } catch (Exception e) {
            recordError(ctx, rowNum, e.getCause() != null ? e.getCause().getMessage() : e.getMessage());
        }
    }

    private void checkReferences(ImportContext ctx, Map<String, Object> row) {
        for (MetaColumn column : ctx.referenceColumns) {
            Object value = row.get(column.getColumnCode());
            if (value == null) {
                continue;
            }
            long id = Long.parseLong(value.toString());
            String cacheKey = column.getColumnCode() + ':' + id;
            Boolean known = ctx.referenceCache.get(cacheKey);
            if (known == null) {
                known = referenceExists(column, id);
                ctx.referenceCache.put(cacheKey, known);
            }
            if (!Boolean.TRUE.equals(known)) {
                throw new MetaTableException(column.getColumnName() + " 引用的记录不存在: " + id);
            }
        }
    }

    private boolean referenceExists(MetaColumn column, long id) {
        if (column.getReferenceTable() == null || column.getReferenceTable().isEmpty() || column.getReferenceColumn() == null ||
                column.getReferenceColumn().isEmpty()) {
            throw new MetaTableException(column.getColumnName() + " 关联配置不完整");
        }
        String sql = "SELECT COUNT(*) FROM " + SqlIdentifier.quote(column.getReferenceTable()) + " WHERE " + SqlIdentifier
                .quote(column.getReferenceColumn()) + " = :id";
        Integer count = jdbcTemplate.queryForObject(sql, Map.of("id", id), Integer.class);
        return count != null && count > 0;
    }

    private void recordError(ImportContext ctx, int rowNum, String message) {
        ctx.failed++;
        if (ctx.errors.size() < ctx.maxErrors) {
            ctx.errors.add("第 " + rowNum + " 行: " + message);
        } else {
            ctx.errorTruncated = true;
        }
    }

    private ImportResponse finish(ImportContext ctx) {
        flushPending(ctx);
        return ImportResponse.of(ctx.total, ctx.success, ctx.failed, ctx.errors, ctx.errorTruncated);
    }

    private void flushPending(ImportContext ctx) {
        int size = ctx.pending.size();
        int start = 0;
        while (start < size) {
            Set<String> signature = ctx.pending.get(start).keySet();
            int end = start + 1;
            while (end < size && ctx.pending.get(end).keySet().equals(signature)) {
                end++;
            }
            insertRange(ctx, start, end, signature);
            start = end;
        }
        ctx.success = size;
    }

    private void insertRange(ImportContext ctx, int from, int to, Set<String> codes) {
        String sql = buildInsertSql(ctx.table, ctx.columns, codes);
        for (int start = from; start < to; start += ctx.batchSize) {
            int end = Math.min(start + ctx.batchSize, to);
            MapSqlParameterSource[] batch = new MapSqlParameterSource[end - start];
            for (int index = start; index < end; index++) {
                batch[index - start] = buildParams(ctx, ctx.pending.get(index), codes);
            }
            jdbcTemplate.batchUpdate(sql, batch);
        }
    }

    private String buildInsertSql(MetaTable table, List<MetaColumn> columns, Set<String> codes) {
        List<String> fields = new ArrayList<>(List.of(
                SqlIdentifier.quote("creator_id"),
                SqlIdentifier.quote("create_time"),
                SqlIdentifier.quote("updater_id"),
                SqlIdentifier.quote("update_time"),
                SqlIdentifier.quote("deleted")));
        List<String> placeholders = new ArrayList<>(List.of(
                ":creatorId", ":createTime", ":updaterId", ":updateTime", ":deleted"));
        for (MetaColumn column : columns) {
            if (codes.contains(column.getColumnCode())) {
                fields.add(SqlIdentifier.quote(column.getColumnCode()));
                placeholders.add(":" + column.getColumnCode());
            }
        }
        return String.format(
                "INSERT INTO %s (%s) VALUES (%s)",
                SqlIdentifier.quote(table.physicalTableName()),
                String.join(", ", fields),
                String.join(", ", placeholders));
    }

    private MapSqlParameterSource buildParams(ImportContext ctx, Map<String, Object> row, Set<String> codes) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("creatorId", ctx.currentUid);
        params.addValue("createTime", ctx.now);
        params.addValue("updaterId", ctx.currentUid);
        params.addValue("updateTime", ctx.now);
        params.addValue("deleted", 0);
        for (MetaColumn column : ctx.columns) {
            if (!codes.contains(column.getColumnCode())) {
                continue;
            }
            params.addValue(column.getColumnCode(), validator.convertValue(column, row.get(column.getColumnCode())));
        }
        return params;
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
        final List<MetaColumn> referenceColumns;
        final Long currentUid;
        final LocalDateTime now = LocalDateTime.now();
        final long maxRows;
        final int maxErrors;
        final int batchSize;
        int total;
        int success;
        int failed;
        boolean errorTruncated;
        final List<String> errors = new ArrayList<>();
        final List<Map<String, Object>> pending = new ArrayList<>();
        final Map<String, Boolean> referenceCache = new HashMap<>();

        ImportContext(MetaTable table, List<MetaColumn> columns, Long currentUid,
                MetaTableTransferProperties properties) {
            this.table = table;
            this.currentUid = currentUid;
            this.maxRows = properties.getMaxImportRows();
            this.maxErrors = properties.getMaxErrorList();
            this.batchSize = properties.getImportBatchSize();
            // findColumns 每次返回全新实体，就地改写仅影响本次导入；
            // REFERENCE 存在性由 checkReferences 缓存校验，校验阶段按 INTEGER 只做数值解析避免逐行 COUNT
            this.referenceColumns = columns.stream()
                    .filter(c -> c.getDataType() == MetaColumnType.REFERENCE)
                    .toList();
            for (MetaColumn column : columns) {
                if (column.getDataType() == MetaColumnType.REFERENCE) {
                    column.setDataType(MetaColumnType.INTEGER);
                }
            }
            this.columns = columns;
        }
    }
}
