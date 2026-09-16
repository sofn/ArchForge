# Module governance

Which Gradle module may exist, where it lives, and when it must leave the
monolith. Source of truth for the L1–L4 layering; enforced by Modulith
`@ApplicationModule` metadata + ArchUnit.

## Layers

```
L1 Kernel     archforge-common-* · archforge-infrastructure · archforge-starters/*
              → publishable, semantic version, strict backward compat
L2 Apps       archforge-server-admin :8080 · archforge-server-web :8081
              → the only bootable shells; assembly only, no business code
L3 Built-in   archforge-builtin/archforge-admin-user · archforge-builtin/archforge-meta-runtime
              · archforge-builtin/archforge-meta-designer
              → organizational capabilities (auth/dict/log/scheduler/modeling)
              → whitelist, capped (≤10), default-assembled
L4 Modules    archforge-module-*      ← flat at repo root, no group dir
              ⤷ framework-provided: archforge-module-cms · archforge-module-task
              ⤷ user-provided: archforge-module-payment, archforge-module-order, …
              → business domains (CRUD); default-assembled, uncapped
```

- `archforge-example/` is gone — every business domain is a flat
  `archforge-module-*`.
- `archforge-module-*` is the business-domain namespace; the prefix marks
  "business domain", not "optional" — **L4 is assembled by default**, same as
  L3. A user-repo module is assembled by its own `app` shell instead.

## Placement test (L3 vs L4)

| | L1/L3 framework built-in | L4 `archforge-module-*` |
|---|---|---|
| Count | few, **≤ 10** | many, 10–100+ |
| Home | this repo, ships with the framework | **the user's own repo** (or in-tree `archforge-module-*`) |
| Test | is it an **organizational** capability? (workflow/notify/approval/audit/permission/meta) | is it a **business** capability? (a CRUD domain) |
| Governance | strict: Modulith whitelist + ArchUnit + versioned | free: the framework only provides codegen |
| Assembly | default | default (same as L3) |

**Adding an L3 module requires review** (the cap exists). Business domains
always go `archforge-module-*` via codegen — never into `archforge-builtin/`.

## Design-time vs runtime (publish axis)

The L1–L4 layering is the **governance** axis (who may depend on whom). A second,
orthogonal axis decides **who ships to production** and **who is publishable**:

- `archforge-meta-runtime` — publishable runtime: meta definition model +
  repositories + dynamic CRUD/datascope. No codegen, no DDL generators, no web.
- `archforge-meta-designer` — design-time only, **never published**: codegen
  (`api/codegen/**`), DDL generators, schema diff, physical-table introspection,
  `MetaTableAdminService`/`Import`/`Migration*` surfaces. One-way dependency:
  designer → runtime, never reverse (ArchUnit `ARCH-014`).
- HTTP surface is gated: `MetaTableDesignerController` assembles only when
  `arch-forge.designer.enabled=true` (dev/test profiles set it; prod omits it).
- `server-admin` / `server-web` are **Reference Applications** — bootable shells
  that generate `spec/openapi.yaml` and host the integration/ArchUnit testbed.
  They are not "the framework itself"; physical extraction to an `examples/`
  repo is deferred until a real external consumer exists (P3).
- **Project Definition files** (P3-4): meta-table definitions have a file form —
  `project-definition/meta/<tableCode>.yaml`, contract
  `spec/schemas/meta/table.schema.json`. `archforge meta export|import` syncs
  DB ↔ YAML (`MetaTableDefinitionCodec`/`Service` in designer, strict parsing,
  dry-run diff by default, `removedColumns` = the only column delete). The DB is
  still the runtime truth; the file-first flip is a separate reviewed decision.

## Data layer

One `EntityManagerFactory` + one `PlatformTransactionManager`
(`common.persistence.JpaConfig`), bound directly to the dynamic-datasource
router. No per-module EMF, no `GroupDataSourceProxy`. Schema belongs to Flyway:
`ddl-auto: validate` everywhere; `spring.datasource.dynamic.strict: true` so an
undefined datasource name fails fast instead of silently falling back to
`master`.

Module migrations live in the module jar under `db/migration/<module>/`. The
module-aware `FlywayConfig` orchestrator runs `db/migration/__root/` first
(shared `flyway_schema_history`), then each module directory under its own
history table `flyway_schema_history_<module>` — module version numbers are
local and restart at V1.

## Microservice graduation criteria

Evaluate extracting a module to a standalone service when **any** holds:

1. the module needs **independent scaling** (it is the bottleneck);
2. the module needs an **independent release cadence**;
3. the module needs its **own database or credentials** (a security boundary,
   not a performance one);
4. the module's **change frequency/risk** is markedly higher than the rest.

Path: physically promote the module's `api` package into a
`archforge-module-xxx-api` Gradle module → turn `-biz` into a standalone jar →
add Spring Cloud. `@NamedInterface` already defines the contract surface, so
the promotion is mechanical (`-api`/`-biz` naming matches yudao).

## Rules enforced elsewhere

- `@ApplicationModule(id, type, allowedDependencies)` on every module root
  package; `server-admin` whitelists the L3/L4 `api` packages it consumes.
- ArchUnit `ARCH-*` rules (incl. `ARCH-014` designer boundary) +
  `ModulithIntegrationTest` assert the default assembly set
  (`admin-user`, `meta-table`, `cms`, `task`).
- `docs/specs/flyway.md` owns migration history policy.
