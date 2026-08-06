package com.lesofn.archforge.meta.table.internal.service;

import static com.lesofn.archforge.meta.table.internal.exception.MetaTableErrorCode.META_TABLE_DATA_NOT_EXISTS;

import com.lesofn.archforge.meta.table.api.domain.MetaColumn;
import com.lesofn.archforge.meta.table.internal.exception.MetaTableErrorCode;
import com.lesofn.archforge.meta.table.api.domain.MetaTable;
import com.lesofn.archforge.meta.table.api.dto.ImportResult;
import com.lesofn.archforge.meta.table.api.dto.MetaPageResult;
import com.lesofn.archforge.meta.table.api.enums.MetaDataFormat;
import com.lesofn.archforge.meta.table.api.service.MetaTableAdminService;
import com.lesofn.archforge.meta.table.api.service.MetaTableCrudService;
import com.lesofn.archforge.meta.table.internal.ddl.SqlIdentifier;
import com.lesofn.archforge.meta.table.internal.exception.MetaTableException;
import com.lesofn.archforge.meta.table.internal.validator.MetaTableValidator;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Array;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.postgresql.util.PGobject;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 元表格行数据通用 CRUD 服务实现。
 */
@Service
@RequiredArgsConstructor
public class MetaTableCrudServiceImpl implements MetaTableCrudService {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final MetaTableAdminService metaTableAdminService;
    private final MetaTableValidator validator;
    private final MetaTableDataInserter inserter;
    private final MetaTableDataExporter exporter;
    private final MetaTableDataImporter importer;

    @Override
    @Transactional("metaTableTransactionManager")
    public Long insert(Long tableId, Map<String, Object> row, Long currentUid) {
        MetaTable table = metaTableAdminService.findById(tableId);
        List<MetaColumn> columns = metaTableAdminService.findColumns(tableId);
        validator.validateValues(row, columns, true);
        return inserter.insert(table, columns, row, currentUid);
    }

    @Override
    @Transactional("metaTableTransactionManager")
    public Boolean update(Long tableId, Long dataId, Map<String, Object> row, Long currentUid) {
        MetaTable table = metaTableAdminService.findById(tableId);
        List<MetaColumn> columns = metaTableAdminService.findColumns(tableId);
        validator.validateValues(row, columns, false);

        String physicalName = SqlIdentifier.quote(table.physicalTableName());
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("id", dataId);
        params.addValue("updaterId", currentUid);
        params.addValue("updateTime", java.time.LocalDateTime.now());

        List<String> sets = new ArrayList<>();
        sets.add(SqlIdentifier.quote("updater_id") + " = :updaterId");
        sets.add(SqlIdentifier.quote("update_time") + " = :updateTime");

        appendUpdateColumns(columns, row, sets, params);

        String sql = String.format(
                "UPDATE %s SET %s WHERE id = :id AND deleted = 0",
                physicalName,
                String.join(", ", sets));

        int rows = jdbcTemplate.update(sql, params);
        return rows > 0;
    }

    @Override
    @Transactional("metaTableTransactionManager")
    public Boolean softDelete(Long tableId, Long dataId, Long currentUid) {
        MetaTable table = metaTableAdminService.findById(tableId);
        String physicalName = SqlIdentifier.quote(table.physicalTableName());
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("id", dataId);
        params.addValue("deleted", 1);
        params.addValue("updaterId", currentUid);
        params.addValue("updateTime", java.time.LocalDateTime.now());

        String sql = String.format(
                "UPDATE %s SET deleted = :deleted, updater_id = :updaterId, update_time = :updateTime " +
                        "WHERE id = :id AND deleted = 0",
                physicalName);

        int rows = jdbcTemplate.update(sql, params);
        return rows > 0;
    }

    @Override
    public MetaPageResult<Map<String, Object>> list(
            Long tableId, Map<String, Object> filters, int currentPage, int pageSize) {
        MetaTable table = metaTableAdminService.findById(tableId);
        List<MetaColumn> columns = metaTableAdminService.findColumns(tableId);

        String physicalName = SqlIdentifier.quote(table.physicalTableName());
        String mainAlias = "main";
        List<String> selectColumns = ReferenceDisplayBuilder.buildSelectColumns(columns, mainAlias);
        List<String> joins = ReferenceDisplayBuilder.buildJoins(columns, mainAlias);

        MapSqlParameterSource params = new MapSqlParameterSource();
        String whereClause = buildWhereClause(columns, filters, params, mainAlias);

        String fromClause = " FROM " + physicalName + " " + mainAlias + " " + String.join(" ", joins);
        String countSql = "SELECT COUNT(*)" + fromClause + " WHERE " + mainAlias + ".deleted = 0" + whereClause;
        Long total = jdbcTemplate.queryForObject(countSql, params, Long.class);
        long finalTotal = total == null ? 0L : total;

        int offset = (currentPage - 1) * pageSize;
        params.addValue("limit", pageSize);
        params.addValue("offset", offset);

        String querySql = "SELECT " + String.join(", ", selectColumns) + fromClause + " WHERE " + mainAlias +
                ".deleted = 0" + whereClause + " ORDER BY " + mainAlias + ".id DESC LIMIT :limit OFFSET :offset";

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(querySql, params);
        List<Map<String, Object>> converted = rows.stream().map(this::convertRow).toList();

        return MetaPageResult.of(converted, finalTotal, pageSize, currentPage);
    }

    @Override
    public void export(Long tableId, MetaDataFormat format, OutputStream out) {
        exporter.export(tableId, format, out);
    }

    @Override
    public ImportResult importData(Long tableId, MetaDataFormat format, InputStream in, Long currentUid) {
        return importer.importData(tableId, format, in, currentUid);
    }

    private String buildWhereClause(List<MetaColumn> columns, Map<String, Object> filters, MapSqlParameterSource params,
            String mainAlias) {
        if (filters == null || filters.isEmpty()) {
            return "";
        }
        Map<String, MetaColumn> columnMap = columns.stream()
                .collect(Collectors.toMap(MetaColumn::getColumnCode, c -> c, (a, b) -> a));
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Object> entry : filters.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value == null || isEmptyFilterValue(value)) {
                continue;
            }
            FilterPath filterPath = parseFilterKey(columnMap, key);
            if (filterPath == null || !filterPath.column().isSearchableColumn()) {
                continue;
            }
            MetaColumn column = filterPath.column();
            String paramName = "filter_" + key.replace('.', '_').replace(' ', '_');
            if (filterPath.jsonPath() != null) {
                params.addValue(paramName, value.toString());
                sb.append(" AND ")
                        .append(buildJsonPathExpression(column, filterPath.jsonPath(), paramName, value, mainAlias));
            } else if (isRangeSearch(column, value)) {
                appendRangeCondition(sb, column, key, value, paramName, params, mainAlias);
            } else if (isLikeSearch(column)) {
                params.addValue(paramName, "%" + value + "%");
                sb.append(" AND ").append(mainAlias).append(".").append(SqlIdentifier.quote(key)).append("::text LIKE :")
                        .append(paramName);
            } else {
                params.addValue(paramName, validator.convertValue(column, value));
                sb.append(" AND ").append(mainAlias).append(".").append(SqlIdentifier.quote(key)).append(" = :")
                        .append(paramName);
            }
        }
        return sb.toString();
    }

    private FilterPath parseFilterKey(Map<String, MetaColumn> columnMap, String key) {
        int dot = key.indexOf('.');
        if (dot > 0) {
            String columnCode = key.substring(0, dot);
            MetaColumn column = columnMap.get(columnCode);
            if (column != null) {
                return new FilterPath(column, key.substring(dot + 1));
            }
        }
        MetaColumn column = columnMap.get(key);
        return column == null ? null : new FilterPath(column, null);
    }

    private boolean isLikeSearch(MetaColumn column) {
        return "LIKE".equalsIgnoreCase(resolveSearchType(column));
    }

    private boolean isRangeSearch(MetaColumn column, Object value) {
        if (!"RANGE".equalsIgnoreCase(resolveSearchType(column))) {
            return false;
        }
        return value instanceof Map<?, ?> map && (map.containsKey("start") || map.containsKey("end")) ||
                value instanceof List<?> list && list.size() == 2;
    }

    private String resolveSearchType(MetaColumn column) {
        if (column.getSearchType() != null && !column.getSearchType().isEmpty()) {
            return column.getSearchType();
        }
        return switch (column.getDataType()) {
            case STRING, TEXT, ENUM, JSON, GEO -> "LIKE";
            default -> "EXACT";
        };
    }

    private void appendRangeCondition(StringBuilder sb, MetaColumn column, String key, Object value, String paramName,
            MapSqlParameterSource params, String mainAlias) {
        Object start = null;
        Object end = null;
        if (value instanceof Map<?, ?> map) {
            start = map.get("start");
            end = map.get("end");
        } else if (value instanceof List<?> list) {
            start = list.get(0);
            end = list.get(1);
        }
        String quoted = mainAlias + "." + SqlIdentifier.quote(key);
        if (start != null && !isEmptyFilterValue(start)) {
            params.addValue(paramName + "_start", validator.convertValue(column, start));
            sb.append(" AND ").append(quoted).append(" >= :").append(paramName).append("_start");
        }
        if (end != null && !isEmptyFilterValue(end)) {
            params.addValue(paramName + "_end", validator.convertValue(column, end));
            sb.append(" AND ").append(quoted).append(" <= :").append(paramName).append("_end");
        }
    }

    private boolean isEmptyFilterValue(Object value) {
        if (value == null) {
            return true;
        }
        if (value instanceof String string) {
            return string.isEmpty();
        }
        if (value instanceof Collection<?> collection) {
            return collection.isEmpty();
        }
        return false;
    }

    private String buildJsonPathExpression(MetaColumn column, String jsonPath, String paramName, Object value,
            String mainAlias) {
        String quoted = mainAlias + "." + SqlIdentifier.quote(column.getColumnCode());
        String[] parts = jsonPath.split("\\.");
        if (parts.length == 1) {
            return quoted + " ->> '" + parts[0] + "' LIKE :" + paramName;
        }
        return quoted + " #>> ARRAY[" + java.util.Arrays.stream(parts)
                .map(p -> "'" + p.replace("'", "''") + "'")
                .collect(Collectors.joining(", ")) + "] LIKE :" + paramName;
    }

    private record FilterPath(MetaColumn column, String jsonPath) {
    }

    private Map<String, Object> convertRow(Map<String, Object> row) {
        for (Map.Entry<String, Object> entry : row.entrySet()) {
            Object value = entry.getValue();
            if (value == null) {
                continue;
            }
            if (value instanceof Timestamp timestamp) {
                entry.setValue(timestamp.toLocalDateTime());
            } else if (value instanceof Date date) {
                entry.setValue(date.toLocalDate());
            } else if (value instanceof OffsetDateTime offsetDateTime) {
                entry.setValue(offsetDateTime.toString());
            } else if (value instanceof UUID uuid) {
                entry.setValue(uuid.toString());
            } else if (value instanceof Array sqlArray) {
                entry.setValue(convertSqlArray(sqlArray));
            } else if (value instanceof PGobject pgObject) {
                entry.setValue(pgObject.getValue());
            }
        }
        return row;
    }

    private List<Object> convertSqlArray(Array sqlArray) {
        try {
            Object array = sqlArray.getArray();
            if (array instanceof Object[]) {
                return Arrays.asList((Object[]) array);
            }
            return Collections.singletonList(array.toString());
        } catch (SQLException e) {
            throw new MetaTableException(MetaTableErrorCode.META_COLUMN_VALUE_INVALID, "读取数组字段失败");
        }
    }

    private void appendUpdateColumns(
            List<MetaColumn> columns,
            Map<String, Object> row,
            List<String> sets,
            MapSqlParameterSource params) {
        for (MetaColumn column : columns) {
            if (row.containsKey(column.getColumnCode())) {
                Object value = row.get(column.getColumnCode());
                String quoted = SqlIdentifier.quote(column.getColumnCode());
                if (value == null) {
                    sets.add(quoted + " = NULL");
                } else {
                    sets.add(quoted + " = :" + column.getColumnCode());
                    params.addValue(column.getColumnCode(), validator.convertValue(column, value));
                }
            }
        }
    }
}
