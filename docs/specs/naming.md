# Naming

## Services: `Sys*` vs domain

Two service styles coexist in `archforge-admin-user`.

| Style | Example | Meaning |
|-------|---------|---------|
| `Sys*` application/service | `SysUserService`, `SysMenuService`, `SysRoleService` | JPA-facing service around `Sys*` entities. Used by most current controllers. |
| Domain service | `UserService`, `User` + `UserPO` + `UserRepository` | DDD model. Prefer this for new work on a bounded context that has already been extracted. |

Rules:

- Do not add a new `Sys*` entity/service for a context that already has a domain type (`User`).
- Controllers stay thin: `UserController` talks to an application service (`AdminUserService` in `server-admin`, or a domain service). They do not become `SysUserController`.
- `Sys*` in a class name means "maps to a `sys_*` table", not "system module HTTP path".

## Tables

| Prefix | Owner | Examples |
|--------|-------|----------|
| `sys_` | Platform / admin-user / meta | `sys_user`, `sys_role`, `sys_menu`, `sys_dept`, `sys_dict_type`, `sys_meta_table` |
| `blog_` | Blog bounded context | `blog_article`, `blog_category` |

New tables keep the prefix of their bounded context. Do not create unprefixed platform tables.

## HTTP and types

- Paths: `/admin/{resource}`, `/web/{resource}` — see [api-path.md](api-path.md).
- Request/response types: `UserCreateRequest`, `UserDetailResponse`. No new bare `XxxDTO` / `*ItemDTO`.
- MapStruct: `XxxConvertor` (not `XxxMapper`).
- Gradle modules: `archforge-` prefix — see [directory.md](directory.md).
