package com.lesofn.archforge.meta.table.internal.generator;

import java.nio.file.Path;
import java.util.List;
import lombok.Data;

@Data
public class GeneratedResult {
    private Path backendDir;
    private Path frontendDir;
    private List<Path> files;
}
