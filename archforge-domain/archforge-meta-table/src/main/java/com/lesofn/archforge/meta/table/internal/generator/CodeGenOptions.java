package com.lesofn.archforge.meta.table.internal.generator;

import java.nio.file.Path;
import lombok.Data;

@Data
public class CodeGenOptions {
    private Path projectRoot;
    private Path backendOutputDir;
    private Path frontendOutputDir;
    private String basePath;
    private boolean overwrite;
}
