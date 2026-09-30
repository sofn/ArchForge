package com.lesofn.archforge.meta.table.api.service;

import com.lesofn.archforge.meta.table.api.domain.MetaColumn;
import com.lesofn.archforge.meta.table.api.domain.MetaTable;
import java.util.List;
import java.util.Optional;

/**
 * Runtime read path for meta-table definitions, addressed by {@code tableCode}
 * (P3-4-3 F3, ADR-0008). Unpinned ({@code arch-forge.meta.source=db|shadow})
 * every lookup reads the meta repositories, so designer writes are visible at
 * once. Under {@code file} the startup applier calls {@link #pin()} right after
 * materializing the definition files: the DB mirror is frozen into an
 * immutable in-memory view, request-time lookups issue no definition SQL, and
 * each instance serves exactly the definitions it was deployed with.
 */
public interface MetaDefinitionRegistry {

    /**
     * A live table with its live columns in sort order. Pinned snapshots are
     * shared across requests — callers must treat both as read-only.
     */
    record TableSnapshot(MetaTable table, List<MetaColumn> columns) {
    }

    Optional<TableSnapshot> find(String tableCode);

    /** Number of live tables. */
    long count();

    /** Freeze the current DB mirror; later lookups never touch the meta tables. */
    void pin();
}
