# Flyway history gaps

`archforge-server-admin/src/main/resources/db/migration/` currently has
**V1–V4, V6–V18, V20–V23**. There is **no V5 and no V19** in the repository.

## Why

- **V4.1 / V5** were the Quartz-era schema. They were deleted when scheduling
  moved to db-scheduler. `V23__replace_quartz_with_db_scheduler.sql` creates the
  new tables on a fresh database and, on a legacy database, drops `qrtz_*` /
  `sys_quartz_*` and deletes the stale V4.1/V5 rows from `flyway_schema_history`.
- **V19** was removed from the tree the same way (gap, not a pending file).

## `ignore-migration-patterns: "*:missing"`

`application-prod.yaml` and `application-staging.yaml` set:

```yaml
spring.flyway.ignore-migration-patterns: "*:missing"
```

That lets an **existing** database whose `flyway_schema_history` still lists
deleted versions start up. It also **hides a future accidental delete** of a
migration file — Flyway will treat it as "missing" instead of failing.

Do **not** widen this further. When adding a new version, never reuse V5 or V19;
next file is **V24**. Tightening the pattern to an explicit allow-list
(e.g. only the known deleted checksums) is a follow-up, not a silent drop of
the ignore on production DBs that still have the old history rows.
