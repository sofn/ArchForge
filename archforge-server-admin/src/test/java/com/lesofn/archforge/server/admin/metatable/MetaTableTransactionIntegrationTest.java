package com.lesofn.archforge.server.admin.metatable;

import static org.junit.jupiter.api.Assertions.*;

import com.lesofn.archforge.meta.table.api.dao.MetaTableRepository;
import com.lesofn.archforge.meta.table.api.domain.MetaTable;
import com.lesofn.archforge.server.admin.Application;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration test proving {@code metaTableJdbcTemplate} shares the transactional connection of
 * {@code metaTableTransactionManager}: DDL/DML executed through the template must roll back
 * together with JPA metadata writes when the business transaction fails.
 *
 * @author sofn
 */
@SpringBootTest(
        classes = {
                Application.class, MetaTableTransactionIntegrationTest.ProbeConfig.class
        },
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Tag("slow")
class MetaTableTransactionIntegrationTest {

    private static final String PROBE_TABLE = "meta_tx_it_probe";
    private static final String PROBE_CODE = "tx_it_probe";

    @Qualifier("metaTableJdbcTemplate")
    @Autowired
    private NamedParameterJdbcTemplate jdbc;

    @Autowired
    private MetaTableRepository repository;

    @Autowired
    private MetaTxProbeService probe;

    @BeforeAll
    void cleanUp() {
        jdbc.getJdbcOperations().execute("DROP TABLE IF EXISTS " + PROBE_TABLE);
        jdbc.update("DELETE FROM sys_meta_table WHERE table_code = :code", Map.of("code", PROBE_CODE));
    }

    @TestConfiguration
    static class ProbeConfig {

        @Bean
        MetaTxProbeService metaTxProbeService(
                MetaTableRepository repository,
                @Qualifier("metaTableJdbcTemplate") NamedParameterJdbcTemplate jdbcTemplate) {
            return new MetaTxProbeService(repository, jdbcTemplate);
        }
    }

    static class MetaTxProbeService {

        private final MetaTableRepository repository;
        private final NamedParameterJdbcTemplate jdbcTemplate;

        MetaTxProbeService(MetaTableRepository repository, NamedParameterJdbcTemplate jdbcTemplate) {
            this.repository = repository;
            this.jdbcTemplate = jdbcTemplate;
        }

        record SessionIdentity(long holderPid, long templatePid, boolean autoCommit) {
        }

        @Transactional("metaTableTransactionManager")
        public SessionIdentity sessionIdentity() {
            DataSource ds = requireDataSource();
            Connection bound = DataSourceUtils.getConnection(ds);
            try {
                long holderPid = backendPid(bound);
                boolean autoCommit = bound.getAutoCommit();
                Long templatePid = jdbcTemplate
                        .getJdbcOperations()
                        .queryForObject("SELECT pg_backend_pid()", Long.class);
                assertNotNull(templatePid);
                return new SessionIdentity(holderPid, templatePid, autoCommit);
            } catch (SQLException e) {
                throw new IllegalStateException(e);
            } finally {
                DataSourceUtils.releaseConnection(bound, ds);
            }
        }

        @Transactional("metaTableTransactionManager")
        public void createAllThenFail() {
            createAll();
            throw new IllegalStateException("boom");
        }

        @Transactional("metaTableTransactionManager")
        public void createAll() {
            MetaTable table = new MetaTable();
            table.setTableCode(PROBE_CODE);
            table.setTableName("事务集成测试");
            table.setStatus(1);
            repository.save(table);
            jdbcTemplate
                    .getJdbcOperations()
                    .execute("CREATE TABLE " + PROBE_TABLE + " (id BIGINT PRIMARY KEY, name TEXT)");
            jdbcTemplate.getJdbcOperations().execute("INSERT INTO " + PROBE_TABLE + " VALUES (1, 'row')");
        }

        private DataSource requireDataSource() {
            DataSource ds = jdbcTemplate.getJdbcTemplate().getDataSource();
            assertNotNull(ds);
            return ds;
        }

        private long backendPid(Connection connection) throws SQLException {
            try (Statement st = connection.createStatement();
                    var rs = st.executeQuery("SELECT pg_backend_pid()")) {
                rs.next();
                return rs.getLong(1);
            }
        }
    }

    @Test
    void jdbcTemplateUsesTheBoundTransactionalConnection() {
        MetaTxProbeService.SessionIdentity identity = probe.sessionIdentity();
        assertEquals(identity.holderPid(), identity.templatePid(), "template must run on the tx session");
        assertFalse(identity.autoCommit(), "tx connection must have autocommit disabled");
    }

    @Test
    void ddlAndDmlRollBackTogetherWithMetadataWhenTransactionFails() {
        assertThrows(IllegalStateException.class, () -> probe.createAllThenFail());

        assertTrue(repository.findByTableCodeAndDeletedFalse(PROBE_CODE).isEmpty(), "metadata row must roll back");
        Boolean tableGone = jdbc
                .getJdbcOperations()
                .queryForObject("SELECT to_regclass('" + PROBE_TABLE + "') IS NULL", Boolean.class);
        assertEquals(Boolean.TRUE, tableGone, "physical table must roll back");
    }

    @Test
    void ddlAndDmlCommitTogetherWithMetadataOnSuccess() {
        probe.createAll();

        assertTrue(repository.findByTableCodeAndDeletedFalse(PROBE_CODE).isPresent(), "metadata row must commit");
        Integer rows = jdbc.getJdbcOperations().queryForObject("SELECT COUNT(*) FROM " + PROBE_TABLE, Integer.class);
        assertEquals(1, rows);

        cleanUp();
    }
}
