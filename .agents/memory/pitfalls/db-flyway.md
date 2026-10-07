# Pitfalls — PostgreSQL / Flyway

- **SCRAM verifier drift**: the `archforge` role password can diverge from the container env password (e.g. after volume reuse). `psql` inside the container uses local trust auth and will NOT reveal the mismatch — test over TCP to the container IP to exercise SCRAM. Fix: `ALTER USER ... PASSWORD '<env value>'`.
- **Flyway checksum failures from ghost history**: deleted migrations (e.g. old quartz 4.1/5) leave `flyway_schema_history` rows → validation fails. Delete the stale history rows (equivalent to `flyway repair`; V23 does the same in-tree).
- **Ordering**: kill running app → fix DB → restart. The app retries connections and can hit Flyway timeout while you are mid-fix.
- **Never flatten `__root` and module migration dirs into one Flyway run**: `__root/V1` and `cms/V1` collide ("Found more than one migration with version 1"). Gradle: `flywayMigrateAll` = `__root` + one `flywayMigrate<Module>` per module with `table = flyway_schema_history_<module>`; tests: use `FlywayConfig.FlywayModuleOrchestrator`. `FlywayMigrateTask.group` is Flyway's own boolean — set the Gradle group via `(this as Task).group`.
- **Gradle's flyway plugin defaults the password to the literal `archforge`**; the CLI must pass `DB_PASSWORD`/`DB_USERNAME` (`cli.db.FlywayMigration`), otherwise a generated password fails only in the Gradle step.
- **Legacy-DB simulations must stop at the migration under test** (`.target("23")`): later migrations assume the full V1–V22 schema the fixture does not recreate.
