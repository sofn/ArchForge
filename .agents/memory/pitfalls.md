# Pitfalls index — real failures this project has already hit

Only actual failures go here. Conventions and style rules belong in `docs/specs/` or `.agents/skills/`, not in pitfalls.

| Topic | File | What it covers |
|-------|------|----------------|
| Auth / sa-token | `pitfalls/auth.md` | wildcard matching, dead-code traps, session resolution |
| Build / Gradle | `pitfalls/build.md` | bootRun UP-TO-DATE, JVM flag order, running-jar overwrite |
| DB / Flyway | `pitfalls/db-flyway.md` | SCRAM verifier drift, history repair, password env |
| Meta-table | `pitfalls/meta-table.md` | import boundary cases, reserved prefixes |
