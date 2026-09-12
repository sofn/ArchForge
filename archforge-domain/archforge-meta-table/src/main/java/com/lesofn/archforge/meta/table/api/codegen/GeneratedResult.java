package com.lesofn.archforge.meta.table.api.codegen;

import java.nio.file.Path;
import java.util.List;
import lombok.Data;

@Data
@SuppressWarnings("NullAway.Init")
public class GeneratedResult {
    private Path backendDir;
    private Path frontendDir;
    private List<Path> files;
}
