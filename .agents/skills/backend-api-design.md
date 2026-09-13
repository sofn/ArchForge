# Skill: backend API design

Use when adding or changing HTTP APIs in `ArchForge`.

## Before coding

1. Read [`../docs/architecture.md`](../docs/architecture.md) and
   [`../docs/specs/api-path.md`](../docs/specs/api-path.md).
2. Decide the server: admin `:8080` or web `:8081`.
3. Put the path under `/admin/*` or `/web/*`. Never add `/system/menu` or `/system/role`.
4. Update [`../spec/openapi.yaml`](../spec/openapi.yaml) in the same change.

## Shape

- Controller in the matching server module, thin, constructor-injected.
- Request / Response types (`XxxCreateRequest`, `XxxDetailResponse`).
- Auth: `@SaCheckLogin` / `@SaCheckRole` / `@SaCheckPermission` with the correct `Stp*Util.TYPE`.
- Sensitive public endpoints: `@RateLimit`.
- Errors: module `ErrorCode` + exception. See [`../docs/specs/error-codes.md`](../docs/specs/error-codes.md).
- Admin success: envelope `{code, message, data}`.
- Web errors: ProblemDetail.

## After coding

- Enum value changed? Update [`../spec/enums.yaml`](../spec/enums.yaml) and the frontend ([`enum-sync.md`](../docs/specs/enum-sync.md)).
- Do not edit Admin or Web unless the task explicitly spans repos ([`cross-module-change.md`](cross-module-change.md)).
