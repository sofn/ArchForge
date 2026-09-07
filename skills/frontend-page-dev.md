# Skill: frontend page development

Use when adding a page in ArchForgeAdmin or ArchForgeWeb.

## Which client?

| Repo | Port | Backend | Errors / envelope |
|------|------|---------|-------------------|
| ArchForgeAdmin | 8848 | `server-admin` :8080 via `/api` | `{code, message, data}` |
| ArchForgeWeb | 3000 | `server-web` :8081 | ProblemDetail on errors |

Do not call the other server.

## Contract first

1. Read the contract at `spec/openapi.yaml` in the ArchForge repo (`../ArchForge/spec/openapi.yaml` from a client repo). If the endpoint is missing, stop and raise the change there — do not work around it client-side.
2. Shared enums come from `spec/enums.yaml` in the ArchForge repo (`../ArchForge/spec/enums.yaml`). Align Admin `menuType` with backend 1/2/3/4; buttons use `isButton`.

## Admin

- Page: `src/views/<feature>/`
- API: `src/api/<feature>.ts` using `http` (`baseURL: /api`)
- Auth header from `src/utils/auth`
- Permissions: `v-perms` / `hasPerms()` matching backend permission strings
- `pnpm dev` (8848), `pnpm typecheck`, `pnpm lint`

## Web

- Page: `apps/web/src/app/<route>/page.tsx`
- API: `apps/web/src/lib/api.ts` + `httpClient.ts`
- Auth cookies: `token`, `tokenName`, `refreshToken`
- API base: `NEXT_PUBLIC_API_BASE_URL` (default `http://localhost:8081`)
- On HTTP error, read ProblemDetail `detail`
- `pnpm dev` (3000), `pnpm typecheck`, `pnpm lint`

## Do not

- Invent deleted `/system/menu` or `/system/role` clients.
- Hard-code enum numbers that disagree with `enums.yaml`.
- Commit secrets or production API hosts.
