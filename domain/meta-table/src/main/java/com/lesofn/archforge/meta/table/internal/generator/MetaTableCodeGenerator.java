package com.lesofn.archforge.meta.table.internal.generator;

import com.lesofn.archforge.meta.table.api.domain.MetaColumn;
import com.lesofn.archforge.meta.table.api.domain.MetaTable;
import com.lesofn.archforge.meta.table.internal.exception.MetaTableException;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import java.io.IOException;
import org.springframework.stereotype.Component;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
public class MetaTableCodeGenerator {

    private static final String TEMPLATE_DIR = "/templates/codegen";
    private static final String ENCODING = "UTF-8";

    private final Configuration configuration;

    public MetaTableCodeGenerator() {
        this.configuration = new Configuration(Configuration.VERSION_2_3_33);
        this.configuration.setDefaultEncoding(ENCODING);
        this.configuration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        this.configuration.setTemplateLoader(
                new freemarker.cache.ClassTemplateLoader(MetaTableCodeGenerator.class, TEMPLATE_DIR));
    }

    public GeneratedResult generate(MetaTable table, List<MetaColumn> columns, CodeGenOptions options)
            throws MetaTableException {
        Objects.requireNonNull(table, "MetaTable must not be null");
        Objects.requireNonNull(columns, "columns must not be null");
        Objects.requireNonNull(options, "CodeGenOptions must not be null");

        Path projectRoot = requireDirectory(options.getProjectRoot(), "projectRoot");
        Path backendOutputDir = resolveOutputDir(projectRoot, options.getBackendOutputDir(), "backendOutputDir");
        Path frontendOutputDir = resolveOutputDir(projectRoot, options.getFrontendOutputDir(), "frontendOutputDir");

        validateWithinProjectRoot(projectRoot, backendOutputDir);
        validateWithinProjectRoot(projectRoot, frontendOutputDir);

        String tableCode = table.getTableCode();
        Path frontendRoot = resolveFrontendRoot(frontendOutputDir, tableCode);
        if (!isWithinProjectRoot(projectRoot, frontendRoot)) {
            throw new MetaTableException("frontendRoot must be inside project root: " + frontendRoot);
        }

        if (!options.isOverwrite()) {
            checkNotEmpty(backendOutputDir);
            checkNotEmpty(frontendOutputDir);
        } else {
            deleteIfExists(backendOutputDir);
            deleteIfExists(frontendOutputDir);
        }

        Map<String, Object> model = CodeGenModelFactory.buildModel(table, columns, options);

        List<Path> files = new ArrayList<>();
        files.addAll(renderBackend(backendOutputDir, model));
        files.addAll(renderFrontend(frontendRoot, frontendOutputDir, model));

        GeneratedResult result = new GeneratedResult();
        result.setBackendDir(backendOutputDir);
        result.setFrontendDir(frontendOutputDir);
        result.setFiles(files);
        return result;
    }

    private Path requireDirectory(Path path, String name) {
        if (path == null) {
            throw new MetaTableException(name + " must not be null");
        }
        return path.toAbsolutePath().normalize();
    }

    private Path resolveOutputDir(Path projectRoot, Path outputDir, String name) {
        if (outputDir == null) {
            throw new MetaTableException(name + " must not be null");
        }
        if (outputDir.isAbsolute()) {
            return outputDir.normalize();
        }
        return projectRoot.resolve(outputDir).normalize();
    }

    private void validateWithinProjectRoot(Path projectRoot, Path target) {
        if (!isWithinProjectRoot(projectRoot, target)) {
            throw new MetaTableException("Output directory must be inside project root: " + target);
        }
    }

    private boolean isWithinProjectRoot(Path projectRoot, Path target) {
        Path root = projectRoot.toAbsolutePath().normalize();
        Path absolute = target.toAbsolutePath().normalize();
        return absolute.startsWith(root);
    }

    private Path resolveFrontendRoot(Path frontendOutputDir, String tableCode) {
        Path normalized = frontendOutputDir.normalize();
        if (isPathSegment(normalized, tableCode) && isPathSegment(normalized.getParent(), "views") && isPathSegment(normalized
                .getParent().getParent(), "src")) {
            return normalized.getParent().getParent().getParent().normalize();
        }
        return normalized;
    }

    private boolean isPathSegment(Path path, String segment) {
        return path != null && path.getFileName() != null && path.getFileName().toString().equals(segment);
    }

    private void checkNotEmpty(Path dir) {
        if (!Files.exists(dir)) {
            return;
        }
        if (!Files.isDirectory(dir)) {
            throw new MetaTableException("Output path exists but is not a directory: " + dir);
        }
        try (var stream = Files.list(dir)) {
            if (stream.findAny().isPresent()) {
                throw new MetaTableException("Output directory is not empty: " + dir);
            }
        } catch (IOException e) {
            throw new MetaTableException("Failed to check output directory: " + dir, e);
        }
    }

    private void deleteIfExists(Path dir) {
        if (!Files.exists(dir)) {
            return;
        }
        try {
            Files.walkFileTree(dir, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            throw new MetaTableException("Failed to clean output directory: " + dir, e);
        }
    }

    private List<Path> renderBackend(Path backendDir, Map<String, Object> model) {
        String javaPackage = (String) model.get("javaPackage");
        String entityName = (String) model.get("entityName");
        String packagePath = "com/lesofn/archforge/generated/" + javaPackage;

        Map<String, String> templateToPath = new LinkedHashMap<>();
        templateToPath.put("build.gradle.kts.ftl", "build.gradle.kts");
        templateToPath.put("entity.java.ftl", "src/main/java/" + packagePath + "/domain/" + entityName + ".java");
        templateToPath.put("dao.java.ftl", "src/main/java/" + packagePath + "/dao/" + entityName + "Dao.java");
        templateToPath.put("create-request.java.ftl", "src/main/java/" + packagePath + "/dto/" + entityName +
                "CreateRequest.java");
        templateToPath.put("update-request.java.ftl", "src/main/java/" + packagePath + "/dto/" + entityName +
                "UpdateRequest.java");
        templateToPath.put("list-request.java.ftl", "src/main/java/" + packagePath + "/dto/" + entityName + "ListRequest.java");
        templateToPath.put("response.java.ftl", "src/main/java/" + packagePath + "/dto/" + entityName + "Response.java");
        templateToPath.put("page-result.java.ftl", "src/main/java/" + packagePath + "/dto/" + entityName + "PageResult.java");
        templateToPath.put("service.java.ftl", "src/main/java/" + packagePath + "/service/" + entityName + "Service.java");
        templateToPath.put("controller.java.ftl", "src/main/java/" + packagePath + "/rest/" + entityName + "Controller.java");
        templateToPath.put("project-module.java.ftl", "src/main/java/" + packagePath + "/error/" + entityName +
                "ProjectModule.java");
        templateToPath.put("error-code.java.ftl", "src/main/java/" + packagePath + "/error/" + entityName + "ErrorCode.java");
        templateToPath.put("exception.java.ftl", "src/main/java/" + packagePath + "/error/" + entityName + "Exception.java");
        templateToPath.put("test-application.java.ftl", "src/test/java/" + packagePath + "/GeneratedTestApplication.java");
        templateToPath.put("integration-test.java.ftl", "src/test/java/" + packagePath + "/" + entityName +
                "IntegrationTest.java");

        return renderAll(backendDir, templateToPath, model);
    }

    private List<Path> renderFrontend(Path frontendRoot, Path frontendOutputDir, Map<String, Object> model) {
        String tableCode = (String) model.get("tableCode");
        String className = (String) model.get("className");

        Map<String, String> templateToPath = new LinkedHashMap<>();
        templateToPath.put("api.ts.ftl", "src/api/" + tableCode + ".ts");
        templateToPath.put("route.ts.ftl", "src/router/modules/" + tableCode + ".ts");
        templateToPath.put("types.ts.ftl", "src/views/" + tableCode + "/utils/types.ts");
        templateToPath.put("hook.tsx.ftl", "src/views/" + tableCode + "/utils/hook.tsx");
        templateToPath.put("index.vue.ftl", "src/views/" + tableCode + "/index.vue");
        templateToPath.put("form-index.vue.ftl", "src/views/" + tableCode + "/form/index.vue");

        List<Path> files = new ArrayList<>();
        for (Map.Entry<String, String> entry : templateToPath.entrySet()) {
            Path target = frontendRoot.resolve(entry.getValue());
            files.add(renderTemplate(entry.getKey(), target, model));
        }
        return files;
    }

    private List<Path> renderAll(Path baseDir, Map<String, String> templateToPath, Map<String, Object> model) {
        List<Path> files = new ArrayList<>();
        for (Map.Entry<String, String> entry : templateToPath.entrySet()) {
            Path target = baseDir.resolve(entry.getValue());
            files.add(renderTemplate(entry.getKey(), target, model));
        }
        return files;
    }

    private Path renderTemplate(String templateName, Path target, Map<String, Object> model) {
        try {
            Template template = configuration.getTemplate(templateName);
            Files.createDirectories(target.getParent());
            try (Writer writer = Files.newBufferedWriter(target, StandardCharsets.UTF_8)) {
                template.process(model, writer);
            }
            return target.normalize();
        } catch (IOException e) {
            throw new MetaTableException("Failed to write generated file: " + target, e);
        } catch (freemarker.template.TemplateException e) {
            throw new MetaTableException("Failed to process template: " + templateName, e);
        }
    }

    public String renderToString(String templateName, Map<String, Object> model) {
        try {
            Template template = configuration.getTemplate(templateName);
            StringWriter writer = new StringWriter();
            template.process(model, writer);
            return writer.toString();
        } catch (IOException e) {
            throw new MetaTableException("Failed to render template to string: " + templateName, e);
        } catch (freemarker.template.TemplateException e) {
            throw new MetaTableException("Failed to process template: " + templateName, e);
        }
    }
}
