package com.lesofn.archforge.meta.table.internal.service;

import com.lesofn.archforge.meta.table.api.domain.MetaColumn;
import com.lesofn.archforge.meta.table.api.domain.MetaTable;
import com.lesofn.archforge.meta.table.internal.ddl.SqlIdentifier;
import com.lesofn.archforge.meta.table.internal.validator.MetaTableValidator;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

/**
 * 元表格数据行插入器。
 */
@Component
@RequiredArgsConstructor
public class MetaTableDataInserter {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final MetaTableValidator validator;

    public Long insert(MetaTable table, List<MetaColumn> columns, Map<String, Object> row, Long currentUid) {
        String physicalName = SqlIdentifier.quote(table.physicalTableName());
        MapSqlParameterSource params = new MapSqlParameterSource();
        LocalDateTime now = LocalDateTime.now();
        params.addValue("creatorId", currentUid);
        params.addValue("createTime", now);
        params.addValue("updaterId", currentUid);
        params.addValue("updateTime", now);
        params.addValue("deleted", 0);

        List<String> fields = new ArrayList<>();
        fields.add(SqlIdentifier.quote("creator_id"));
        fields.add(SqlIdentifier.quote("create_time"));
        fields.add(SqlIdentifier.quote("updater_id"));
        fields.add(SqlIdentifier.quote("update_time"));
        fields.add(SqlIdentifier.quote("deleted"));

        List<String> placeholders = new ArrayList<>();
        placeholders.add(":creatorId");
        placeholders.add(":createTime");
        placeholders.add(":updaterId");
        placeholders.add(":updateTime");
        placeholders.add(":deleted");

        for (MetaColumn column : columns) {
            Object value = row.get(column.getColumnCode());
            if (value == null) {
                continue;
            }
            fields.add(SqlIdentifier.quote(column.getColumnCode()));
            placeholders.add(":" + column.getColumnCode());
            params.addValue(column.getColumnCode(), validator.convertValue(column, value));
        }

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
}
