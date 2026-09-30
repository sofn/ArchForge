# 0008. Meta runtime: definition registry + `tableCode` as the public identity

Date: 2026-09-30
Status: accepted

## Context

The runtime CRUD path (`MetaTableCrudServiceImpl`, data exporter/importer)
queried `sys_meta_table` + columns on every request, and every
`/admin/meta-table/**` endpoint addressed tables by the DB auto-increment `id`.
Codegen compiled that id into generated controllers (`TABLE_ID = 42L`), and
those ids differ between environments. Under file-first write authority
(ADR-0007) the files carry no id at all. This is tier B of the flip review;
it was accepted into the same change as F2 on 2026-09-30.

## Decision

- meta-runtime owns `MetaDefinitionRegistry` (`find(tableCode)`, `count()`, `pin()`):
  - `db`/`shadow`: a repository pass-through with no cache, so designer writes
    are visible at once.
  - `file`: the startup applier calls `pin()` after materializing. The DB
    mirror is frozen into an immutable in-memory view, request-time lookups
    run no definition SQL, and each instance serves the definitions it
    shipped with.
- The snapshot is taken from the materialized mirror, not parsed from YAML
  directly. Field defaults therefore match `db` mode exactly (the DB column
  defaults).
- `tableCode` is the public identity:
  - `MetaTableCrudService` takes `String tableCode`.
  - Every `/admin/meta-table/{id}` path is now `/admin/meta-table/{tableCode}`.
  - Designer endpoints resolve the code through `MetaTableAdminService.findByCode`.
  - Generated controllers embed `TABLE_CODE`.

  The code is immutable after creation, and responses still carry `id` as information.
- `pin()` requires the blocking apply lock from ADR-0007. With a try-lock, a
  skipping instance would pin a pre-apply mirror until its next restart.

## Consequences

- Breaking REST change, shipped in the same batch as the regenerated
  `spec/openapi.yaml`, ArchForgeAdmin (`src/api/metaTable.ts`, meta-table
  views) and ArchForgeWeb types.
- Pinned snapshots are shared. Runtime code must never mutate definition
  entities; the importer now swaps REFERENCE → INTEGER on copies.
- In file mode a definition change needs a restart, which is the documented
  file-first contract. A reload endpoint is not provided.
- The DB mirror stays: it is the materialization target, the FK anchor of
  `sys_meta_table_migration`, and the source for tools. Dropping it is not planned.

## Alternatives considered

- Pure YAML registry without the mirror: field defaults would diverge from
  `db` mode and designer/tool paths would lose ids. It stays tempting for
  generated projects without designer. Revisit it together with that item.
- Caching registry in `db` mode: needs invalidation on every designer write
  for no measured gain.
- Accept both id and code in the path (dual addressing): two identities on one
  resource, and ids keep leaking into clients.
