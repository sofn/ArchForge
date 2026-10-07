package com.lesofn.archforge.server.admin.metatable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.lesofn.archforge.common.persistence.testsupport.AbstractIntegrationTest;
import com.lesofn.archforge.meta.table.api.dao.MetaTableRepository;
import com.lesofn.archforge.meta.table.api.domain.MetaColumn;
import com.lesofn.archforge.meta.table.api.domain.MetaColumnType;
import com.lesofn.archforge.meta.table.api.domain.MetaTable;
import com.lesofn.archforge.meta.table.api.errors.MetaTableErrorCode;
import com.lesofn.archforge.meta.table.api.errors.MetaTableException;
import com.lesofn.archforge.meta.table.api.service.MetaTableAdminService;
import com.lesofn.archforge.server.admin.Application;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

/**
 * 设计器是个能执行 DDL 的入口：默认值、显示表达式、表前缀三个字段都会流入 SQL。本类用真实 PostgreSQL
 * 证明恶意输入既不会被执行，也不会留下半截状态（登记行 / 物理表）。
 *
 * <p>
 * 注入载荷只指向测试自建的牺牲表 {@code probe_victim}，不碰任何真实系统表。
 */
@SpringBootTest(classes = {
        Application.class
}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Tag("slow")
class MetaTableInjectionIntegrationTest extends AbstractIntegrationTest {

    private static final String VICTIM = "probe_victim";

    @Qualifier("metaTableJdbcTemplate")
    @Autowired
    private NamedParameterJdbcTemplate jdbc;

    @Autowired
    private MetaTableAdminService adminService;

    @Autowired
    private MetaTableRepository tableRepository;

    private final List<Long> legacyRows = new ArrayList<>();

    @BeforeEach
    void createVictim() {
        for (String name : List.of(VICTIM, "sys_" + VICTIM)) {
            jdbc.getJdbcOperations().execute(
                    "CREATE TABLE IF NOT EXISTS " + name + " (id BIGINT PRIMARY KEY, deleted INT DEFAULT 0 NOT NULL)");
        }
    }

    @AfterEach
    void dropVictim() {
        for (Long id : legacyRows) {
            tableRepository.deleteById(id);
        }
        legacyRows.clear();
        jdbc.getJdbcOperations().execute("DROP TABLE IF EXISTS " + VICTIM);
        jdbc.getJdbcOperations().execute("DROP TABLE IF EXISTS sys_" + VICTIM);
    }

    @Test
    void defaultValueCannotInjectStatements() {
        MetaColumn amount = column("amount", MetaColumnType.INTEGER);
        amount.setDefaultValue("1); DROP TABLE " + VICTIM + "; --");

        MetaTableException e = assertThrows(MetaTableException.class,
                () -> adminService.create(table("inj_default", "meta_"), List.of(amount)));

        assertEquals(MetaTableErrorCode.META_COLUMN_VALUE_INVALID.getCode(), e.getErrorInfo().getCode());
        assertTrue(exists(VICTIM), "stacked DROP TABLE must not have run");
        assertNoTraceOf("inj_default");
    }

    @Test
    void defaultValueCannotAddConstraints() {
        // the exact payload from the 2026-10-07 metatable-flow report (landed a CHECK constraint)
        MetaColumn amount = column("amount", MetaColumnType.DECIMAL);
        amount.setDefaultValue("0, CONSTRAINT chk_probe_injected CHECK (amount >= 0)");

        assertThrows(MetaTableException.class, () -> adminService.create(table("inj_check", "meta_"), List.of(amount)));

        assertNoTraceOf("inj_check");
    }

    @Test
    void temporalDefaultCannotBreakOutOfItsQuotes() {
        MetaColumn since = column("since", MetaColumnType.DATE);
        since.setDefaultValue("2020-01-01'); DROP TABLE " + VICTIM + "; --");

        // quoted branch: either rejected or stored as an inert string literal — never executed
        try {
            adminService.create(table("inj_date", "meta_"), List.of(since));
        } catch (MetaTableException | org.springframework.dao.DataAccessException expected) {
            // PostgreSQL refuses the malformed date literal — fine, the point is the victim survives
        }

        assertTrue(exists(VICTIM), "stacked DROP TABLE must not have run");
    }

    @Test
    void displayExpressionCannotCarryASubquery() {
        MetaColumn owner = column("owner_ref", MetaColumnType.REFERENCE);
        owner.setReferenceTable("meta_owner");
        owner.setReferenceColumn("id");
        owner.setDisplayExpression("ref.id ||\n(SELECT password FROM sys_user LIMIT 1)");

        MetaTableException e = assertThrows(MetaTableException.class,
                () -> adminService.create(table("inj_expr", "meta_"), List.of(owner)));

        assertEquals(MetaTableErrorCode.META_COLUMN_VALUE_INVALID.getCode(), e.getErrorInfo().getCode());
        assertNoTraceOf("inj_expr");
    }

    @Test
    void platformPrefixCannotAdoptASystemTable() {
        MetaTableException e = assertThrows(MetaTableException.class,
                () -> adminService.create(table("menu", "sys_"), List.of(column("name", MetaColumnType.STRING))));

        assertEquals(MetaTableErrorCode.META_TABLE_PREFIX_INVALID.getCode(), e.getErrorInfo().getCode());
        assertTrue(exists("sys_menu"));
        assertFalse(tableRepository.findByTableCodeAndDeletedFalse("menu").isPresent());
    }

    @Test
    void createRefusesToAdoptAnExistingPhysicalTable() {
        // CREATE TABLE IF NOT EXISTS would silently "adopt" cms_article; adoption has its own import flow
        MetaTableException e = assertThrows(MetaTableException.class,
                () -> adminService.create(table("article", "cms_"), List.of(column("title", MetaColumnType.STRING))));

        assertEquals(MetaTableErrorCode.META_PHYSICAL_TABLE_EXISTS.getCode(), e.getErrorInfo().getCode());
        assertTrue(exists("cms_article"));
        assertFalse(tableRepository.findByTableCodeAndDeletedFalse("article").isPresent(), "registry row must roll back");
    }

    @Test
    void deleteNeverDropsAPlatformPhysicalTable() {
        // a legacy registry row written before the prefix check existed
        MetaTable legacy = table(VICTIM, "sys_");
        legacy.setStatus(1);
        legacy.setSchemaVersion(1);
        Long id = Objects.requireNonNull(tableRepository.save(legacy).getId());
        legacyRows.add(id);

        assertThrows(MetaTableException.class, () -> adminService.delete(id, true));

        assertTrue(exists("sys_" + VICTIM), "physical table behind a platform-prefixed row must survive delete");
    }

    @Test
    void ordinaryTableStillRoundTrips() {
        MetaColumn amount = column("amount", MetaColumnType.DECIMAL);
        amount.setDefaultValue("0.50");
        MetaColumn note = column("note", MetaColumnType.STRING);
        note.setDefaultValue("it's fine; -- really");

        Long id = adminService.create(table("inj_ok", "meta_"), List.of(amount, note));

        assertTrue(exists("meta_inj_ok"));
        adminService.delete(id, true);
        assertFalse(exists("meta_inj_ok"));
    }

    private void assertNoTraceOf(String code) {
        assertFalse(tableRepository.findByTableCodeAndDeletedFalse(code).isPresent(), "registry row must roll back");
        assertFalse(exists("meta_" + code), "physical table must not exist");
    }

    private boolean exists(String relation) {
        return Boolean.TRUE.equals(jdbc.getJdbcOperations().queryForObject("SELECT to_regclass(?) IS NOT NULL",
                Boolean.class, relation));
    }

    private MetaTable table(String code, String prefix) {
        MetaTable table = new MetaTable();
        table.setTableCode(code);
        table.setTableName(code);
        table.setTablePrefix(prefix);
        return table;
    }

    private MetaColumn column(String code, MetaColumnType type) {
        MetaColumn column = new MetaColumn();
        column.setColumnCode(code);
        column.setColumnName(code);
        column.setDataType(type);
        if (type == MetaColumnType.STRING) {
            column.setLength(100);
        }
        return column;
    }
}
