# Enum sync

Shared numeric/string enums are a contract. They live in
[`spec/enums.yaml`](../../spec/enums.yaml) in this repository.

## Flow

```
Backend Java enum changes
        │
        ▼
  spec/enums.yaml   (this repo, same change)
        │
        ▼
  Admin / Web TypeScript constants + labels
```

1. Change the Java enum (and dictionary annotation if any).
2. Update `spec/enums.yaml` in the same pull request / commit series.
3. Regenerate the clients from this repo:

   ```bash
   cd scripts && npm ci && cd ..
   node scripts/gen-enums.mjs
   ```

   It writes `../ArchForgeAdmin/src/types/enums.generated.ts` and
   `../ArchForgeWeb/apps/web/src/types/enums.generated.ts`.
   Commit the regenerated files in those repos.
4. Frontend CI fails when those generated files drift from `spec/enums.yaml`.
5. Do not ship a backend value the UI cannot name.

## Generated TypeScript constants

Both frontends consume `enums.generated.ts` (generated, never hand-edited). For
every entry in `spec/enums.yaml` it exports:

- `<Enum>` const object (`as const`) + matching union type,
- `<Enum>Label` record mapping value → label for rendering.

## MenuTypeEnum

Canonical **backend** values:

| Value | Name | Meaning |
|------:|------|---------|
| 1 | MENU | 页面 |
| 2 | CATALOG | 目录 |
| 3 | IFRAME | 内嵌 iframe |
| 4 | OUTSIDE_LINK_REDIRECT | 外链跳转 |

**Buttons are not a `menu_type`.** They are `sys_menu.is_button = 1`. Admin must not keep vue-pure-admin `0/1/2/3` UI codes.

Admin `src/views/system/menu/utils/enums.ts` (`menuTypeOptions`) **must stay aligned** with 1/2/3/4. Use `isButton` for button rows and `menuTypeToRouterMeta()` for iframe / outside-link `frameSrc`.

## Other registered enums

- `StatusEnum`: `0` disable / `1` enable
- `UserStatus`: `1` normal / `2` disabled / `3` frozen
- `BlogArticleStatus`: `0` draft / `1` published / `2` offline
- `NoticeTypeEnum`: `1` notification / `2` announcement
- `NoticeStatusEnum`: `0` close / `1` open
- `GenderEnum`: `0` male / `1` female / `2` unknown
- `OperationStatusEnum`: `0` fail / `1` success
- `LoginStatusEnum`: `0` fail / `1` success / `2` logout / `3` register

If a UI hard-codes these numbers, it must match `spec/enums.yaml`.
