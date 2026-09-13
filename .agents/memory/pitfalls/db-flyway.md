# Pitfalls — PostgreSQL / Flyway

- **SCRAM verifier drift**: the `archforge` role password can diverge from the container env password (e.g. after volume reuse). `psql` inside the container uses local trust auth and will NOT reveal the mismatch — test over TCP to the container IP to exercise SCRAM. Fix: `ALTER USER ... PASSWORD '<env value>'`.
- **Flyway checksum failures from ghost history**: deleted migrations (e.g. old quartz 4.1/5) leave `flyway_schema_history` rows → validation fails. Delete the stale history rows (equivalent to `flyway repair`; V23 does the same in-tree).
- **Ordering**: kill running app → fix DB → restart. The app retries connections and can hit Flyway timeout while you are mid-fix.
