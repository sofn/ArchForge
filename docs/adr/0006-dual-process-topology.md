# 0006. Dual-process topology: keep server-admin / server-web split, ship all-in-one packaging

Date: 2026-09-14
Status: accepted

## Context

`server-admin` (:8080) and `server-web` (:8081) are two Spring Boot apps with
separate sa-token realms, response formats, CORS chains and config surfaces.
A merge proposal argued a single JVM would save memory and simplify ops.

Code-level review (see `codeplans/reports/2026-09-14-service-topology-review-and-final-plan.md`)
found the merge cost is real: two same-named `Application` classes, duplicate
`passwordEncoder` beans, an admin `SaInterceptor` registered on `/**` that
would 401 every `/web/**` request, dual CORS chains, divergent response
formats and message-converter ordering — M1–M10 in the appendix below.

More importantly, merging **weakens the security boundary**: `server-admin`
holds `RSA_PRIVATE_KEY`, `LLM_API_KEY` and S3 credentials; `server-web`
holds none of these and faces untrusted C-end input (registration, file
upload). Same-JVM means a C-end exploit reaches admin secrets.

At the same time, `server-web` had **no deployment path at all** — no compose
service, no CI smoke — which was a real delivery gap.

## Decision

- **Keep two JVM processes.** No merge now. The "two JVMs cost memory"
  argument was moot — `server-web` wasn't even deployed.
- **Ship an all-in-one container** (`docker/allinone/`, image
  `archforge:allinone`): nginx + server-admin + server-web + next.js under
  s6-overlay, one `docker run` delivers the whole product. Packaging unites;
  processes do not.
- **`server-web` gets first-class deployment**: `backend-web` service in
  prod/staging compose, CI startup smoke on :8081, presence in the
  all-in-one image.
- **Reserve Route B′** (a thin `archforge-server` composite shell with a
  single `main()` over both modules) for when a trigger condition hits —
  single-node < 1 GB RAM, "two jars" becomes an adoption blocker, or the
  product repositions to a single deliverable.

## Consequences

- Deployment has two honest shapes: split services (independent scale /
  release) or all-in-one (single host, minimal ops). Both keep the
  process boundary.
- Before any future merge, the appendix checklist must be cleared — M3
  (`SaInterceptor` path whitelist) first.
- CI now proves both jars boot against real infra.

## Appendix — merge checklist (M1–M10), must clear before Route B′

Blocking:

- **M1** Two same-named `Application` classes each with auto-config
  `exclude`s — composite shell keeps one `main()`, explicit
  `scanBasePackages`, excludes both original `Application` classes.
- **M2** Two `PasswordConfig` / `passwordEncoder` beans →
  `BeanDefinitionOverrideException`. Move to `common-base` or name-split.

High risk (boots but misbehaves):

- **M3** admin `SaInterceptor` on `/**` would force `StpAdminUtil.checkLogin()`
  on `/web/**` — all C-end 401. Must become a whitelist (`/admin/**`,
  `/auth/**`).
- **M4** Both sides `SaManager.putStpLogic(...)` in `@PostConstruct` —
  key by loginType, add an integration test for registration order.
- **M5** Dual CORS: global `CorsFilter` (admin) + `addCorsMappings` (web)
  → duplicated headers / preflight failure. Unify or segment by path.
- **M6** Response format split: admin envelope vs web ProblemDetail —
  both `@RestControllerAdvice`s need strict `basePackages`.
- **M7** MessageConverter contention: `ByteArrayHttpMessageConverter`
  ordering vs the extra `jacksonJsonHttpMessageConverter` bean.

Adjudication needed:

- **M8** Config matrix merge: `spring.application.name`, sa-token timeouts
  (604800/86400), actuator exposure, tracing, mail, 8 profile yamls.
- **M9** Modulith/ArchUnit semantics: `ModulithRoot` assumes entry point
  stays in `server.admin`; ARCH-101 (web must not depend on admin) needs
  re-scoping.
- **M10** Contract & CI: `spec/openapi.yaml` lists both servers;
  live-export / `OpenApiSnapshotTest` / smoke covered admin only (now
  both — matrix job added alongside this ADR).
