# 0007. Meta definitions: file-first write authority via startup materialization

Date: 2026-09-30
Status: accepted

## Context

Meta-table definitions (`sys_meta_table` / `sys_meta_table_column`) were
edited only through the designer UI, so the DB was the source of truth.
P3-4 gave them a reviewable file form (`project-definition/meta/*.yaml`) with
an export/import channel. F1 added a shadow diff. The flip review
(`codeplans/ArchForge/2026-09-16-meta-source-of-truth-flip.md`) considered
three options: tier A (materialize files into the DB at startup, read path
unchanged), tier B (runtime file registry, REST identity by `tableCode`) and
tier C (dual write). It chose A first, with B reviewed separately.

## Decision

`arch-forge.meta.source` selects write authority:

- **db** (default): the DB is the truth.
- **shadow**: startup diff, WARN only.
- **file**: the YAML is the truth. `MetaTableFileSourceApplier` (an
  `ApplicationRunner`, `@Order(1)`) materializes it into the DB mirror at
  startup. It runs in one transaction behind a PostgreSQL advisory lock
  (`APPLY_LOCK_KEY`) that it waits for up to
  `arch-forge.meta.apply-lock-timeout`. Any failure aborts startup.
  Designer/import writes are rejected with `10415`, and
  `EnumOptionsMigrationRunner` is not assembled.

The sync semantics apply to every apply path, including `archforge meta import --apply`:

- Keys present in a file are assertions; absent keys are unmanaged.
- The apply mutates the managed rows in place, so audit fields and `version` survive.
- Every touched table is validated with `MetaTableValidator`, and a failure
  rolls back the whole sync.
- A soft-deleted code is revived instead of re-inserted.
- Nothing is deleted implicitly: `removedColumns` is the only column delete,
  and orphan columns and tables are only reported.

Definition source: `definition-dir`, then walk-up discovery, then the packaged
`classpath:archforge/meta/` mirror.

## Consequences

- Hand-written partial files converge: applying twice writes nothing, so
  idempotency comes from the diff and needs no hash/audit table.
- Physical DDL stays with Flyway. A file-defined table needs its migration;
  the materializer writes definition rows only.
- No profile defaults to `file`. Switching an environment is an ops decision;
  rolling back is `source=db`.
- Multi-instance boots are serialized, and each instance leaves with the
  mirror equal to its own files.

## Alternatives considered

- Per-file content hash for idempotency: needs a new table and misses edits
  made on the DB side, which the diff catches.
- `pg_try_advisory_lock` + skip (the review's default): an instance that skips
  boots without checking the mirror against its own files.
- Tier C dual write: large inconsistency window, unclear rollback.
