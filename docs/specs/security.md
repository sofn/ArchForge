# Security

Applies to both `server-admin` and `server-web`.

## sa-token

Auth is **sa-token**, not Spring Security JWT.

| Realm | Util | Server | Client |
|-------|------|--------|--------|
| Admin | `StpAdminUtil` | :8080 | ArchForgeAdmin |
| Web | `StpWebUtil` | :8081 | ArchForgeWeb |

- Header: `Authorization: Bearer <token>` when `tokenName` is `Authorization`.
- Admin stores token in cookie `authorized-token` + localStorage `user-info`.
- Web stores `token`, `tokenName`, `refreshToken` in cookies (and localStorage mirror).
- Tokens from one realm must not be sent to the other.

Login / refresh endpoints are public. Everything else requires login unless explicitly opened.

## SaCheckPermission / role

Admin controllers currently gate with `@SaCheckRole(value = "ADMIN", type = StpAdminUtil.TYPE)`.

Finer-grained checks use **sa-token permission annotations**:

```java
@SaCheckPermission(value = "system:user:add", type = StpAdminUtil.TYPE)
```

Permission strings come from `sys_menu.permission` and must match Admin `v-perms` / `hasPerms()`.

- New mutating admin APIs need a permission (or an explicit documented exemption).
- Do not invent permission codes in the frontend only.
- **Every `/admin/meta-table/**` handler carries its own `@SaCheckPermission("meta-table:…")`** (list / add / edit /
  remove / export). They read, rewrite and export arbitrary business tables, so the class-level role check is not
  enough; `MetaTablePermissionCoverageTest` fails on any handler without one.
- Do not pre-open login-free paths for endpoints that do not exist (`/admin/auth/register` was one); the list is
  `AdminSaTokenConfig.PUBLIC_PATHS` and `AdminPublicPathConsistencyTest` checks it against the controller.

Auth failures: HTTP 401 / 403 ProblemDetail (`AdminAuthExceptionHandler`).

## RateLimit

`@RateLimit` (Redis + Lua) on sensitive public endpoints.

```java
@RateLimit(key = "login", time = 60, maxCount = 10, limitType = RateLimit.LimitType.IP)
```

| `LimitType` | Key |
|-------------|-----|
| `GLOBAL` | method |
| `IP` | client IP |
| `USER` | login id |

Required on login, register, verification-code, password-reset. Exceeded → `SystemErrorCode.E_RATE_LIMIT_EXCEEDED`.

## XssFilter

Incoming HTML/script in query/header values is escaped (`<` / `>` only). Do not encode parentheses or apostrophes.

- Admin rich text and C-end Markdown are stored, not executed as HTML, unless explicitly sanitized on render.
- Never persist raw `<script>` from user input.
- C-end uploads (`POST /web/file/upload`) must reuse shared `FileUploadValidator`, reject SVG/HTML, rate-limit, and serve non-bitmap files as `attachment`.

## CORS

Configured per server (`AdminCorsConfig` and the web equivalent):

- Dev may use origin patterns.
- **Prod forbids `allowedOrigins: *`** when credentials are on. Set explicit origins in env (`arch-forge.cors.allowed-origins`).
- Prefer the global CORS filter over scattering `@CrossOrigin` on controllers.

## Secrets and env

Do **not** commit secrets. Use environment / profile files:

| Kind | Examples |
|------|----------|
| DB | `SPRING_DATASOURCE_*`, Flyway URL |
| Redis | Redis host / password |
| Object storage | S3/RustFS keys |
| Mail | SMTP credentials (C-end verification codes) |
| CORS | production origin list |
| Tokens | sa-token timeout / cookie flags |

Local templates stay as `.env.example` / `application-*.yaml.example`. Production values come from the environment, never from git.

Production must inject `ARCH_FORGE_RSA_PRIVATE_KEY`, `DB_PASSWORD` and `REDIS_PASSWORD` (the prod compose starts Redis
with `--requirepass` and both apps read `spring.data.redis.password`). Missing RSA in `prod` fails fast at startup. Docker Compose files require `${DB_PASSWORD:?…}` with no baked-in default. There is no JWT signing key: auth is sa-token (opaque tokens in Redis), so `JWT_SECRET` / `arch-forge.jwt.*` were removed as dead configuration.

## Actuator

Keep the exposure allow-list small.

| Profile | `management.endpoints.web.exposure.include` | Port |
|---------|-----------------------------------------------|------|
| admin / web default (dev) | `health,info,metrics,prometheus` | business port |
| admin / web `staging`, `prod` | `health,info,prometheus` | **separate management port** (`MANAGEMENT_SERVER_PORT`; admin `8089`, web `8091`), not published, not behind the frontend `/api` proxy |

Probes for orchestrators are `/livez` and `/readyz` on the business port (no login). Prometheus scrapes the
management port over the internal observability network.

Do not expose `env`, `beans`, `heapdump`, or `mappings` without an explicit Spec change.

## Dynamic SQL (meta-table)

The meta-table designer is an entry point that executes DDL, so every value that reaches SQL is validated *and*
rendered strictly — validation alone is not trusted:

- **Column default values** are rendered by `ColumnTypeResolver.formatDefaultValue`: quoted branches double the
  single quote; unquoted branches (INTEGER/DECIMAL/FILE/IMAGE/REFERENCE and numeric/boolean array elements) are
  parsed as the target type and re-printed canonically, anything else throws. The validator rejects the same inputs
  before anything is stored.
- **REFERENCE display expressions** follow a whitelist grammar (`DisplayExpression`): `ref.<column>[::text]`,
  `'string literals'`, joined with `||`. No functions, operators, parentheses, comments or sub-queries. Validation
  and runtime rendering share the one parser; a stored expression that does not parse degrades to the raw reference
  value and logs a warning instead of being executed.
- **Physical table name = prefix + code**, validated as a whole (`SqlIdentifier.validatePhysicalTableName`): the prefix
  is `[a-z][a-z0-9_]{0,31}`, the result may not start with `sys_ qrtz_ pg_ sql_ information_schema_ flyway_`, and an
  empty prefix (adopted tables) may not start with `meta_`. `create` refuses a name that already exists
  (`META_PHYSICAL_TABLE_EXISTS`) — adoption is the import flow's job — and `delete` re-validates before `DROP`.
- **REFERENCE targets** must be the table itself or a registered meta table (`MetaTableValidator`); at runtime
  `ReferenceDisplayBuilder` never joins a platform table (`SqlIdentifier.isPlatformTableName`) and shows the raw value
  for such legacy definitions. Otherwise a display expression such as `ref.password` reads any platform column.

## Data scope

Row-level scope exists only inside a `@DataPermission` call (`DataScopeAspect` → `DataScopeContextHolder`). It fails
closed: a call whose user cannot be resolved gets ONLY_SELF without a user id — no rows. Without the annotation the
meta-table filter sees no scope and returns every row, so `MetaTableDataScopeCoverageTest` requires it on every
`MetaTableController` handler. Generated module controllers check their `<tableCode>:*` permissions (seeded by the
generated menu SQL) and run export/import under `@DataPermission`.

## Redis value serialization

`GenericJacksonJsonRedisSerializer.enableUnsafeDefaultTyping()` lets whoever can write a Redis value pick the class
that is instantiated when it is read back. Use `RedisJsonSerializers.safeJson(extraPrefixes)` (redisson-starter): it
resolves only `com.lesofn.archforge.*`, collections, maps, numbers, strings, booleans, `java.time`, UUIDs and arrays;
application types are added through `arch-forge.cache.composite.allowed-type-prefixes`.

## Encryption helper

`AESEncrypter` has no built-in key: `AESEncrypter.of(<Base64 key>)` (16/24/32 bytes), AES-GCM, random IV per message,
hex(IV‖ciphertext‖tag). Error messages never contain plaintext, ciphertext or key.

Nothing in ArchForge encrypts with it, so there is no `AES_KEY` setting and `archforge init` does not generate one. An
application that uses the helper owns its key: read it from its own environment variable, fail at startup when it is
missing, and never fall back to a literal. Data encrypted by the old helper (ECB, built-in key) used a key that is
public in the git history — treat it as plaintext: decrypt it once in a migration outside the library and re-encrypt
with `AESEncrypter.of(...)`. The old key must not come back into the code.

## Request log

`arch-forge.request-log.mask-fields` are key *fragments* — a key containing one is masked (`token` ⇒ `accessToken`,
`refresh_token`, …). `staging` / `prod` do not log request or response bodies at all.
