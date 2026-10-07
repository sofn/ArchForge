# Pitfalls — auth / sa-token

- **Wildcard `"*:*:*"` matches literally per segment** — it only hits two-segment permissions like `system:user:list`; single-segment `meta-table:list` is NOT matched. Super-admin wildcard must be `"*"`. (verified via jshell `vagueMatch`, 2026-09)
- **`RequestContext.currentUid` was dead code**: its only writer `AuthResourceFilter` had `@Service` commented out and zero references — every `getCurrentUid()` returned 0. Post-sa-token, uid lives in `LoginContext` (session → `SystemLoginUser`). When auditing fields, trace the writer chain first.
- **`LoginContext.findAdminUser()` throws `SaTokenContextException`** when sa-token context is uninitialized (outer filters). Callers in filter chains must degrade gracefully.
- **Resolving uid without request context**: `StpAdminUtil.STP_LOGIC.getLoginIdByToken(token)` hits the dao token→loginId mapping — works outside request scope.
- **Captcha codes live in Redis**: `captcha:<uuid>` → JSON string; strip quotes before building the login request body.
- **Dead `/proxy` endpoint was an open redirect** — legacy `redirect:` + arbitrary url param. Deleted with the auth dead-code cleanup (commit 032fe8a7).
- **`@DataPermission` used to fail open**: the aspect skipped the scope when the user could not be resolved, and
  `MetaDataScopeProviderImpl` reads "no scope" as `all()`. The aspect now sets ONLY_SELF without a user id (= no rows).
  The scope lives in the request `RequestContext` — outside a request scope `DataScopeContextHolder.set` is a no-op, so
  tests must run inside `ScopedValueContext.runInScope`.
- **sa-token keeps its DAO in a static**, overwritten by each Spring test context that starts; Spring Framework 7 *pauses*
  idle cached contexts (stops SmartLifecycle beans such as `LettuceConnectionFactory`). A plain unit test touching
  `StpAdminUtil` can therefore inherit a stopped Redis connection — give it its own `SaTokenDaoDefaultImpl` and restore.
- **An interceptor that needs the cached body must walk the wrapper chain** (`WebUtils.getNativeRequest(request,
  RepeatableRequestWrapper.class)`): the request-log filter wraps the caching wrapper again, so `instanceof` misses it
  (broke API signatures and the repeat-submit fingerprint).
