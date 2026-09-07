# Directory layout

## Backend module prefix

Gradle projects and directories use the `archforge-` prefix:

```
ArchForge/
├── archforge-common/
│   ├── archforge-common-base/
│   ├── archforge-common-error/
│   └── archforge-common-jpa/
├── archforge-domain/
│   ├── archforge-admin-user/
│   ├── archforge-blog/
│   └── archforge-meta-table/
├── archforge-infrastructure/
├── archforge-server-admin/
├── archforge-server-web/
├── archforge-cli/
├── archforge-example/archforge-example-task/
├── archforge-starters/
│   ├── archforge-cache-starter/
│   ├── archforge-lock-starter/
│   ├── archforge-redisson-starter/
│   └── archforge-trace-starter/
├── archforge-dependencies/
├── docker/                 # not prefixed
├── skills/                 # backend standard lives here
└── scripts/
```

Do not add a new module without the prefix. Domain package names: hyphen → subpackage (`meta-table` → `com.lesofn.archforge.meta.table`).

## Admin frontend (`ArchForgeAdmin`)

```
src/
├── api/            # one file per backend resource (user.ts, system.ts, dict.ts)
├── views/          # pages: system/, monitor/, meta-table/, login/, ...
├── components/     # Re* shared components
├── store/modules/  # Pinia
├── router/
├── utils/          # http, auth
├── layout/
└── directives/     # v-perms, …
locales/
mock/
```

New business pages go under `src/views/<feature>/` with a matching `src/api/<feature>.ts`.

## Web frontend (`ArchForgeWeb`)

```
apps/web/
├── src/
│   ├── app/                 # App Router pages
│   ├── components/          # UI + Header / AuthProvider
│   └── lib/                 # api.ts, httpClient.ts
├── messages/                # en.json, zh.json
├── i18n/
├── e2e/
└── middleware.ts
```

New C-end routes are App Router folders under `apps/web/src/app/`. API calls stay in `src/lib/api.ts` (or a split module next to it), always hitting `/web/*` on :8081.
