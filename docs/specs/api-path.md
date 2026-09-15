# API path prefixes

Every contract path carries a service prefix:

| Server | Prefix | Client |
|--------|--------|--------|
| `server-admin` :8080 | `/admin/*` | ArchForgeAdmin |
| `server-web` :8081 | `/web/*` | ArchForgeWeb |

New endpoints go under `/admin/{resource}` or `/web/{resource}`. Do not add a third prefix.

## Live admin resources

```
/admin/auth/login
/admin/auth/logout
/admin/auth/refresh-token
/admin/auth/getConfig
/admin/auth/captchaImage
/admin/auth/getLoginUserInfo
/admin/auth/getRouters

/admin/user
/admin/role
/admin/menu
/admin/dept
/admin/config
/admin/notice
/admin/operation-log
/admin/login-log
/admin/server
/admin/dashboard/metrics
/admin/dashboard/trends
/admin/dashboard/recent-activities
/admin/dashboard/todo
/admin/chat/config
/admin/chat/sessions
/admin/chat/sessions/{id}
/admin/chat/sessions/{id}/messages
/admin/permission-matrix/menus/tree
/admin/permission-matrix/roles/{roleId}/permissions
/admin/scheduler-job
/admin/cms/article
/admin/cms/category
/admin/cms/file
/admin/meta-table
/admin/task
/admin/file
/admin/monitor
/admin/system/dict
/admin/idempotent/token
```

Shared infrastructure mounts per-server: `IdempotentTokenController` reads
`arch-forge.idempotent.base-path` (`/admin/idempotent` on :8080, `/web/idempotent` on :8081).

## Deleted — do not document or re-add

```
/system/menu
/system/role
/quartz
```

Those controllers are not part of the contract. Menu and role live at `/admin/menu` and `/admin/role`.
`/system/dict` moved to `/admin/system/dict` — no `/system/*` path remains.

## Web resources

```
/web/login
/web/logout
/web/refresh-token
/web/register
/web/forgot-password
/web/reset-password
/web/verification-code/send
/web/user/profile
/web/user/change-password
/web/dashboard/metrics
/web/notices
/web/operation-logs
/web/categories
/web/articles
/web/articles/{slug}
/web/user/articles
/web/file/upload
/web/file/{fileId}
/web/idempotent/token
```

See [spec/openapi.yaml](../../spec/openapi.yaml).
