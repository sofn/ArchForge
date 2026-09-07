# API path prefixes

Target prefixes:

| Server | Prefix | Client |
|--------|--------|--------|
| `server-admin` :8080 | `/admin/*` | ArchForgeAdmin |
| `server-admin` :8080 | `/auth/*` | Admin login / captcha / routers |
| `server-web` :8081 | `/web/*` | ArchForgeWeb |

New endpoints go under `/admin/{resource}` or `/web/{resource}`. Do not add a third prefix.

## Live admin resources

```
/auth/login
/auth/logout
/auth/refresh-token
/auth/getConfig
/auth/captchaImage
/auth/getLoginUserInfo
/auth/getRouters

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
```

Admin paths that still lack `/admin` (legacy, migrate when touched):

```
/blog/article
/blog/category
/blog/file
/meta-table
/file
/monitor
/quartz
```

## Deleted — do not document or re-add

```
/system/menu
/system/role
```

Those controllers are not part of the contract. Menu and role live at `/admin/menu` and `/admin/role`.

## Migration backlog

```
/system/dict
```

Dictionary admin still uses the leftover `/system` prefix. Move to `/admin/dict` (and update Admin `src/api/dict.ts`) as a dedicated change. Until then it is the only allowed `/system/*` path.

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
```

See [spec/openapi.yaml](../../spec/openapi.yaml).
