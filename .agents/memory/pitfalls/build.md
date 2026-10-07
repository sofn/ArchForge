# Pitfalls — build / Gradle / local run

- **`bootRun` can go UP-TO-DATE after a failed start** and silently skip a real restart. For manual smoke runs prefer `bootJar` + `java -jar build/libs/*.jar`.
- **`--enable-preview` is a JVM flag**: it must come BEFORE `-jar` (`java --enable-preview -jar app.jar`), otherwise `UnsupportedClassVersionError`.
- **Rebuilding a jar overwrites the file a running process mapped** → `ClassNotFoundException` on inner classes. Kill the old process before/after `bootJar`, then restart.
- **`pkill -f java -jar` can match your own shell command line** and kill the shell. Use `pgrep -f` + explicit PID, or `jps`.
- **Dev datasource needs `DB_PASSWORD` env** — the compose PG password, not a guess; missing it surfaces as `PSQLException` at startup.
- **`ErrorInfo` formats with slf4j `{}`**: `{0}` placeholders in an `ErrorCode` message are never replaced, and args passed to a code whose message has no placeholder are dropped (the client only sees the bare code message). Use `{}` and a placeholder wherever a detail is passed.
- **Error Prone `UnrecognisedJavadocTag` (+ `-Werror`)** fires on some `{@code …}` snippets in field-level Javadoc (seen with `{@code _ -}` and several adjacent `{@code}`s in one sentence); write plain text instead of fighting it. `MissingSummary` needs a summary sentence before `@param`.
- **`pkill -f` with the jar name inside your own command line kills your shell** (hit again 2026-10): find the PID with `ss -ltnp | grep :8080` and `kill <pid>`.
- **Moving the actuator to `management.server.port` also moves `/actuator/health`**: keep a probe on the business port (`management.endpoint.health.probes.add-additional-paths=true` → `/livez`, `/readyz`) and add it to `AdminSaTokenConfig.PUBLIC_PATHS`, or orchestrator probes get 401.
- **`Observation.start(...)` without `openScope()` creates a span that is never *current***: `tracer.currentSpan()` is null for the whole request and MDC `traceId` stays empty. The hand-rolled `OtelTracer` in trace-starter also passes a no-op event publisher, so Boot's `Slf4JEventListener` never populates MDC — `RequestLogFilter` sets `traceId`/`spanId` itself.
- **Do not reuse Spring Boot's metric names for custom observations** (`http.server.requests`): two families with different tag sets double every `sum()` and put raw paths into labels.
- **A test that reads files outside its module must declare them as task inputs**: with `org.gradle.caching=true` a change
  to `docker/*.yml` or `scripts/*.sh` alone is served the cached (green) result of `DeploymentArtifactsConsistencyTest`
  — server-admin's `test` task lists them under `deploymentArtifacts`.
- **`@ConfigurationProperties` binding keeps an unresolved `${VAR}` as literal text** instead of failing: a missing
  `CORS_ALLOWED_ORIGINS` bound the origin `"${CORS_ALLOWED_ORIGINS}"`. Fail-fast checks must reject values containing `${`.
