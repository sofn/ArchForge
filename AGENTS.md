# AGENTS.md

## Git Commit Rules

- Do NOT append `Co-Authored-By` lines to commit messages.

## Workflow: Plan Before Execute

For every new requirement:

1. **Write a plan first** — save to `../codeplans/ArchForge/<date>-<topic>.md`
2. **Wait for user review** — do NOT start implementation until approved
3. **Track progress** — update plan file status after each step (pending / in_progress / done)
4. **Verify each step** — run `./gradlew build` after each change
5. **Verify before push** — run `./gradlew :archforge-server-admin:bootRun` to confirm startup
6. **Push codeplans repo** after completion

## Agent Loop Files

- Do NOT create or keep `.agent-loop/` inside this repository.
- Place all agent-loop related files in `../codeplans/ArchForge/.agent-loop/`.

## Static Analysis Layer (auto-enforced)

Five layers run automatically — you never invoke them separately:

- **Error Prone** (2.50.0) hooks into javac: every `compileJava` /
  `compileTestJava` is analyzed for bug patterns. ERROR-severity findings
  fail the compile. See the wiring in the root `build.gradle.kts`
  (`net.ltgt.errorprone` 5.1.1); severity downgrades live there and must
  carry a reason.
- **Checkstyle** (10.26.1) gates style on the semantic side (import
  hygiene, naming, control-flow traps — formatting stays with Spotless).
  `checkstyleMain`/`checkstyleTest` run **before `test`** and as part of
  `check`/`build`. Config: `config/checkstyle/checkstyle.xml`,
  suppressions: `config/checkstyle/checkstyle-suppressions.xml`
  (tests keep snake_case method names by convention).
- **SpotBugs** (4.10.4) scans bytecode after compilation: NP (null deref),
  RV (ignored return values), DL (String locks), resource leaks and more fail
  `spotbugsMain`/`spotbugsTest` (part of `check`). Exclusions live in
  `config/spotbugs/spotbugs-exclude.xml` (main) and `spotbugs-exclude-test.xml`
  (test — mirrors the main ruleset plus test allowances); every entry needs a
  reason. **NullAway** (0.14.1) extends Error Prone with null-safety analysis
  over the jspecify `@NullMarked` base — enforced at ERROR severity on all
  source sets; any nullness violation fails the compile.
- **Forbidden APIs** (3.10) bans by signature: `System.out/err` (cli and test
  sources exempt — MCP stdio / debug prints), `Runtime.exec`, `Thread.sleep`,
  `java.util.Date/Calendar/SimpleDateFormat`, internal JDK APIs. Rules:
  `config/forbiddenapis/forbidden-signatures.txt` (+ test variant).
- **OWASP Dependency Check** (13.0.0) scans the runtime classpath for CVEs
  (transitives included) via `./gradlew dependencyCheckAnalyze` — a dedicated
  CI job keyed by the `OWASP_NVD_API_KEY` secret, with OSS Index as the
  anonymous secondary source. Not wired into `check` (NVD sync is heavy).
- **Spotless** rewrites formatting via `spotlessApply`; `spotlessCheck`
  fails the build on unformatted code.

## Per-Edit Verification Protocol (MANDATORY for AI agents)

Every code edit must be verified BEFORE moving on — "configured" is not
"working", and a compile alone is not a pass:

1. **After editing main code in module M** (e.g. M = archforge-server-admin):
   `./gradlew :M:compileJava :M:checkstyleMain`
   (compileJava includes Error Prone; checkstyleMain is the style gate)
2. **After editing tests in module M**:
   `./gradlew :M:compileTestJava :M:checkstyleTest`
3. **After behavioral changes**: run the affected tests —
   `./gradlew :M:test --tests '*FooTest*'` (checkstyle gates test automatically).
4. **After bytecode-level edits** (or any doubt): add `spotbugsMain` for the
   module to the targeted command above.
5. **Dependency changes**: `./gradlew dependencyCheckAnalyze` locally (needs
   network; NVD data sync requires the API key in CI) — or rely on the CI job.
6. **Before claiming the task complete**:
   `./gradlew spotlessApply compileJava compileTestJava checkstyleMain checkstyleTest forbiddenApisMain spotbugsMain`
   then the targeted tests. For the full gate including all tests:
   `./gradlew build`.
5. **Never batch unverified edits** — fix loop violations immediately while
   the context is small; do not park them for a "final pass".

Known sandbox caveat: `build`/`test` require Docker (Testcontainers). In
Docker-less environments the protocol above (compile + checkstyle + unit
tests without @Tag("slow")/@Tag("contract")) is the verification floor.

## Verification Checklist

Before claiming work is complete:

- [ ] `./gradlew build` passes (includes spotless + checkstyle + all tests)
- [ ] `./gradlew :archforge-server-admin:bootRun` starts without errors
- [ ] No new Error Prone warnings introduced beyond the pre-existing baseline
- [ ] Plan file updated with final status

## Project Context

This repository is part of the **ArchForge multi-repository project** (three
independent Git repositories, cloned side by side, no submodules). For the
machine-readable project map, read `repos.yaml` first.

```
archforge/
├── ArchForge/          # backend + contracts (this repo)
│   ├── spec/           # openapi.yaml, enums.yaml, schemas/
│   ├── docs/specs/     # API / naming / error-code / security standards
│   ├── docs/architecture.md
│   └── skills/         # agent skills + backend standard
├── ArchForgeWeb/       # C-end web client (Next.js)  — consumes server-web :8081
└── ArchForgeAdmin/     # admin client (vue-pure-admin) — consumes server-admin :8080
```

- **This repo owns the contract**: `spec/openapi.yaml` (OpenAPI 3.1) and
  `spec/enums.yaml`. `spec/schemas/` holds JSON Schema 2020-12 definitions.
  The old `ArchForgeSpec` repository is retired — its contents live here now.
- Canonical backend standard: `skills/archforge-project-standard/standard.md`
  (pointer: `docs/specs/backend-standard.md`).
- Cross-repository behavior: read `repos.yaml`, then `docs/architecture.md`
  before changing anything that affects the Web / Admin clients or the contract.
- Do not modify another repository unless explicitly required.
- This repo exposes two applications: `server-admin` (port 8080) and
  `server-web` (port 8081).
- **Do not invent deleted APIs**: `/system/menu` and `/system/role` no longer
  exist (see `repos.yaml` → `contract.deleted_paths`).
- **Contract sync rule**: whenever the backend API changes (paths, parameters,
  request/response schemas, auth), update `spec/openapi.yaml` in the same
  change. CI diffs the live export against it and blocks breaking changes.
- **Example modules are not the product.** `archforge-example-task` is an
  unlinked in-tree example (`settings.gradle.kts` dynamic include). Do not
  add it to `server-admin` (`api(project)`, `scanBasePackages`, a second
  EMF, `task_master`, or `hibernate.hbm2ddl.auto=update`). Do not put
  `/task` or `/web/task` in `spec/openapi.yaml`. Production
  EntityManagerFactories must not hard-code `hibernate.hbm2ddl.auto=update`
  — schema belongs to Flyway.
- **Scheduler is db-scheduler**, not Quartz. HTTP lives at `/admin/scheduler-job`.
  Do not reintroduce `/quartz` or document `sys_quartz_job` / `QuartzReflectionJob`
  as current. See `skills/archforge-project-standard/standard.md` §3.8.
