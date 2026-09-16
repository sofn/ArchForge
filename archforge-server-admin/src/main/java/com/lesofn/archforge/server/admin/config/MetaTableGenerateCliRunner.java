package com.lesofn.archforge.server.admin.config;

import com.lesofn.archforge.meta.table.api.domain.MetaColumn;
import com.lesofn.archforge.meta.table.api.domain.MetaTable;
import com.lesofn.archforge.meta.table.api.service.MetaTableAdminService;
import com.lesofn.archforge.meta.table.api.codegen.CodeGenOptions;
import com.lesofn.archforge.meta.table.api.codegen.GeneratedResult;
import com.lesofn.archforge.meta.table.api.codegen.MetaTableCodeGenerator;
import java.nio.file.Path;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * 元表格代码生成 CLI 入口。
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "generate.tableId")
public class MetaTableGenerateCliRunner implements CommandLineRunner {

    private final ApplicationContext applicationContext;
    private final Environment environment;
    private final MetaTableAdminService metaTableAdminService;
    private final MetaTableCodeGenerator metaTableCodeGenerator;
    private final CodeGenWorkspaceResolver codeGenWorkspaceResolver;

    @Override
    public void run(String... args) {
        Long tableId = Long.valueOf(environment.getProperty("generate.tableId"));
        MetaTable table = metaTableAdminService.findById(tableId);
        List<MetaColumn> columns = metaTableAdminService.findColumns(tableId);

        String tableCode = table.getTableCode();
        String backendDirProp = environment.getProperty("generate.backendDir");
        String frontendDirProp = environment.getProperty("generate.frontendDir");
        String basePath = environment.getProperty("generate.basePath", "/generated/" + tableCode);
        boolean overwrite = Boolean.parseBoolean(environment.getProperty("generate.overwrite", "false"));

        Path projectRoot = codeGenWorkspaceResolver.resolve();
        Path backendDir = codeGenWorkspaceResolver.resolveBackendDir(backendDirProp, tableCode);
        Path frontendDir = codeGenWorkspaceResolver.resolveFrontendDir(frontendDirProp, tableCode);

        CodeGenOptions options = new CodeGenOptions();
        options.setProjectRoot(projectRoot);
        options.setBackendOutputDir(backendDir);
        options.setFrontendOutputDir(frontendDir);
        options.setBasePath(basePath);
        options.setOverwrite(overwrite);

        GeneratedResult result = metaTableCodeGenerator.generate(table, columns, options);
        log.info("Generated backend: {}", result.getBackendDir());
        log.info("Generated frontend: {}", result.getFrontendDir());
        log.info("Generated files count: {}", result.getFiles().size());

        // P0-K: 设计工具不得杀宿主进程——只关闭 context，由 JVM 自然退出（进程内残留
        // 非 daemon 线程时调用方负责终止，例如 CI/超时控制）
        int exitCode = SpringApplication.exit(applicationContext, () -> 0);
        if (exitCode != 0) {
            log.warn("Code generation finished with non-zero exit code {}", exitCode);
        }
    }
}
