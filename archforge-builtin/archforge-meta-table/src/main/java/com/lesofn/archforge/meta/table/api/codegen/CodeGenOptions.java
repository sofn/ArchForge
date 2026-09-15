package com.lesofn.archforge.meta.table.api.codegen;

import java.nio.file.Path;
import lombok.Data;

@Data
@SuppressWarnings("NullAway.Init")
public class CodeGenOptions {
    private Path projectRoot;
    private Path backendOutputDir;
    private Path frontendOutputDir;
    private String basePath;
    private boolean overwrite;
}
