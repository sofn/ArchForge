# Pitfalls — meta-table module

- **Platform-table import gap** (fixed 6ad81904): `validateTableCode` blocked `sys_/qrtz_/pg_/sql_/information_schema_` but NOT `meta_`/`flyway_` — the module's own prefix must stay legal for `create()`, so a separate `PLATFORM_TABLE_PREFIXES` blacklist guards import (both compatibility reason AND an independent fail-fast in `importTable` — do not rely on the preview path alone).
- **ArchUnit naming rule `noMapperNamedClasses`** rejects classes named `*Mapper` outside the mapper package — `PgTypeMapper` had to become `PgTypeMapping`.
- **SpotBugs NP on repeated getter calls**: a second `getX()` on a nullable-returning method defeats the first null-check → hoist to a local.
- **Imported tables register with `tablePrefix=""` and `tableCode=physical name`** to bypass `create()`'s `meta_` prefixing and DDL generation.
- **Audit columns are never registered as MetaColumn** on import; `NOT NULL` without default → `required=true`; physical defaults are not recorded in metadata.
