# 0011. sa-token instead of Spring Security + JWT

Date: 2026-10-08 (decision taken 2026-08; moved here from the docs site's ADR 0002)
Status: accepted

## Context

Typical Java admin templates ship a hand-written JWT filter chain. ArchForge needs two isolated login realms
(admin and C-end) with annotation-level permissions that match `sys_menu.permission`, and a mental model small
enough that coding agents get it right. With a JWT chain, the second realm grows a second filter copy and token
revocation needs its own store anyway.

## Decision

Use **sa-token** with opaque tokens stored in Redis: `StpAdminUtil` for server-admin, `StpWebUtil` for server-web,
`Authorization: Bearer <token>`, permissions via `@SaCheckPermission(value, type = StpAdminUtil.TYPE)`.

## Consequences

- Logout and kick-out are a Redis delete; there is no JWT signing key (`JWT_SECRET` was removed as dead config).
- Every admin handler declares its permission and every checked permission must be grantable through a menu or
  button — enforced by `AdminHandlerPermissionCoverageTest` and `PermissionSeedConsistencyIntegrationTest`.
- Spring Security is not on the classpath; do not add it for authentication.

## Alternatives considered

- Spring Security + JWT — rejected: a filter chain per realm, revocation still needs a server-side store.
- Spring Security sessions — rejected: two isolated realms in one app are awkward, and the agent-facing model grows.
