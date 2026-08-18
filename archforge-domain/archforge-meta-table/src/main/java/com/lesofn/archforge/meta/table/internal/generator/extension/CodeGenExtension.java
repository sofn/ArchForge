package com.lesofn.archforge.meta.table.internal.generator.extension;

import com.lesofn.archforge.meta.table.api.domain.MetaColumn;
import com.lesofn.archforge.meta.table.api.domain.MetaTable;
import com.lesofn.archforge.meta.table.internal.generator.CodeGenOptions;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public interface CodeGenExtension {

    String getName();

    default int order() {
        return 0;
    }

    default void beforeBuildModel(MetaTable table, List<MetaColumn> columns, CodeGenOptions options,
            Map<String, Object> model) {
    }

    default void afterBuildModel(MetaTable table, List<MetaColumn> columns, CodeGenOptions options, Map<String, Object> model) {
    }

    default Map<String, String> extraBackendTemplates(Map<String, Object> model) {
        return Collections.emptyMap();
    }

    default Map<String, String> extraFrontendTemplates(Map<String, Object> model) {
        return Collections.emptyMap();
    }

    default void postProcess(Path backendDir, Path frontendDir, Map<String, Object> model, List<Path> generatedFiles) {
    }
}
