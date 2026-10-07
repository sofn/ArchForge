# 0009. Staging/prod actuator on a management port; meta-table SQL and secrets hardening

Date: 2026-10-07
Status: accepted

## Context

The 2026-10-07 review listed P0 findings. Verification split them: some were wrong (the admin/web token namespaces
are isolated; `nestProp.vue` renders static demo data; `.env.*` in the SPA are public Vite settings), but several were
real and share a root cause — a value, an endpoint or a port that was trusted because *some* check existed upstream:

- the meta-table designer builds DDL from column defaults, display expressions and `tablePrefix`; the display-expression
  blacklist used `String.matches(".*KEYWORD.*")`, which never crosses a line break;
- prod exposed only `health,info`, so Prometheus could not scrape it, while the obvious fix (expose `prometheus`)
  would publish it through the frontend nginx `/api` proxy and web's published `8081`;
- `maskFields` matched whole key names, so the session token in the login response (`accessToken`) was written to `info.log`.

## Decision

- **Strict rendering, not just validation** for every value that reaches DDL: typed defaults, a whitelist grammar for
  display expressions (`DisplayExpression`, shared by validator and runtime), and physical-name validation of
  *prefix + code* (`SqlIdentifier.validatePhysicalTableName`). `create` refuses an existing physical table.
- **`staging`/`prod` serve the actuator on `management.server.port`** (admin 8089, web 8091), never published and not
  behind the nginx proxy; only `health,info,prometheus` are exposed. Probes live on the business port as `/livez`
  and `/readyz` (`probes.add-additional-paths`). Prometheus uses DNS service discovery on those ports.
- **Allow-listed Redis polymorphic typing** (`RedisJsonSerializers.safeJson`) replaces `enableUnsafeDefaultTyping`;
  the prod compose starts Redis with `--requirepass`.
- **`AESEncrypter` loses its built-in key** (`getInstance()` removed, `of(base64Key)` added, AES-GCM). It had no
  in-repo callers, so the break is contained to the published `common-base` API.
- `mask-fields` entries become key fragments; staging/prod stop logging payloads.
- Error-code templates use slf4j `{}` (the `ErrorInfo` formatter); `{0}` never rendered.

## Consequences

- Operators who probed `/actuator/health` on the business port in prod must switch to `/livez` / `/readyz` (or the
  management port). The shipped all-in-one image already did.
- A stored display expression that uses a function (`COALESCE(...)`) now fails designer validation and degrades to the
  raw value at runtime until rewritten with `ref.col`, `'text'`, `||`.
- Cached application entities need `arch-forge.cache.composite.allowed-type-prefixes`; project types work unchanged.
- Alternatives rejected: a scrape bearer-token filter (works on the shared port but needs token distribution and a
  Prometheus credentials file), and blocking `/api/actuator` in nginx only (misses web, misses other proxies).
