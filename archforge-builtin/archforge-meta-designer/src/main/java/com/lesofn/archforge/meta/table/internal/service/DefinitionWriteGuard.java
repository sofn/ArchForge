package com.lesofn.archforge.meta.table.internal.service;

import com.lesofn.archforge.meta.table.api.errors.MetaTableErrorCode;
import com.lesofn.archforge.meta.table.api.errors.MetaTableException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Rejects designer/import definition writes under
 * {@code arch-forge.meta.source=file} — the definition YAML is the only write
 * path then (P3-4-3 F2); {@code MetaTableDefinitionService} materializes it.
 */
@Component
@RequiredArgsConstructor
class DefinitionWriteGuard {

    private final Environment environment;

    void check() {
        if ("file".equalsIgnoreCase(environment.getProperty("arch-forge.meta.source"))) {
            throw new MetaTableException(MetaTableErrorCode.META_DEFINITION_FILE_MANAGED);
        }
    }
}
