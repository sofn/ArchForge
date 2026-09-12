# 0005. Service placement: api.service + internal.service, runtime via ports

Date: 2026-09-12
Status: accepted

## Context

`server-admin` accumulated service beans that belonged to domains
(`UserExportService`, `PermissionMatrixService`, `LoginAttemptService`,
`ScheduledJobService`). Moving them wholesale hit two walls: infrastructure
must not depend on domain (ADR-0003), and `server-web`'s component scan
covers the whole `user` package — an impl requiring a runtime bean would
break web startup.

## Decision

- **Domain-facing services** live as interface in `api.service` +
  implementation in `internal.service`.
- **Runtime coupling** goes through a port interface in `api.*` (e.g.
  `api.scheduler.SchedulerJobRuntime`); the server app supplies the adapter
  (`DbSchedulerJobRuntime`). Domain impls resolve the port lazily via
  `ObjectProvider` so apps without the runtime still boot — read paths work,
  write paths fail with an explicit error.
- Pure web-layer services (DTO assembly, cross-domain projection, sa-token
  session glue) stay in `server-admin` — moving them would rebuild the
  infra→domain inversion.

## Consequences

- New domain logic gets a service interface in `api.service`, not a
  `@Service` class in the server module.
- Adding a runtime dependency to a domain impl requires a port; a hard bean
  reference is a web-startup bug.

## Alternatives considered

- Sink runtime glue into `infrastructure` — rejected: it touches domain
  types, and infrastructure must not depend on domain.
- Keep services in server-admin — rejected for domain-facing ones: it leaves
  the module boundary hollow and blocks `server-web` reuse.
