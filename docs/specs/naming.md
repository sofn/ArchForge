# Naming

## Domain module layout

Every domain module (`admin-user`, `blog`, `meta-table`) has exactly two
top-level packages — `api` (published contract, `@NamedInterface` per
package) and `internal` (implementation). See
[ADR-0001](../adr/0001-api-internal-layout.md).

## Services

- Interface in `api.service`, implementation in `internal.service` — see
  [ADR-0005](../adr/0005-service-placement.md).
- `Sys*` in a class name means "maps to a `sys_*` table", not "system module
  HTTP path". `SysUserService` is the JPA-facing service over `sys_user`.
- Controllers stay thin: they talk to `api.service` interfaces (or web-layer
  assemblers in `server-admin`), never to `internal.*` classes — Modulith
  enforces this.
- One `EntityManagerFactory` per datasource group; entities carry domain
  behavior directly — see [ADR-0002](../adr/0002-single-emf-per-datasource.md).

## Tables

| Prefix | Owner | Examples |
|--------|-------|----------|
| `sys_` | Platform / admin-user / meta | `sys_user`, `sys_role`, `sys_menu`, `sys_dept`, `sys_dict_type`, `sys_meta_table` |
| `blog_` | Blog bounded context | `blog_article`, `blog_category` |

New tables keep the prefix of their bounded context. Do not create unprefixed platform tables.

## HTTP and types

- Paths: `/admin/{resource}`, `/web/{resource}` — see [api-path.md](api-path.md).
- Request/response types: `UserCreateRequest`, `UserDetailResponse`. No new bare `XxxDTO` / `*ItemDTO`
  (the 11 legacy `*DTO` classes are frozen — ArchUnit fails on new ones).
- MapStruct: `XxxConvertor` (not `XxxMapper`) — ArchUnit enforces.
- Gradle modules: `archforge-` prefix — see [directory.md](directory.md).
