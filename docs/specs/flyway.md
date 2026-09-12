# Flyway history gaps

`archforge-common/archforge-common-jpa/src/main/resources/db/migration/` currently has
**V1–V4, V6–V18, V20–V25**. There is **no V5 and no V19** in the repository.

Migrations live in `common-jpa` so `server-admin` and `server-web` share the same
`classpath:db/migration` — both apps migrate the shared `archforge_user` database
(Flyway serialises concurrent runs via `flyway_schema_history`).

## Why

- **V4.1 / V5** were the Quartz-era schema. They were deleted when scheduling
  moved to db-scheduler. `V23__replace_quartz_with_db_scheduler.sql` creates the
  new tables on a fresh database and, on a legacy database, drops `qrtz_*` /
  `sys_quartz_*` and deletes the stale V4.1/V5 rows from `flyway_schema_history`.
- **V19** was removed from the tree the same way (gap, not a pending file).

## Configuration: `arch-forge.flyway.*`

Spring Boot 4 ships no Flyway auto-configuration (and flyway-core carries none),
so `spring.flyway.*` keys bind to nothing in this project. Migration is wired by
`com.lesofn.archforge.common.persistence.FlywayConfig`, gated on
`arch-forge.flyway.enabled` and configured via `arch-forge.flyway.*`
(`FlywayProperties`). EMF ordering is enforced by
`FlywayDependencyBeanFactoryPostProcessor` (covers `userDbConfig`, `blogDbConfig`,
`metaTableDbConfig`).

`application-prod.yaml` / `application-staging.yaml` (both servers) set:

```yaml
arch-forge:
  flyway:
    enabled: true
    ignore-migration-patterns: "*:missing"
```

That lets an **existing** database whose `flyway_schema_history` still lists
deleted versions start up. It also **hides a future accidental delete** of a
migration file — Flyway will treat it as "missing" instead of failing.

Flyway's ignore patterns are `type:state` pairs — they cannot whitelist specific
versions. The exit criterion for dropping `*:missing` is therefore **every
environment has migrated past V23** (V23 deletes the stale history rows, after
which "missing" is never reported). Remove the key at that point; do not widen
the pattern before it.

When adding a new version, never reuse V5 or V19; next file is **V26**.
