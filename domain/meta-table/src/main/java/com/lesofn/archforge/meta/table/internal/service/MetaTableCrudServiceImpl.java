package com.lesofn.archforge.meta.table.internal.service;

import static com.lesofn.archforge.meta.table.internal.exception.MetaTableErrorCode.META_TABLE_DATA_NOT_EXISTS;

import com.lesofn.archforge.common.utils.excel.FastExcelUtil;
import com.lesofn.archforge.meta.table.api.domain.MetaColumn;
import com.lesofn.archforge.meta.table.api.domain.MetaTable;
import com.lesofn.archforge.meta.table.api.dto.MetaPageResult;
import com.lesofn.archforge.meta.table.api.service.MetaTableAdminService;
import com.lesofn.archforge.meta.table.api.service.MetaTableCrudService;
import com.lesofn.archforge.meta.table.internal.ddl.SqlIdentifier;
import com.lesofn.archforge.meta.table.internal.exception.MetaTableException;
import com.lesofn.archforge.meta.table.internal.validator.MetaTableValidator;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
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

    @Override
    @Transactional("metaTableTransactionManager")
    public Long insert(Long tableId, Map<String, Object> row, Long currentUid) {
        MetaTable table = metaTableAdminService.findById(tableId);
        List<MetaColumn> columns = metaTableAdminService.findColumns(tableId);
        validator.validateValues(row, columns, true);

        String physicalName = SqlIdentifier.quote(table.physicalTableName());
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("creatorId", currentUid);
        params.addValue("createTime", LocalDateTime.now());
        params.addValue("deleted", 0);

        List<String> fields = new ArrayList<>();
        fields.add(SqlIdentifier.quote("creator_id"));
        fields.add(SqlIdentifier.quote("create_time"));
        fields.add(SqlIdentifier.quote("deleted"));

        List<String> placeholders = new ArrayList<>();
        placeholders.add(":creatorId");
        placeholders.add(":createTime");
        placeholders.add(":deleted");

        appendValueColumns(columns, row, fields, placeholders, params);

        String sql = String.format(
                "INSERT INTO %s (%s) VALUES (%s)",
                physicalName,
                String.join(", ", fields),
                String.join(", ", placeholders));

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(sql, params, keyHolder, new String[] {
                "id"
        });
        Number key = keyHolder.getKey();
        return Objects.requireNonNull(key).longValue();
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
        params.addValue("updateTime", LocalDateTime.now());

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
        params.addValue("updateTime", LocalDateTime.now());

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
        List<String> quotedColumns = buildQuotedColumns(columns);

        MapSqlParameterSource params = new MapSqlParameterSource();
        String whereClause = buildWhereClause(columns, filters, params);

        String countSql = "SELECT COUNT(*) FROM " + physicalName + " WHERE deleted = 0" + whereClause;
        Long total = jdbcTemplate.queryForObject(countSql, params, Long.class);
        long finalTotal = total == null ? 0L : total;

        int offset = (currentPage - 1) * pageSize;
        params.addValue("limit", pageSize);
        params.addValue("offset", offset);

        String querySql = "SELECT " + String.join(", ", quotedColumns) + " FROM " + physicalName + " WHERE deleted = 0" +
                whereClause + " ORDER BY id DESC LIMIT :limit OFFSET :offset";

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(querySql, params);
        List<Map<String, Object>> converted = rows.stream().map(this::convertRow).toList();

        return MetaPageResult.of(converted, finalTotal, pageSize, currentPage);
    }

    @Override
    public void export(Long tableId, OutputStream out) {
        MetaTable table = metaTableAdminService.findById(tableId);
        List<MetaColumn> columns = metaTableAdminService.findColumns(tableId);
        List<MetaColumn> visibleColumns = columns.stream()
                .filter(MetaColumn::isListVisibleColumn)
                .toList();

        String physicalName = SqlIdentifier.quote(table.physicalTableName());
        List<String> quotedColumns = buildQuotedColumns(columns);
        String querySql = "SELECT " + String.join(", ", quotedColumns) + " FROM " + physicalName +
                " WHERE deleted = 0 ORDER BY id DESC";

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(querySql, Map.of());

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
            throw new RuntimeException("导出 Excel 失败", e);
        }
    }

    private List<String> buildQuotedColumns(List<MetaColumn> columns) {
        List<String> result = new ArrayList<>();
        result.add(SqlIdentifier.quote("id"));
        for (MetaColumn column : columns) {
            result.add(SqlIdentifier.quote(column.getColumnCode()));
        }
        result.add(SqlIdentifier.quote("creator_id"));
        result.add(SqlIdentifier.quote("create_time"));
        result.add(SqlIdentifier.quote("updater_id"));
        result.add(SqlIdentifier.quote("update_time"));
        result.add(SqlIdentifier.quote("deleted"));
        return result;
    }

    private String buildWhereClause(List<MetaColumn> columns, Map<String, Object> filters, MapSqlParameterSource params) {
        if (filters == null || filters.isEmpty()) {
            return "";
        }
        Map<String, MetaColumn> columnMap = columns.stream()
                .collect(Collectors.toMap(MetaColumn::getColumnCode, c -> c, (a, b) -> a));
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Object> entry : filters.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value == null || (value instanceof String s && s.isEmpty())) {
                continue;
            }
            MetaColumn column = columnMap.get(key);
            if (column == null || !column.isSearchableColumn()) {
                continue;
            }
            String paramName = "filter_" + key;
            params.addValue(paramName, value);
            String quoted = SqlIdentifier.quote(key);
            if (column.getDataType() == com.lesofn.archforge.meta.table.api.domain.MetaColumnType.STRING || column
                    .getDataType() == com.lesofn.archforge.meta.table.api.domain.MetaColumnType.TEXT || column
                            .getDataType() == com.lesofn.archforge.meta.table.api.domain.MetaColumnType.ENUM) {
                params.addValue(paramName, "%" + value + "%");
                sb.append(" AND ").append(quoted).append(" LIKE :").append(paramName);
            } else {
                sb.append(" AND ").append(quoted).append(" = :").append(paramName);
            }
        }
        return sb.toString();
    }

    private Map<String, Object> convertRow(Map<String, Object> row) {
        for (Map.Entry<String, Object> entry : row.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof Timestamp timestamp) {
                entry.setValue(timestamp.toLocalDateTime());
            } else if (value instanceof Date date) {
                entry.setValue(date.toLocalDate());
            }
        }
        return row;
    }

    private void appendValueColumns(
            List<MetaColumn> columns,
            Map<String, Object> row,
            List<String> fields,
            List<String> placeholders,
            MapSqlParameterSource params) {
        for (MetaColumn column : columns) {
            Object value = row.get(column.getColumnCode());
            if (value != null) {
                fields.add(SqlIdentifier.quote(column.getColumnCode()));
                placeholders.add(":" + column.getColumnCode());
                params.addValue(column.getColumnCode(), validator.convertValue(column, value));
            }
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
