# 0002. One EntityManagerFactory per datasource group

Date: 2026-09-12
Status: accepted

## Context

`admin-user` ran two EMFs over the same `GroupDataSourceProxy(ds,"user")`:
`SysUser` and `UserPO` both mapped `sys_user`, forcing a
`SysUser → UserAggregate → UserPO` three-hop conversion on every read/write.
The DDD-side `UserDomainService` had zero callers and its five domain events
were never published.

## Decision

One datasource group = one `EntityManagerFactory` + one `PersistenceUnit` +
one transaction manager. `*DbConfig` scans exactly one entity package. JPA
entities carry domain behavior directly (rich entities, e.g.
`SysUser.completeProfile`), not via a parallel aggregate model.

## Consequences

- No second persistence context can silently diverge — a single table is
  mapped by a single entity.
- Repository = `api.dao` Spring-Data interface; services call it directly.
- Re-adding a PO/aggregate layer for the same table is a violation.

## Alternatives considered

- Full migration to aggregates+POs — rejected: an XL rewrite whose only
  payoff was paying the conversion tax at every call site.
- Keep both EMFs but pick one per call — rejected: same table in two
  persistence contexts means dirty-check divergence; this was a bug farm,
  not a choice.
