package com.lesofn.archforge.server.admin.persistence;

import static org.junit.jupiter.api.Assertions.*;

import com.lesofn.archforge.common.persistence.testsupport.AbstractIntegrationTest;
import com.lesofn.archforge.meta.table.api.dao.MetaTableRepository;
import com.lesofn.archforge.meta.table.api.domain.MetaTable;
import com.lesofn.archforge.server.admin.Application;
import com.lesofn.archforge.user.api.dao.SysConfigRepository;
import com.lesofn.archforge.user.api.domain.SysConfig;
import jakarta.persistence.EntityManagerFactory;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;

/**
 * 合并 EMF 后的跨模块事务契约：单个 {@code @Transactional} 必须同时覆盖 admin-user 与 meta-table
 * 两个模块的实体写入——成功一起提交、失败一起回滚。此前每模块一个 EntityManagerFactory /
 * TransactionManager 的时代，跨模块写处于各自独立的事务边界内，无法保证原子性。
 *
 * @author sofn
 */
@SpringBootTest(
        classes = {
                Application.class, CrossModuleTransactionIntegrationTest.ProbeConfig.class
        },
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Tag("slow")
class CrossModuleTransactionIntegrationTest extends AbstractIntegrationTest {

    private static final String PROBE_CONFIG_KEY = "tx_it_cross_module_probe";
    private static final String PROBE_TABLE_CODE = "tx_it_cross_module_probe";

    @Autowired
    private SysConfigRepository configRepository;

    @Autowired
    private MetaTableRepository metaTableRepository;

    @Autowired
    private CrossModuleTxProbeService probe;

    @Autowired
    private Map<String, EntityManagerFactory> entityManagerFactories;

    @Autowired
    private Map<String, PlatformTransactionManager> transactionManagers;

    @TestConfiguration
    static class ProbeConfig {

        @Bean
        CrossModuleTxProbeService crossModuleTxProbeService(
                SysConfigRepository configRepository, MetaTableRepository metaTableRepository) {
            return new CrossModuleTxProbeService(configRepository, metaTableRepository);
        }
    }

    static class CrossModuleTxProbeService {

        private final SysConfigRepository configRepository;
        private final MetaTableRepository metaTableRepository;

        CrossModuleTxProbeService(
                SysConfigRepository configRepository, MetaTableRepository metaTableRepository) {
            this.configRepository = configRepository;
            this.metaTableRepository = metaTableRepository;
        }

        @Transactional
        public void writeBothModulesThenFail() {
            writeBothModules();
            throw new IllegalStateException("boom");
        }

        @Transactional
        public void writeBothModules() {
            SysConfig config = new SysConfig();
            config.setConfigName("跨模块事务探针");
            config.setConfigKey(PROBE_CONFIG_KEY);
            config.setConfigValue("1");
            config.setConfigType(0);
            configRepository.save(config);

            MetaTable table = new MetaTable();
            table.setTableCode(PROBE_TABLE_CODE);
            table.setTableName("跨模块事务探针");
            table.setStatus(1);
            metaTableRepository.save(table);
        }
    }

    @AfterEach
    void cleanUp() {
        configRepository.findByConfigKey(PROBE_CONFIG_KEY).ifPresent(configRepository::delete);
        metaTableRepository
                .findByTableCodeAndDeletedFalse(PROBE_TABLE_CODE)
                .ifPresent(metaTableRepository::delete);
    }

    @Test
    void singleEntityManagerFactoryAndTransactionManager() {
        assertEquals(1, entityManagerFactories.size(), "merged EMF: exactly one EntityManagerFactory bean");
        assertEquals(
                1,
                transactionManagers.size(),
                "merged EMF: exactly one PlatformTransactionManager bean");
    }

    @Test
    void writesAcrossModulesRollBackTogether() {
        assertThrows(IllegalStateException.class, () -> probe.writeBothModulesThenFail());

        assertTrue(
                configRepository.findByConfigKey(PROBE_CONFIG_KEY).isEmpty(),
                "admin-module write must roll back with the meta-table write");
        assertTrue(
                metaTableRepository.findByTableCodeAndDeletedFalse(PROBE_TABLE_CODE).isEmpty(),
                "meta-table write must roll back with the admin-module write");
    }

    @Test
    void writesAcrossModulesCommitTogether() {
        probe.writeBothModules();

        assertTrue(configRepository.findByConfigKey(PROBE_CONFIG_KEY).isPresent());
        assertTrue(
                metaTableRepository.findByTableCodeAndDeletedFalse(PROBE_TABLE_CODE).isPresent());
    }
}
