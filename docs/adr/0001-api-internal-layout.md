# 0001. Domain modules: api + internal only

Date: 2026-09-12
Status: accepted

## Context

Domain modules (`admin-user`, `blog`, `meta-table`) had grown extra trees —
`domain/`, `infrastructure/`, `internal/convert/` — beside `api`/`internal`,
producing two parallel persistence models in `admin-user` and leaking
`internal.*` types across module boundaries.

## Decision

Every domain module has exactly two top-level packages:

- `api` — the module's published contract. Each `api.*` package is declared
  `@NamedInterface` in its `package-info.java`; all modules are Modulith
  `Type.CLOSED` and other modules depend via `module::*` in
  `allowedDependencies`.
- `internal` — implementation. Invisible to other modules; enforced by
  `ModulithIntegrationTest.modulesAreValid`.

No third top-level package may be created. An ArchUnit rule in
`server-admin`'s `ArchitectureTest` fails the build if `domain` or
`infrastructure` packages reappear inside domain modules.

## Consequences

- New types choose a side deliberately: contract shape → `api`, wiring → `internal`.
- Anything another module needs must live under `api` (and its package must
  declare `@NamedInterface` — package-level declarations do not recurse into
  sub-packages).
- The collapsed DDD trees stay collapsed; see ADR-0002.

## Alternatives considered

- Keep `domain/`+`infrastructure/` beside `api/`+`internal/` — rejected: it
  produced duplicate entities over the same tables and an orphaned
  EntityManagerFactory.
- Keep modules `Type.OPEN` — rejected: it hid the `internal` leak that this
  layout exists to prevent.
