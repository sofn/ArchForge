# Pitfalls — meta-table module

- **Platform-table import gap** (fixed 6ad81904): `validateTableCode` blocked `sys_/qrtz_/pg_/sql_/information_schema_` but NOT `meta_`/`flyway_` — the module's own prefix must stay legal for `create()`, so a separate `PLATFORM_TABLE_PREFIXES` blacklist guards import (both compatibility reason AND an independent fail-fast in `importTable` — do not rely on the preview path alone).
- **ArchUnit naming rule `noMapperNamedClasses`** rejects classes named `*Mapper` outside the mapper package — `PgTypeMapper` had to become `PgTypeMapping`.
- **SpotBugs NP on repeated getter calls**: a second `getX()` on a nullable-returning method defeats the first null-check → hoist to a local.
- **Imported tables register with `tablePrefix=""` and `tableCode=physical name`** to bypass `create()`'s `meta_` prefixing and DDL generation.
- **Audit columns are never registered as MetaColumn** on import; `NOT NULL` without default → `required=true`; physical defaults are not recorded in metadata.
- **Never validate SQL fragments with a blacklist regex**: the old display-expression check used `String.matches(".*SELECT.*")`; `.` does not match a newline, so `ref.id ||\n(SELECT …)` passed (2026-10, fixed by the `DisplayExpression` whitelist grammar). Same trap for any `matches(".*X.*")` guard.
- **`validateTableCode(code)` is not a physical-name check**: the table is `prefix + code`, and `CREATE TABLE IF NOT EXISTS` silently *adopts* an existing relation (`sys_` + `menu` → the system table, later `DROP … CASCADE`). Validate the whole name (`validatePhysicalTableName`) and refuse existing relations in `create`.
- **Unquoted DDL fragments must be re-printed from a parsed value**, not passed through (`DEFAULT <value>` for numbers/arrays). Quoted ones must double `'`.
- **A REFERENCE column could join any physical table** (fixed 2026-10): pointing `referenceColumn` at `id` skipped every
  check, so `referenceTable: sys_user` + `ref.password` listed password hashes. The target must be the table itself or a
  registered meta table (design time), and `ReferenceDisplayBuilder` never joins a platform table (runtime, for legacy rows).
- **Row values are not strings**: `convertValue` used `value.toString()` for JSON/GEO, so a JSON array/object from the API
  reached PostgreSQL as `[a, b]` / `{k=v}` (500). Serialize with Jackson (`toJsonString`).
- **Validation must fail closed when its reference data is missing**: an ENUM column whose dictionary did not exist
  accepted any value. Same rule as the data scope — no allow-list means nothing is allowed.
- **Unique violations are input errors**: translate `DuplicateKeyException` (index `uq_<physical table>_<key>`) into
  `META_COLUMN_VALUE_INVALID` naming the column — never a 500 carrying the constraint, key value and SQL state.
