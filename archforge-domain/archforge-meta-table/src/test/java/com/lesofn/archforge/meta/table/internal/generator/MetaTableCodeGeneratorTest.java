package com.lesofn.archforge.meta.table.internal.generator;

import com.lesofn.archforge.meta.table.api.domain.MetaColumn;
import com.lesofn.archforge.meta.table.api.domain.MetaColumnType;
import com.lesofn.archforge.meta.table.api.domain.MetaTable;
import com.lesofn.archforge.meta.table.api.domain.OptionItem;
import com.lesofn.archforge.meta.table.internal.generator.extension.CodeGenExtensionRegistry;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.assertj.core.api.Assertions.assertThat;

class MetaTableCodeGeneratorTest {

    @TempDir
    Path tempDir;

    private final MetaTableCodeGenerator generator = new MetaTableCodeGenerator(new CodeGenExtensionRegistry());

    @Test
    void shouldGenerateAllTypes() throws Exception {
        MetaTable table = new MetaTable();
        table.setId(1L);
        table.setTableCode("demo_project");
        table.setTableName("演示项目");
        table.setDescription("测试所有类型生成");

        List<MetaColumn> columns = new ArrayList<>();
        columns.add(column("name", "名称", MetaColumnType.STRING, true, true));
        columns.add(column("description", "描述", MetaColumnType.TEXT, true, true));
        columns.add(column("age", "年龄", MetaColumnType.INTEGER, true, true));
        columns.add(column("price", "价格", MetaColumnType.DECIMAL, true, true, 10, 2));
        columns.add(column("active", "启用", MetaColumnType.BOOLEAN, true, true));
        columns.add(column("birthday", "生日", MetaColumnType.DATE, true, true));
        columns.add(column("event_time", "事件时间", MetaColumnType.DATETIME, true, true));
        columns.add(column("status", "状态", MetaColumnType.ENUM, true, true, new OptionItem("启用", "ENABLED"),
                new OptionItem("禁用", "DISABLED")));
        columns.add(column("metadata", "元数据", MetaColumnType.JSON, true, true));
        columns.add(column("attachment", "附件", MetaColumnType.FILE, true, true));
        columns.add(column("uuid_col", "UUID列", MetaColumnType.UUID, true, true));
        columns.add(column("created_at_tz", "创建时间TZ", MetaColumnType.TIMESTAMPTZ, true, true));
        columns.add(arrayColumn("tags", "标签", MetaColumnType.ARRAY, true, true, "STRING"));
        columns.add(column("location", "位置", MetaColumnType.GEO, true, true));

        CodeGenOptions options = new CodeGenOptions();
        options.setProjectRoot(tempDir);
        options.setBackendOutputDir(tempDir.resolve("backend"));
        options.setFrontendOutputDir(tempDir.resolve("src/views/demo_project"));
        options.setBasePath("/generated/demo_project");
        options.setOverwrite(true);

        GeneratedResult result = generator.generate(table, columns, options);

        Path entityFile = tempDir.resolve(
                "backend/src/main/java/com/lesofn/archforge/generated/demoproject/domain/DemoProject.java");
        Path controllerFile = tempDir.resolve(
                "backend/src/main/java/com/lesofn/archforge/generated/demoproject/rest/DemoProjectController.java");
        Path buildGradleFile = tempDir.resolve("backend/build.gradle.kts");
        Path indexVue = tempDir.resolve("src/views/demo_project/index.vue");
        Path typesTs = tempDir.resolve("src/views/demo_project/utils/types.ts");

        assertThat(result.getFiles()).contains(entityFile, controllerFile, indexVue, typesTs);

        String entityContent = Files.readString(entityFile);
        assertThat(entityContent).contains("@JdbcTypeCode(SqlTypes.UUID)");
        assertThat(entityContent).contains("private UUID uuidCol");
        assertThat(entityContent).contains("@JdbcTypeCode(SqlTypes.ARRAY)");
        assertThat(entityContent).contains("private String[] tags");
        assertThat(entityContent).contains("@Type(JsonbStringUserType.class)");
        assertThat(entityContent).contains("private String metadata");
        assertThat(entityContent).contains("private OffsetDateTime createdAtTz");

        String serviceContent = Files.readString(tempDir.resolve(
                "backend/src/main/java/com/lesofn/archforge/generated/demoproject/service/DemoProjectService.java"));
        assertThat(serviceContent).contains("cb.equal(root.get(\"uuidCol\"), request.getUuidCol())");
        assertThat(serviceContent).contains("cb.equal(root.get(\"tags\"), request.getTags())");
        assertThat(serviceContent).contains("StringUtils.hasText(request.getKeyword())");

        String controllerContent = Files.readString(controllerFile);
        assertThat(controllerContent).contains("@GetMapping(\"/export\")");
        assertThat(controllerContent).contains("@PostMapping(\"/import\")");

        String buildGradleContent = Files.readString(buildGradleFile);
        assertThat(buildGradleContent).contains("api(project(\":archforge-domain:archforge-meta-table\"))");

        String typesContent = Files.readString(typesTs);
        assertThat(typesContent).doesNotContain(": ,");
        assertThat(typesContent).contains("uuidCol?: string");
        assertThat(typesContent).contains("tags?: string[]");
    }

    private MetaColumn column(String code, String name, MetaColumnType type, boolean required, boolean listVisible) {
        MetaColumn col = new MetaColumn();
        col.setColumnCode(code);
        col.setColumnName(name);
        col.setDataType(type);
        col.setRequired(required);
        col.setSearchable(true);
        col.setListVisible(listVisible);
        return col;
    }

    private MetaColumn column(String code, String name, MetaColumnType type, boolean required, boolean listVisible,
            OptionItem... options) {
        MetaColumn col = column(code, name, type, required, listVisible);
        if (options.length > 0) {
            List<OptionItem> list = new ArrayList<>();
            for (OptionItem option : options) {
                list.add(option);
            }
            col.setOptions(list);
        }
        return col;
    }

    private MetaColumn column(String code, String name, MetaColumnType type, boolean required, boolean listVisible,
            int precision, int scale) {
        MetaColumn col = column(code, name, type, required, listVisible);
        col.setPrecision(precision);
        col.setScale(scale);
        return col;
    }

    private MetaColumn arrayColumn(String code, String name, MetaColumnType type, boolean required, boolean listVisible,
            String elementType) {
        MetaColumn col = column(code, name, type, required, listVisible);
        col.setArrayElementType(elementType);
        return col;
    }
}
