# Pitfalls index — real failures this project has already hit

Only actual failures go here. Conventions and style rules belong in `docs/specs/` or `.agents/skills/`, not in pitfalls.

| Topic | File | What it covers |
|-------|------|----------------|
| Auth / sa-token | `pitfalls/auth.md` | wildcard matching, dead-code traps, session resolution, data scope fail-closed, static DAO vs paused test contexts, request-wrapper chains |
| Build / Gradle | `pitfalls/build.md` | bootRun UP-TO-DATE, JVM flag order, running-jar overwrite, undeclared test inputs vs build cache, literal unresolved placeholders, regex recursion on long input, CLI processes as scheduler nodes |
| DB / Flyway | `pitfalls/db-flyway.md` | SCRAM verifier drift, history repair, password env |
| Meta-table | `pitfalls/meta-table.md` | import boundary cases, reserved prefixes, REFERENCE targets, JSON binding, fail-closed ENUM, unique violations |
