package com.lesofn.archforge.meta.table.api.service;

import com.lesofn.archforge.meta.table.api.definition.DefinitionSource;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.jspecify.annotations.Nullable;

/**
 * DB ↔ YAML synchronization for meta-table definitions
 * ({@code <tableCode>.yaml}). Export is fidelity-checked; import is dry-run by
 * default. Keys present in a file are assertions, absent keys are unmanaged
 * (the DB keeps its default/previous value). Nothing is deleted implicitly —
 * {@code removedColumns} in the file is the only column delete. An applying
 * sync validates every table it touched and rolls back entirely on failure.
 */
public interface MetaTableDefinitionService {

    /**
     * Transaction-scoped PostgreSQL advisory lock key (ASCII "METADEF1") taken
     * by {@link #materialize} — visible as {@code locktype = 'advisory'} in
     * {@code pg_locks} while an instance waits at startup.
     */
    long APPLY_LOCK_KEY = 0x4D45544144454631L;

    /** What one import pass would change — populated identically in dry-run and apply modes. */
    record SyncReport(
            List<String> createdTables,
            List<String> updatedTables,
            List<String> newColumns,
            List<String> changedColumns,
            List<String> orphanColumns,
            List<String> removedColumns,
            List<String> orphanTables) {

        public boolean isEmpty() {
            return createdTables.isEmpty() && updatedTables.isEmpty() && newColumns.isEmpty() && changedColumns.isEmpty() &&
                    orphanColumns.isEmpty() && removedColumns.isEmpty() && orphanTables.isEmpty();
        }

        public List<String> lines() {
            List<String> out = new ArrayList<>();
            createdTables.forEach(c -> out.add("+ table " + c));
            updatedTables.forEach(c -> out.add("~ table " + c));
            newColumns.forEach(c -> out.add("+ column " + c));
            changedColumns.forEach(c -> out.add("~ column " + c));
            orphanColumns.forEach(c -> out.add("! orphan " + c + " (in DB, absent from file — kept)"));
            removedColumns.forEach(c -> out.add("- column " + c + " (removedColumns — soft-deleted)"));
            orphanTables.forEach(c -> out.add("! orphan table " + c + " (in DB, no definition file — kept)"));
            return out;
        }
    }

    /** Export non-deleted tables to {@code <dir>/<tableCode>.yaml}; returns written files. */
    List<Path> exportTo(Path dir, @Nullable String tableCode);

    /** {@link #syncFrom(DefinitionSource, String, boolean)} over a filesystem directory. */
    SyncReport syncFrom(Path dir, @Nullable String tableCode, boolean apply);

    /**
     * Import the source's definitions into the DB — a dry-run diff unless
     * {@code apply}. Orphan tables (live in the DB, no file) are only reported
     * for a full sync ({@code tableCode == null}).
     */
    SyncReport syncFrom(DefinitionSource source, @Nullable String tableCode, boolean apply);

    /**
     * File-mode startup apply ({@code arch-forge.meta.source=file}): a full
     * applying sync serialized across instances by a transaction-scoped
     * PostgreSQL advisory lock. Waits at most {@code lockTimeout} for a
     * concurrent applier, then fails — every instance finishes with the DB
     * mirror equal to its own definition files.
     */
    SyncReport materialize(DefinitionSource source, Duration lockTimeout);
}
