# API response format

ArchForge uses **two formats**. Do not mix them across servers.

## Admin — `server-admin` :8080

Success and business results are wrapped:

```json
{
  "code": 0,
  "message": "ok",
  "data": {}
}
```

| Field | Rule |
|-------|------|
| `code` | `0` = success. Non-zero is an [error code](error-codes.md). |
| `message` | Human-readable; Jackson alias of backend `msg`. |
| `data` | Payload. Paginated lists: `{ list, total, pageSize, currentPage }`. |

Implemented by `ResultValueWrapper` + `ResponseResult`. ArchForgeAdmin reads this envelope as-is (`src/utils/http`).

Auth failures on admin may still emit RFC 9457 ProblemDetail (`401` / `403`). Treat that as an exception path, not the success contract.

## Web — `server-web` :8081

C-end **errors** are RFC 9457 ProblemDetail:

```json
{
  "type": "about:blank",
  "title": "Unauthorized",
  "status": 401,
  "detail": "未登录或登录已过期",
  "instance": "/web/user/profile",
  "code": 401
}
```

| Field | Rule |
|-------|------|
| `status` | HTTP status |
| `detail` | Human-readable error |
| `code` | Optional ArchForge node/code property |
| `title` / `instance` | RFC 9457 |

ArchForgeWeb must read `detail` (and fall back to `message` only if present). Do not assume `{code, message, data}` on error responses.

Successful C-end JSON may still pass through the shared wrapper. Clients should:

1. If HTTP is not 2xx → parse ProblemDetail (`detail`).
2. If body has `code` and `code !== 0` → treat as failure.
3. Otherwise use `data`.

## Do not

- Point Admin at ProblemDetail as the success format.
- Point Web error UI only at `message` and ignore `detail`.
- Invent a third envelope.
