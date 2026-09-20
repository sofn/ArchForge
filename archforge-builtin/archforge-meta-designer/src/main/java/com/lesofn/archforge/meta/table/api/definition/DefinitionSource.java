package com.lesofn.archforge.meta.table.api.definition;

import java.util.Map;

/**
 * Source of meta-table definition YAML documents — file name (e.g.
 * {@code blog_category.yaml}) mapped to raw YAML text. Implementations read
 * from a filesystem directory or from classpath resources; the codec turns the
 * text into {@link TableDefinition}s.
 */
public interface DefinitionSource {

    /**
     * All {@code *.yaml} documents this source exposes, keyed by file name.
     * Order is sorted by file name so downstream processing is deterministic.
     */
    Map<String, String> load();
}
