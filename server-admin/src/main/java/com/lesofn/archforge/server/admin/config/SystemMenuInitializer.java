package com.lesofn.archforge.server.admin.config;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * 幂等初始化系统管理菜单：字典配置。
 * 主要用于开发环境（Flyway 关闭）以及动态新增菜单场景。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SystemMenuInitializer implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        try {
            String check = "SELECT COUNT(*) FROM sys_menu WHERE menu_name = '字典配置' AND deleted = 0";
            Integer count = jdbcTemplate.queryForObject(check, Integer.class);
            if (count != null && count > 0) {
                return;
            }

            LocalDateTime now = LocalDateTime.now();
            Timestamp ts = Timestamp.valueOf(now);

            List<Map<String, Object>> parentRows = jdbcTemplate.queryForList(
                    "SELECT menu_id FROM sys_menu WHERE menu_name = '系统管理' AND deleted = 0 LIMIT 1");
            if (parentRows.isEmpty()) {
                log.warn("未找到系统管理菜单，跳过字典菜单初始化");
                return;
            }
            Long parentId = ((Number) parentRows.get(0).get("menu_id")).longValue();

            String insertType = """
                    INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info,
                                          status, remark, creator_id, create_time, updater_id, update_time, deleted)
                    VALUES (?, '字典配置', 1, 'SystemDict', ?, '/system/dict/index', 0, 'system:dict:list',
                            '{"title":"字典配置","icon":"ep:collection","showParent":true}', 1, '字典配置菜单', 1, ?, 1, ?, 0)
                    RETURNING menu_id
                    """;
            Long dictMenuId = jdbcTemplate.queryForObject(insertType, Long.class, 88L, parentId, ts, ts);

            String insertItem = """
                    INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info,
                                          status, remark, creator_id, create_time, updater_id, update_time, deleted)
                    VALUES (?, ?, 0, ' ', ?, '', 1, ?, ?, 1, '', 1, ?, 1, ?, 0)
                    """;
            List<String[]> buttons = List.of(
                    new String[] {
                            "字典查询", "89", "system:dict:query", "{\"title\":\"字典查询\"}"
                    },
                    new String[] {
                            "字典新增", "90", "system:dict:add", "{\"title\":\"字典新增\"}"
                    },
                    new String[] {
                            "字典修改", "91", "system:dict:edit", "{\"title\":\"字典修改\"}"
                    },
                    new String[] {
                            "字典删除", "92", "system:dict:remove", "{\"title\":\"字典删除\"}"
                    });
            for (String[] btn : buttons) {
                jdbcTemplate.update(insertItem, Long.parseLong(btn[1]), btn[0], dictMenuId, btn[2], btn[3], ts, ts);
            }
            log.info("字典配置菜单初始化完成");
        } catch (Exception e) {
            log.warn("字典配置菜单初始化失败（可忽略）: {}", e.getMessage(), e);
        }
    }
}
