# Error codes

Every business error implements `ErrorCode` and registers with `ErrorManager`.

## Formula

```
code = projectCode * 10000 + moduleCode * 100 + nodeNum
```

| Piece | Source | Notes |
|-------|--------|-------|
| `projectCode` | `ProjectModule.getProjectCode()` | `0` = system default; ArchForge modules use `1` |
| `moduleCode` | `ProjectModule.getModuleCode()` | Unique per bounded context |
| `nodeNum` | enum constant | Unique inside that module |

Duplicates fail at startup (`错误码重复`).

## Modules (`ArchForgeProjectModule`)

| Enum | projectCode | moduleCode | Typical codes |
|------|------------:|-----------:|---------------|
| System (`SystemProjectModule`) | 0 | 0 | `0` success, `1` system error, `12` param, `37` rate limit |
| `ADMIN_AUTH` | 1 | 1 | `10101+` login / token / captcha; security codes start at node `40` |
| `ADMIN_USER` | 1 | 2 | `10201+` user / dict |
| `TASK` | 1 | 3 | `10301+` |
| `META_TABLE` | 1 | 4 | `10401+` |
| `BLOG` | 1 | 5 | `10501+` |
| `WEB_AUTH` | 1 | 6 | `10601+` |
| `CHAT_AI` | 1 | 7 | `10701+` |

`SecurityErrorCode` shares `ADMIN_AUTH` and starts `nodeNum` at `40` so it does not collide with `AdminAuthErrorCode`.

## How to add a code

1. Put the enum in `api/errors/` of the owning module.
2. Register `ErrorManager.register(ArchForgeProjectModule.X, this)` in the constructor.
3. Throw a `BaseRuntimeException` subclass from that module.
4. Do not reuse another module's `nodeNum` space.
5. Surfaces: admin envelope `code`; web ProblemDetail property `code`.

New bounded contexts get a new `ArchForgeProjectModule` entry **before** the first error enum.
