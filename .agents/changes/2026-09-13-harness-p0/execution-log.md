# Execution Log: AI-harness P0

Append-only. One block per work round.

## 2026-09-13 — round 1

**Changed**: `config/forbiddenapis/forbidden-signatures-test.txt`, `AGENTS.md`,
`.github/workflows/ci.yml`, `build.gradle.kts`, `.agents/changes/2026-09-13-harness-p0/*`

**Commands**:

```
$ grep -rn "@Disabled|@Ignore|@PendingFeature|@IgnoreRest|@IgnoreIf" src/** # (all modules)
  → zero usage — clean baseline

$ ./gradlew :archforge-server-admin:forbiddenApisTest :archforge-server-web:forbiddenApisTest
  → PASS (baseline clean)

# negative probe — Java
$ add ProbeDisabledTest.java with @Disabled → forbiddenApisTest
  → FAIL: ForbiddenApiException ✓ (gate works)

# negative probe — Groovy/Spock
$ add ProbeIgnoreSpec.groovy with @spock.lang.Ignore → forbiddenApisTest
  → FAIL: ForbiddenApiException ✓ (bytecode scan covers Groovy)

$ ./gradlew jacocoAggregateReport   # measurement run (clean build)
  → aggregate line coverage 61.8% (6914/11189)
```

**Measured per-module coverage** (→ floors set at ~2-3pp below):

```
cli 16.9 · common-base 48.7 · common-error 44.6 · common-jpa 51.8
admin-user 5.7 · blog 31.1 · meta-table 66.7 · infrastructure 22.1
server-admin 53.5 · server-web 63.1 · cache 51.5 · lock 34.0
redisson 87.5 · request-log 56.1 · trace 95.0
```

```
$ ./gradlew :meta-table:jacocoTestCoverageVerification :server-admin:… :example-task:…
  → meta-table PASS (0.64 floor) · server-admin PASS (0.51) · example-task SKIPPED (no exec data, non-blocking) ✓
```

**Findings**:
- `org.junit.Ignore` (JUnit4) NOT on classpath → signature rejected at parse time, dropped it
- `tasks.named("…")` returns untyped TaskProvider — needed
  `named<JacocoCoverageVerification>` for DSL methods (script compile error otherwise)
- Probe `.class` files survive source deletion — stale compiled classes would make
  `forbiddenApisTest` fail permanently; `clean` required after probes
- First test run after `git reset --hard` produced 41 `NoClassDefFoundError`
  (stale incremental state) — resolved by `./gradlew clean`, not a real regression

**Warnings**: 3 pre-existing checkstyle `MissingSwitchDefault` warnings — non-blocking baseline.

**Skipped**: `verify` aggregate task + arch-rule IDs (P1), golden paths (P1),
`continue-on-error` removal on contract-consistency step (needs spec reconciliation first).

**Final gate**:

```
$ ./gradlew spotlessApply build --no-daemon
  → BUILD SUCCESSFUL in 3m 47s — jacocoTestCoverageVerification executed on
    all 15 floored modules inside `check`; example-task SKIPPED (no exec data)
```

## 2026-09-13 — round 2 (P1: unified verify + rule IDs)

**Changed**: `build.gradle.kts` (verify task), both `ArchitectureTest.java`
(rule IDs via `.because("ARCH-0xx")`), `AGENTS.md` (verify documented in
per-edit protocol)

**Commands**:

```
$ ./gradlew :server-admin:test :server-web:test --tests '*ArchitectureTest*'
  → PASS (because() DSL valid)

$ ./gradlew verify --no-daemon
  → BUILD SUCCESSFUL in 3m41s, normalized banner printed:
    ARCHFORGE VERIFY
    [PASS] compile + spotless
    [PASS] checkstyle + errorprone/nullaway + spotbugs
    [PASS] forbiddenapis (incl. test-skip annotation ban)
    [PASS] unit + integration tests
    [PASS] archunit contract + coverage floors
    Result: PASS
```

**Findings**:
- `-Ptags` / `-PexcludeTags` machinery already existed — `verify -PexcludeTags=slow`
  covers Docker-less mode with no new property (all Docker-dependent tests are
  @Tag("slow"); contract tests like ArchUnit stay in)
- First verify run caught a real spotless violation in my because() lines —
  the harness eating its own cooking; spotlessApply fixed
- Rule IDs: ARCH-001..010 (admin shared space), ARCH-101+ (server-web own);
  shared rules reuse the same ID across modules (ARCH-004 controllers,
  ARCH-009 *Mapper naming appear in both)
