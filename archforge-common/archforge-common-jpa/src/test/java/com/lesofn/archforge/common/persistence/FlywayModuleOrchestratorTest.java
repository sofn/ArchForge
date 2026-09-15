package com.lesofn.archforge.common.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.Test;

/**
 * {@link FlywayConfig.FlywayModuleOrchestrator} 的编排契约：
 * root location 先跑并写默认历史表，模块目录随后各写自己的
 * {@code flyway_schema_history_<module>}。
 *
 * <p>
 * 测试 classpath 上的 {@code db/migration/fake4test/} 充当模块目录；root locations
 * 显式指向同一目录（H2 + 幂等 SQL，两次应用均安全）。
 */
class FlywayModuleOrchestratorTest {

    @Test
    void discoverModulesFindsModuleDirsAndExcludesRoot() {
        assertThat(FlywayConfig.FlywayModuleOrchestrator.discoverModules())
                .containsExactly("fake4test");
    }

    @Test
    void migrateRunsRootThenModuleWithSeparateHistoryTables() throws Exception {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:flyway-orch;DB_CLOSE_DELAY=-1");
        FlywayProperties properties = new FlywayProperties();
        properties.setLocations(List.of("classpath:db/migration/fake4test"));

        new FlywayConfig.FlywayModuleOrchestrator(dataSource, properties).migrate();

        try (Connection conn = dataSource.getConnection()) {
            assertThat(appliedCount(conn, "flyway_schema_history")).isEqualTo(1);
            assertThat(appliedCount(conn, "flyway_schema_history_fake4test")).isEqualTo(1);
            try (ResultSet rs = conn.getMetaData().getTables(null, null, "FLYWAY_ORCH_PROBE", null)) {
                assertThat(rs.next()).as("probe table created by the module migration").isTrue();
            }
        }
    }

    private int appliedCount(Connection conn, String historyTable) throws Exception {
        try (Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM \"" + historyTable +
                        "\" WHERE \"success\" AND \"type\" = 'SQL'")) {
            rs.next();
            return rs.getInt(1);
        }
    }
}
