# Spec: AI-harness P0 — anti-bypass, coverage floor, CI startup smoke

Date: 2026-09-13 · Status: approved (user-selected from audit report)

## Background & Goals

Audit `codeplans/reports/2026-09-13-archforge-ai-harness-audit.md` identified three
P0 gaps in the engineering harness: gates exist but can be silently bypassed,
coverage is advisory-only, and startup smoke is manual-only.

## Current State (verified)

- `config/forbiddenapis/forbidden-signatures-test.txt` — intentionally empty
  (only bundled sigs apply to tests). No ban on `@Disabled`/`@Ignore`.
- `build.gradle.kts` — `jacoco` applied to all subprojects; `jacocoTestReport`
  wired, but **no `jacocoTestCoverageVerification`** anywhere. CI posts a
  PR diff-coverage gate (`diff-cover --fail-under 60`, changed files only);
  no overall floor on push/main.
- `.github/workflows/ci.yml` — `build` + contract gates + dependency-check.
  No job boots the application. `bootRun` smoke is a manual AGENTS.md item.
- `continue-on-error: true` on the spec-consistency step is a documented
  grace-period exception (contract reconciliation in progress).
- App smoke needs: PG+Redis on localhost (`docker/docker-compose.infra.yml`),
  `DB_PASSWORD` env, dev profile, `/actuator/health` exposed (application.yaml).

## Scope

### In
- Ban permanent test-skip annotations in compiled test classes
  (`@Disabled` JUnit5, `@Ignore` JUnit4, `spock.lang.Ignore`/`@IgnoreRest`)
  via forbiddenapis test signatures — bytecode-level, covers Groovy specs.
- AGENTS.md anti-bypass policy clause (gates must not be weakened; exceptions
  require ADR + human approval).
- Per-module `jacocoTestCoverageVerification` ratchet floors (set at
  current-ε of measured coverage), wired into `check`.
- CI `startup-smoke` job: compose infra → `java -jar` server-admin → poll
  `/actuator/health` until UP or timeout.

### Out
- `verify` aggregate task + normalized failure output (P1, separate change)
- Golden Path curation, change budget, arch DSL (P1/P2)
- Removing the documented `continue-on-error` grace exception (needs contract
  reconciliation first — tracked, not this change)

## Business Rules

- A permanently-skipped test must fail the build in every module.
- Coverage floor must not go DOWN without editing the floor (ratchet).
- Weakening any gate (thresholds, suppressions, `continue-on-error`, CI
  changes) requires an ADR + human approval — stated in AGENTS.md.

## Risks

- Coverage floor set too high → flaky blocks. Mitigate: measure real values,
  floor = measured − margin, exclude generated classes (metamodel, MapStruct).
- Smoke job needs Docker-in-GHA — compose is fine on ubuntu-latest.
- forbiddenapis Groovy coverage: bytecode scan covers compiled specs — verify
  with a deliberately-disabled spec during implementation.

## HARD-GATE Confirmations

- [x] No contract change (openapi.yaml untouched)
- [x] No deleted-API resurrection
- [x] CI changes are additive (new job), no gate weakened
