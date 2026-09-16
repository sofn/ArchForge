# AGENTS.md

## Project

`%NAME%` — Spring Boot 4 / JDK 25 application built on the ArchForge release
set (`com.lesofn.archforge` BOM). Business modules live in flat
`archforge-module-*` directories and are auto-included by
`settings.gradle.kts`.

## Commands

```bash
./gradlew build          # compile + test
./gradlew bootRun        # run the app
archforge module new x   # scaffold archforge-module-x
```

## Layout

```
src/main/java/   application code
archforge/       Project Definition (meta/ api/ permissions/ enums/) — file-first source of truth
spec/            API contract (openapi.yaml, schemas/)
```

## Rules

- Do NOT depend on ArchForge internals beyond the published release set —
  `com.lesofn.archforge:*` coordinates only, never `project(":archforge-…")`.
- Module boundaries: `api/` for cross-module surface, `internal/` stays private.
