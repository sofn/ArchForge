package com.lesofn.archforge.meta.table.internal.generator.extension;

import com.lesofn.archforge.meta.table.api.domain.MetaColumn;
import com.lesofn.archforge.meta.table.api.domain.MetaTable;
import com.lesofn.archforge.meta.table.internal.generator.CodeGenOptions;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CodeGenExtensionRegistry {

    private final List<CodeGenExtension> extensions = new ArrayList<>();

    public CodeGenExtensionRegistry() {
    }

    @Autowired(required = false)
    public CodeGenExtensionRegistry(List<CodeGenExtension> extensions) {
        if (extensions != null) {
            this.extensions.addAll(extensions);
        }
    }

    public void register(CodeGenExtension extension) {
        this.extensions.add(extension);
        this.extensions.sort(Comparator.comparingInt(CodeGenExtension::order));
    }

    public void beforeBuildModel(MetaTable table, List<MetaColumn> columns, CodeGenOptions options, Map<String, Object> model) {
        for (CodeGenExtension e : extensions) {
            e.beforeBuildModel(table, columns, options, model);
        }
    }

    public void afterBuildModel(MetaTable table, List<MetaColumn> columns, CodeGenOptions options, Map<String, Object> model) {
        for (CodeGenExtension e : extensions) {
            e.afterBuildModel(table, columns, options, model);
        }
    }

    public Map<String, String> extraBackendTemplates(Map<String, Object> model) {
        Map<String, String> result = new LinkedHashMap<>();
        for (CodeGenExtension e : extensions) {
            result.putAll(e.extraBackendTemplates(model));
        }
        return result;
    }

    public Map<String, String> extraFrontendTemplates(Map<String, Object> model) {
        Map<String, String> result = new LinkedHashMap<>();
        for (CodeGenExtension e : extensions) {
            result.putAll(e.extraFrontendTemplates(model));
        }
        return result;
    }

    public void postProcess(Path backendDir, Path frontendDir, Map<String, Object> model, List<Path> generatedFiles) {
        for (CodeGenExtension e : extensions) {
            e.postProcess(backendDir, frontendDir, model, generatedFiles);
        }
    }
}
