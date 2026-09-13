# Tasks: AI-harness P0

| # | Task | Files | Verify | Status |
|---|------|-------|--------|--------|
| T1 | Ban skip-annotations in test sigs | `config/forbiddenapis/forbidden-signatures-test.txt` | `forbiddenApisTest` + 2 negative probes (Java/Groovy both FAIL) | done |
| T2 | Anti-bypass policy in AGENTS.md | `AGENTS.md` | clause added under Static Analysis | done |
| T3 | JaCoCo per-module coverage floors | `build.gradle.kts` | measured aggregate 61.8%; floors wired into `check`; example-task SKIPPED non-blocking | done |
| T4 | CI startup-smoke job | `.github/workflows/ci.yml` | yaml lint OK; job = needs(build) → compose infra → jar → poll /actuator/health | done |
| T5 | Full build green + execution-log + commit/push | — | `./gradlew build` running | in_progress |
