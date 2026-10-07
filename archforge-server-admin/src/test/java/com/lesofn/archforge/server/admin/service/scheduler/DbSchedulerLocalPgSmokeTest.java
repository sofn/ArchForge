package com.lesofn.archforge.server.admin.service.scheduler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.kagkarlsson.scheduler.ScheduledExecution;
import com.github.kagkarlsson.scheduler.Scheduler;
import com.github.kagkarlsson.scheduler.task.helper.RecurringTaskWithPersistentSchedule;
import com.github.kagkarlsson.scheduler.task.helper.Tasks;
import com.github.kagkarlsson.scheduler.task.schedule.Schedules;
import com.lesofn.archforge.common.persistence.FlywayConfig;
import com.lesofn.archforge.common.persistence.FlywayProperties;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;
import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.jspecify.annotations.Nullable;
import org.postgresql.ds.PGSimpleDataSource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * db-scheduler migration + live scheduling smoke test against real PostgreSQL.
 *
 * <p>
 * Covers what unit tests cannot: the V23 migration SQL against (a) a fresh database migrated the way the
 * application does it at startup ({@link FlywayConfig.FlywayModuleOrchestrator}: {@code __root} first, then every
 * module with its own history table) and (b) a simulated Quartz-era database, plus live scheduling behavior —
 * recurring execution, reschedule (cron change), pause via cancel, and the persistence of schedule+data in
 * {@code task_data}.
 *
 * <p>
 * It used to be gated on {@code ARCHFORGE_LOCAL_PG=true} and therefore never ran in CI — it silently rotted: the
 * default Flyway scan flattened {@code __root/V1} and {@code cms/V1} into one version sequence and every run died on
 * "Found more than one migration with version 1". Now it runs with the other slow integration tests on a
 * Testcontainers PostgreSQL. Docker-less sandboxes can still point it at a local superuser connection:
 * {@code ARCHFORGE_SMOKE_PG_URL=jdbc:postgresql://localhost:54329/smoke?user=postgres&password=postgres}.
 */
@Tag("slow")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DbSchedulerLocalPgSmokeTest {

    private static final String FRESH_DB = "archforge_fresh";
    private static final String LEGACY_DB = "archforge_legacy";

    private static @Nullable PostgreSQLContainer container;
    private static String url = "";

    @BeforeAll
    static void prepareDatabases() throws Exception {
        String override = System.getenv("ARCHFORGE_SMOKE_PG_URL");
        if (override != null) {
            url = override;
        } else {
            container = new PostgreSQLContainer(DockerImageName.parse("postgres:17-alpine"))
                    .withDatabaseName("smoke").withUsername("postgres").withPassword("postgres");
            container.start();
            url = container.getJdbcUrl() + "&user=postgres&password=postgres";
        }
        try (Connection admin = DriverManager.getConnection(url);
                java.sql.Statement stmt = admin.createStatement()) {
            for (String db : new String[] {
                    FRESH_DB, LEGACY_DB
            }) {
                stmt.execute("DROP DATABASE IF EXISTS " + db);
                stmt.execute("CREATE DATABASE " + db);
            }
        }
    }

    @AfterAll
    static void cleanup() throws Exception {
        try (Connection admin = DriverManager.getConnection(url);
                java.sql.Statement stmt = admin.createStatement()) {
            for (String db : new String[] {
                    FRESH_DB, LEGACY_DB
            }) {
                stmt.execute("DROP DATABASE IF EXISTS " + db);
            }
        } finally {
            if (container != null) {
                container.stop();
            }
        }
    }

    @SuppressWarnings("StringSplitter") // 取首段即可，split() 语义最贴合
    private static DataSource ds(String db) {
        String base = url.split("\\?")[0];
        String query = url.contains("?") ? url.substring(url.indexOf('?')) : "";
        PGSimpleDataSource dataSource = new PGSimpleDataSource();
        dataSource.setURL(base.substring(0, base.lastIndexOf('/') + 1) + db + query);
        return dataSource;
    }

    private static boolean tableExists(Connection c, String table) throws Exception {
        try (ResultSet rs = c.getMetaData().getTables(null, "public", table.toLowerCase(Locale.ROOT), null)) {
            return rs.next();
        }
    }

    @Test
    @Order(1)
    void freshDatabaseMigratesCleanlyWithoutQuartzTables() {
        // exactly what the application does at startup: __root, then each module (own history table)
        new FlywayConfig.FlywayModuleOrchestrator(ds(FRESH_DB), new FlywayProperties()).migrate();

        try (Connection c = ds(FRESH_DB).getConnection()) {
            assertTrue(tableExists(c, "scheduled_tasks"), "db-scheduler table must exist");
            assertTrue(tableExists(c, "sys_scheduled_job"));
            assertTrue(tableExists(c, "sys_job_log"));
            assertFalse(tableExists(c, "qrtz_triggers"), "Quartz tables must never exist on fresh DBs");
            assertFalse(tableExists(c, "sys_quartz_job"));

            try (java.sql.Statement st = c.createStatement();
                    ResultSet rs = st.executeQuery(
                            "SELECT job_name, bean_name, cron, status FROM sys_scheduled_job WHERE job_name='demo-hello'")) {
                assertTrue(rs.next());
                assertEquals("demoSchedulerJob", rs.getString("bean_name"));
                assertEquals("0/30 * * * * *", rs.getString("cron"));
            }
            try (java.sql.Statement st = c.createStatement();
                    ResultSet rs = st.executeQuery(
                            "SELECT remark FROM sys_menu WHERE menu_id=100")) {
                assertTrue(rs.next());
                assertFalse(rs.getString("remark").contains("Quartz"));
            }
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    @Test
    @Order(2)
    void legacyQuartzDatabaseIsMigratedAndCleaned() throws Exception {
        DataSource legacy = ds(LEGACY_DB);
        // 1. Simulate a Quartz-era database (qrtz_* + sys_quartz_* + sys_menu rows) — see
        //    db/legacy-quartz-fixture.sql for the shape a real V1-V22 database would have.
        try (Connection c = legacy.getConnection()) {
            ScriptUtils.executeSqlScript(
                    c,
                    new EncodedResource(new org.springframework.core.io.ClassPathResource("db/legacy-quartz-fixture.sql"), StandardCharsets.UTF_8));
        }
        // 2. Baseline at 22 (the pre-V23 chain is "already applied") and stop at V23, which carries the
        //    Quartz→db-scheduler conversion. Later migrations (V24+) assume the full V1-V22 schema (meta tables, ...)
        //    that this minimal fixture deliberately does not recreate.
        Flyway.configure().dataSource(legacy).locations("classpath:db/migration/__root")
                .baselineOnMigrate(true).baselineVersion("22").target("23").load().migrate();
        // 4. Assertions
        try (Connection c = legacy.getConnection()) {
            assertFalse(tableExists(c, "qrtz_triggers"));
            assertFalse(tableExists(c, "sys_quartz_job"));
            assertFalse(tableExists(c, "sys_quartz_log"));
            assertTrue(tableExists(c, "sys_scheduled_job"));
            assertTrue(tableExists(c, "scheduled_tasks"));

            try (java.sql.PreparedStatement ps = c.prepareStatement(
                    "SELECT id, bean_name, cron, status, description FROM sys_scheduled_job WHERE job_name='legacy-job'");
                    ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next(), "legacy row must be moved");
                assertEquals("demoSchedulerJob", rs.getString("bean_name"), "bean rename applied");
                assertEquals("0/15 * * * * *", rs.getString("cron"), "cron ? normalized to *");
                assertEquals(0, rs.getShort("status"));
            }
            try (java.sql.PreparedStatement ps = c.prepareStatement(
                    "SELECT count(*) FROM sys_job_log"); ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next());
                assertEquals(1, rs.getInt(1), "execution log history preserved");
            }
        }
    }

    @Test
    @Order(3)
    void dynamicSchedulingLifecycleWorks() throws Exception {
        DataSource fresh = ds(FRESH_DB);
        AtomicInteger runs = new AtomicInteger();

        RecurringTaskWithPersistentSchedule<JobInvocationData> task = Tasks.recurringWithPersistentSchedule(
                SchedulerConfig.ADMIN_JOB_TASK)
                .execute((inst, ctx) -> runs.incrementAndGet());

        Scheduler scheduler = Scheduler.create(fresh, task)
                .threads(2)
                .pollingInterval(Duration.ofMillis(300))
                .build();
        scheduler.start();
        try {
            // 1. schedule a recurring instance (every 2s)
            JobInvocationData data = new JobInvocationData(1L, "smoke-job", "DEFAULT", "demoSchedulerJob", "helloWorld", null, Schedules
                    .cron("*/2 * * * * *"));
            scheduler.schedule(
                    SchedulerConfig.ADMIN_JOB_TASK.instance("job-1").data(data).scheduledAccordingToData());

            Thread.sleep(5_000);
            int first = runs.get();
            assertTrue(first >= 2, "recurring execution must fire, got " + first);

            // 2. reschedule: 1s cadence → execution rate increases
            JobInvocationData faster = new JobInvocationData(1L, "smoke-job", "DEFAULT", "demoSchedulerJob", "helloWorld", null, Schedules
                    .cron("*/1 * * * * *"));
            scheduler.reschedule(
                    SchedulerConfig.ADMIN_JOB_TASK.instance("job-1").data(faster).scheduledAccordingToData());
            Thread.sleep(4_000);
            int second = runs.get();
            assertTrue(second - first >= 3, "reschedule to 1s must take effect, delta=" + (second - first));

            // 3. pause: the runtime instance is cancelled (db-scheduler has no persisted
            //    paused state — DisabledSchedule is read-only, writes throw). The
            //    sys_scheduled_job row remains the resume source-of-truth.
            scheduler.cancel(SchedulerConfig.ADMIN_JOB_TASK.instanceId("job-1"));
            Thread.sleep(3_000);
            int third = runs.get();
            assertEquals(second, third, "cancelled instance must stop executing");

            // 4. paused instance is gone from the runtime table
            List<ScheduledExecution<Object>> instances = scheduler.getScheduledExecutionsForTask(
                    SchedulerConfig.ADMIN_JOB_TASK_NAME);
            assertTrue(instances.isEmpty(), "cancelled instance must be removed from the runtime table");
        } finally {
            scheduler.stop();
        }
    }
}
