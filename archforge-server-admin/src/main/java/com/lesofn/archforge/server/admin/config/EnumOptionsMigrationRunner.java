package com.lesofn.archforge.server.admin.config;

import com.lesofn.archforge.common.utils.jackson.JsonUtil;
import com.lesofn.archforge.meta.table.api.domain.OptionItem;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;

/**
 * 开发/非 Flyway 环境下，将历史 MetaColumn.options 迁移为字典配置。
 * 幂等：只处理 dict_code 为空的 ENUM 列。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EnumOptionsMigrationRunner implements ApplicationRunner {

    @Qualifier("metaTableJdbcTemplate")
    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        try {
            String select = """
                    SELECT c.id AS column_id, c.column_code, c.column_name, c.options, t.table_code
                    FROM sys_meta_table_column c
                    JOIN sys_meta_table t ON c.table_id = t.id
                    WHERE c.data_type = 'ENUM'
                      AND c.options IS NOT NULL
                      AND c.options <> ''
                      AND c.dict_code IS NULL
                    """;
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(select, Map.of());
            if (rows.isEmpty()) {
                return;
            }
            log.info("开始迁移 {} 个历史 ENUM 字段的内联选项", rows.size());
            for (Map<String, Object> row : rows) {
                migrateRow(row);
            }
            log.info("ENUM 选项迁移完成");
        } catch (Exception e) {
            log.warn("ENUM 选项迁移失败（可忽略，将在下次启动重试）: {}", e.getMessage(), e);
        }
    }

    private void migrateRow(Map<String, Object> row) {
        Long columnId = ((Number) row.get("column_id")).longValue();
        String tableCode = (String) row.get("table_code");
        String columnCode = (String) row.get("column_code");
        String columnName = (String) row.get("column_name");
        String optionsJson = (String) row.get("options");

        String dictCode = "meta_" + tableCode + "_" + columnCode;
        if (dictCode.length() > 64) {
            dictCode = dictCode.substring(0, 64);
        }

        LocalDateTime now = LocalDateTime.now();
        String insertType = """
                INSERT INTO sys_dict_type (dict_code, dict_name, description, status, sort, deleted, create_time, update_time)
                VALUES (:dictCode, :dictName, :description, :status, :sort, :deleted, :createTime, :updateTime)
                RETURNING dict_type_id
                """;
        MapSqlParameterSource typeParams = new MapSqlParameterSource()
                .addValue("dictCode", dictCode)
                .addValue("dictName", columnName)
                .addValue("description", (String) null)
                .addValue("status", 1)
                .addValue("sort", 0)
                .addValue("deleted", 0)
                .addValue("createTime", Timestamp.valueOf(now))
                .addValue("updateTime", Timestamp.valueOf(now));
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(insertType, typeParams, keyHolder, new String[] {
                "dict_type_id"
        });
        Number dictTypeId = keyHolder.getKey();
        if (dictTypeId == null) {
            log.warn("字典类型插入失败，跳过 columnId={}", columnId);
            return;
        }

        List<OptionItem> items = parseOptions(optionsJson);
        String insertItem = """
                INSERT INTO sys_dict_item (dict_type_id, item_code, item_label, sort, status, deleted, create_time, update_time)
                VALUES (:dictTypeId, :itemCode, :itemLabel, :sort, :status, :deleted, :createTime, :updateTime)
                """;
        for (OptionItem item : items) {
            MapSqlParameterSource itemParams = new MapSqlParameterSource()
                    .addValue("dictTypeId", dictTypeId.longValue())
                    .addValue("itemCode", item.getValue() == null ? "" : item.getValue().toString())
                    .addValue("itemLabel", item.getLabel())
                    .addValue("sort", 0)
                    .addValue("status", 1)
                    .addValue("deleted", 0)
                    .addValue("createTime", Timestamp.valueOf(now))
                    .addValue("updateTime", Timestamp.valueOf(now));
            jdbcTemplate.update(insertItem, itemParams);
        }

        String updateColumn = """
                UPDATE sys_meta_table_column
                SET dict_code = :dictCode, options = NULL
                WHERE id = :id
                """;
        jdbcTemplate.update(updateColumn, Map.of("dictCode", dictCode, "id", columnId));
    }

    private List<OptionItem> parseOptions(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return JsonUtil.fromList(json, OptionItem.class);
        } catch (Exception e) {
            log.warn("解析 ENUM options 失败: {}", json, e);
            return List.of();
        }
    }
}
