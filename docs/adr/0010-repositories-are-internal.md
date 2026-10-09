# 0010. Repositories are internal; other modules read through api services

Date: 2026-10-08
Status: accepted

## Context

Spring Data repositories sat in `api.dao` packages exposed as Modulith named interfaces (31 files across
admin-user, meta-runtime and cms; ADR-0002 said "Repository = `api.dao` Spring-Data interface"). server-admin and
server-web injected them directly — for dashboard counts, latest notices, job logs and scheduler reconciliation — so
every table layout and query shape was part of the module's public contract, and nothing stopped new callers from
writing to another module's tables.

## Decision

Repositories and their custom fragments (`*RepositoryCustom`, `*RepositoryImpl`) live in the owning module's
`internal.dao` package. A caller outside the module uses an `api` service method that says what it needs
(`SysUserService.countNotDeleted()`, `SysNoticeService.latestPublished(limit)`, `SysJobLogService.record(log)`,
`CmsArticleService.latest(limit)`, …). Designer classes share the `meta.table` namespace with the runtime and keep
using its repositories; the designer's former api classes are split into api interfaces + internal implementations.

## Consequences

- ARCH-015 (server-admin `ArchitectureTest`) fails on a repository outside `..internal..`; ARCH-102 (server-web)
  fails on any server-web dependency on a module's internals; Modulith `verify()` rejects server-admin access.
- A new cross-module read is a new api method on the owning module, written next to the repository it uses.
- Test code may still use another module's repositories for fixtures (rules check production classes only).
- ADR-0002's "Repository = `api.dao`" bullet is superseded by this record.

## Alternatives considered

- Freeze the existing 31 and only forbid new ones — rejected: leaves the cross-module coupling in place.
- A generic query facade per module (pass-through `findAll(spec, page)`) — rejected: re-exports the table layout,
  which is what this decision removes.
