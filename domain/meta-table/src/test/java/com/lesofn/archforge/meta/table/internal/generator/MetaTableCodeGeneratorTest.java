package com.lesofn.archforge.meta.table.internal.generator;

import static org.assertj.core.api.Assertions.assertThat;

import com.lesofn.archforge.meta.table.api.domain.MetaColumn;
import com.lesofn.archforge.meta.table.api.domain.MetaColumnType;
import com.lesofn.archforge.meta.table.api.domain.MetaTable;
import com.lesofn.archforge.meta.table.api.domain.OptionItem;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class MetaTableCodeGeneratorTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldGenerateAllFiles() throws Exception {
        MetaTable table = new MetaTable();
        table.setTableCode("demo_project");
        table.setTableName("演示项目");
        table.setDescription("A demo table for code generation");

        List<MetaColumn> columns = List.of(
                column("name", "名称", MetaColumnType.STRING, true, true),
                column("description", "描述", MetaColumnType.TEXT, false, false),
                column("age", "年龄", MetaColumnType.INTEGER, true, true),
                column("price", "价格", MetaColumnType.DECIMAL, true, true),
                column("active", "启用", MetaColumnType.BOOLEAN, true, true),
                column("birthday", "生日", MetaColumnType.DATE, true, true),
                column("created_at", "创建时间", MetaColumnType.DATETIME, false, true),
                column("status", "状态", MetaColumnType.ENUM, true, true, new OptionItem("启用", "ENABLED"),
                        new OptionItem("禁用", "DISABLED")),
                column("attachment", "附件", MetaColumnType.FILE, false, false),
                column("metadata", "元数据", MetaColumnType.JSON, false, false));

        CodeGenOptions options = new CodeGenOptions();
        options.setProjectRoot(tempDir);
        options.setBackendOutputDir(tempDir.resolve("example/demo_project"));
        options.setFrontendOutputDir(tempDir.resolve("src/views/demo_project"));
        options.setBasePath("/generated/demo_project");
        options.setOverwrite(false);

        MetaTableCodeGenerator generator = new MetaTableCodeGenerator();
        GeneratedResult result = generator.generate(table, columns, options);

        assertThat(result.getBackendDir()).exists();
        assertThat(result.getFrontendDir()).exists();
        assertThat(result.getFiles()).isNotEmpty();

        Path buildFile = result.getBackendDir().resolve("build.gradle.kts");
        assertThat(buildFile).exists();

        Path entityFile = result.getBackendDir().resolve(
                "src/main/java/com/lesofn/archforge/generated/demoproject/domain/DemoProject.java");
        assertThat(entityFile).exists();
        String entityContent = Files.readString(entityFile, StandardCharsets.UTF_8);
        assertThat(entityContent).contains("@Entity");

        Path serviceFile = result.getBackendDir().resolve(
                "src/main/java/com/lesofn/archforge/generated/demoproject/service/DemoProjectService.java");
        assertThat(serviceFile).exists();
        String serviceContent = Files.readString(serviceFile, StandardCharsets.UTF_8);
        assertThat(serviceContent).contains("Specification");

        Path indexVue = result.getFrontendDir().resolve("index.vue");
        assertThat(indexVue).exists();
        String vueContent = Files.readString(indexVue, StandardCharsets.UTF_8);
        assertThat(vueContent).contains("el-switch");
    }

    private MetaColumn column(String code, String name, MetaColumnType type, boolean required, boolean listVisible,
            OptionItem... options) {
        MetaColumn column = new MetaColumn();
        column.setColumnCode(code);
        column.setColumnName(name);
        column.setDataType(type);
        column.setRequired(required);
        column.setSearchable(true);
        column.setListVisible(listVisible);
        column.setNullable(!required);
        if (type == MetaColumnType.ENUM && options.length > 0) {
            column.setOptions(List.of(options));
        }
        if (type == MetaColumnType.STRING) {
            column.setLength(255);
        }
        if (type == MetaColumnType.DECIMAL) {
            column.setPrecision(10);
            column.setScale(2);
        }
        return column;
    }
}
