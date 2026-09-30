package com.lesofn.archforge.server.admin.metatable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.lesofn.archforge.common.persistence.testsupport.AbstractIntegrationTest;
import com.lesofn.archforge.meta.table.api.dao.MetaTableRepository;
import com.lesofn.archforge.meta.table.api.domain.MetaColumn;
import com.lesofn.archforge.meta.table.api.domain.MetaColumnType;
import com.lesofn.archforge.meta.table.api.domain.MetaTable;
import com.lesofn.archforge.meta.table.api.errors.MetaTableErrorCode;
import com.lesofn.archforge.meta.table.api.errors.MetaTableException;
import com.lesofn.archforge.meta.table.api.service.MetaDefinitionRegistry;
import com.lesofn.archforge.meta.table.api.service.MetaTableAdminService;
import com.lesofn.archforge.server.admin.Application;
import com.lesofn.archforge.server.admin.config.EnumOptionsMigrationRunner;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.ObjectMapper;

/**
 * {@code arch-forge.meta.source=file} end to end (P3-4-3 F2/F3): the context
 * boots with the definition files materialized and the runtime registry
 * pinned, designer writes are rejected with 10415 (service and HTTP
 * envelope), and the legacy options runner is absent.
 */
@SpringBootTest(classes = {
        Application.class
}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Tag("slow")
class MetaTableFileSourceIntegrationTest extends AbstractIntegrationTest {

    private static final ObjectMapper JSON = new ObjectMapper();

    @DynamicPropertySource
    static void fileSource(DynamicPropertyRegistry registry) throws IOException {
        // Written before the context starts — the applier runs during startup. Lives under build/ (cwd = module).
        Path dir = Files.createTempDirectory(Files.createDirectories(Path.of("build/tmp")), "meta-file-source");
        Files.writeString(dir.resolve("fsrc_boot.yaml"), """
                tableCode: fsrc_boot
                tableName: 启动物化
                columns:
                  - columnCode: name
                    columnName: 名称
                    dataType: STRING
                    required: true
                """);
        registry.add("arch-forge.meta.source", () -> "file");
        registry.add("arch-forge.meta.definition-dir", dir::toString);
    }

    @LocalServerPort
    int port;

    @Autowired
    private MetaTableRepository tableRepository;

    @Autowired
    private MetaTableAdminService adminService;

    @Autowired
    private MetaDefinitionRegistry registry;

    @Autowired
    private ApplicationContext context;

    @Test
    void bootMaterializesTheDefinitionFiles() {
        MetaTable table = tableRepository.findByTableCodeAndDeletedFalse("fsrc_boot").orElseThrow();
        assertEquals("启动物化", table.getTableName());
    }

    @Test
    void designerWritesAreRejectedAtTheService() {
        MetaTable table = new MetaTable();
        table.setTableCode("fsrc_designer");
        table.setTableName("设计器写");
        MetaColumn column = new MetaColumn();
        column.setColumnCode("name");
        column.setColumnName("名称");
        column.setDataType(MetaColumnType.STRING);

        MetaTableException e = assertThrows(MetaTableException.class, () -> adminService.create(table, List.of(column)));

        assertEquals(MetaTableErrorCode.META_DEFINITION_FILE_MANAGED.getCode(), e.getErrorInfo().getCode());
        assertTrue(tableRepository.findByTableCodeAndDeletedFalse("fsrc_designer").isEmpty());
    }

    @Test
    @SuppressWarnings("unchecked")
    void designerWritesAreRejectedOverHttpWithTheEnvelopeCode() throws Exception {
        RestClient rest = RestClient.builder().baseUrl("http://localhost:" + port).build();
        Map<String, Object> login = JSON.readValue(rest.post().uri("/admin/auth/login")
                .header("Content-Type", "application/json")
                .body(Map.of("username", "admin", "password", "admin123"))
                .retrieve().body(String.class), Map.class);
        String token = (String) Objects.requireNonNull(dataOf(login).get("accessToken"));

        Map<String, Object> resp = JSON.readValue(rest.post().uri("/admin/meta-table/create")
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .body(Map.of("tableCode", "fsrc_http", "tableName", "HTTP 写", "columns", List.of(
                        Map.of("columnCode", "name", "columnName", "名称", "dataType", "STRING"))))
                .retrieve().body(String.class), Map.class);

        assertEquals(MetaTableErrorCode.META_DEFINITION_FILE_MANAGED.getCode(), resp.get("code"));
    }

    @Test
    void runtimeReadsThePinnedViewNotTheMirror() {
        assertTrue(registry.find("fsrc_boot").isPresent());
        MetaTable late = new MetaTable();
        late.setTableCode("fsrc_late");
        late.setTableName("启动后直写");
        late.setTablePrefix("meta_");
        late.setStatus(1);
        tableRepository.save(late); // behind the registry's back, after the startup pin

        assertTrue(tableRepository.findByTableCodeAndDeletedFalse("fsrc_late").isPresent());
        assertTrue(registry.find("fsrc_late").isEmpty(), "pinned view must not see later mirror writes");
    }

    @Test
    void legacyEnumOptionsRunnerIsNotAssembled() {
        assertNull(context.getBeanProvider(EnumOptionsMigrationRunner.class).getIfAvailable());
    }
}
