# Pitfalls — auth / sa-token

- **Wildcard `"*:*:*"` matches literally per segment** — it only hits two-segment permissions like `system:user:list`; single-segment `meta-table:list` is NOT matched. Super-admin wildcard must be `"*"`. (verified via jshell `vagueMatch`, 2026-09)
- **`RequestContext.currentUid` was dead code**: its only writer `AuthResourceFilter` had `@Service` commented out and zero references — every `getCurrentUid()` returned 0. Post-sa-token, uid lives in `LoginContext` (session → `SystemLoginUser`). When auditing fields, trace the writer chain first.
- **`LoginContext.findAdminUser()` throws `SaTokenContextException`** when sa-token context is uninitialized (outer filters). Callers in filter chains must degrade gracefully.
- **Resolving uid without request context**: `StpAdminUtil.STP_LOGIC.getLoginIdByToken(token)` hits the dao token→loginId mapping — works outside request scope.
- **Captcha codes live in Redis**: `captcha:<uuid>` → JSON string; strip quotes before building the login request body.
- **Dead `/proxy` endpoint was an open redirect** — legacy `redirect:` + arbitrary url param. Deleted with the auth dead-code cleanup (commit 032fe8a7).
