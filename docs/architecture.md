# ArchForge architecture

Three independent Git repositories, cloned side by side. No submodules.

```
archforge/
├── ArchForge/          # backend + contracts (this repo)
│   ├── spec/                     # openapi.yaml, enums.yaml, schemas/
│   ├── docs/specs/               # API / naming / error-code / security standards
│   ├── .agents/                  # canonical AI assets (skills, memory, changes)
│   ├── archforge-server-admin    :8080
│   └── archforge-server-web      :8081
├── ArchForgeAdmin/     # admin UI (Vite)         :8848
└── ArchForgeWeb/       # C-end Next.js           :3000
```

The retired `ArchForgeSpec` repository was merged into `ArchForge`: its contract,
specs and skills now live in `spec/`, `docs/specs/` and `.agents/skills/`.

Machine-readable map: [`repos.yaml`](../repos.yaml).

## Runtime map

| Process | Repo | Port | Talks to |
|---------|------|------|----------|
| `archforge-server-admin` | ArchForge | **8080** | PostgreSQL, Redis, object storage |
| `archforge-server-web` | ArchForge | **8081** | same infra, separate sa-token realm |
| ArchForgeAdmin (Vite) | ArchForgeAdmin | **8848** | proxies `/api` → `:8080` |
| ArchForgeWeb (Next.js) | ArchForgeWeb | **3000** | `NEXT_PUBLIC_API_BASE_URL` → `:8081` |

```
Browser :8848 ──/api──► server-admin :8080     envelope {code,message,data}
Browser :3000 ─────────► server-web  :8081     errors as ProblemDetail
```

Do not point Admin at `:8081` or Web at `:8080`.

## Deployment topology

Two shapes (see [ADR-0006](adr/0006-dual-process-topology.md) — packaging unites, processes do not):

**All-in-one** (`archforge:allinone`, `./archforge build --allinone` + `up -p allinone`) —
one container, s6-overlay supervises four processes; postgres/redis stay external:

```
                    ┌──────────────── archforge:allinone ───────────────┐
Browser ──:80─────►│ nginx ──► next.js :3000 ──BFF──► server-web :8081 │
Browser ──:8088───►│ nginx ──► admin SPA (static)                     │
        :8088/api─►│ nginx ──► server-admin :8080                     │
                    └──────────────────────────────────────────────────┘
```

- `:80` → C-end web; the browser never sees `:8081` (same-origin BFF proxy).
- `:8088` → admin console; `:8088/api/*` strips prefix → `server-admin` (vite-parity).
- `:8080` / `:8081` / `:3000` are container-internal only.

**Split** (`docker-compose.prod.yml` / `staging`): `backend` (:8080) +
`backend-web` (:8081) + `frontend` (admin SPA) as separate services —
independent scale/release; the C-end web deploys its own Next.js.

## Backend modules

See `repos.yaml` for the full list. Grouping:

| Group | Modules |
|-------|---------|
| Common | `archforge-common-base`, `archforge-common-error`, `archforge-common-jpa` |
| Domain | `archforge-admin-user`, `archforge-blog`, `archforge-meta-table` |
| Apps | `archforge-server-admin`, `archforge-server-web`, `archforge-cli` |
| Infra | `archforge-infrastructure`, `archforge-dependencies` |
| Starters | `archforge-cache-starter`, `archforge-lock-starter`, `archforge-redisson-starter`, `archforge-trace-starter` |
| Example | `archforge-example-task` (unlinked; not a runtime dependency of either server) |

Dependency flow is top-down: servers → infrastructure + domain → common. Domain modules never depend on a server module.

Backend coding conventions stay in the backend repo. This spec only [references](specs/backend-standard.md) them.

## Auth

Both servers use **sa-token** (`Authorization: Bearer <token>`):

- Admin: `StpAdminUtil` (type `admin`)
- Web: `StpWebUtil` (type `web`)

Realms are separate. An admin token is not a C-end token.

## Specs

| Spec | Topic |
|------|--------|
| [specs/api-response.md](specs/api-response.md) | Dual response format: admin envelope vs web ProblemDetail |
| [specs/api-path.md](specs/api-path.md) | `/admin/*` and `/web/*`; leftover `/system/dict` |
| [specs/security.md](specs/security.md) | sa-token, permission, rate limit, XSS, CORS, secrets |
| [specs/naming.md](specs/naming.md) | `Sys*` vs domain services; `sys_` / `blog_` tables |
| [specs/enum-sync.md](specs/enum-sync.md) | Backend enum → `enums.yaml` → frontend |
| [specs/error-codes.md](specs/error-codes.md) | Module error-code formula |
| [specs/directory.md](specs/directory.md) | Backend module prefix + frontend dirs |
| [specs/backend-standard.md](specs/backend-standard.md) | Pointer to canonical backend standard |
| [specs/flyway.md](specs/flyway.md) | V5/V19 history gaps and `*:missing` |

Related:

- [spec/openapi.yaml](../spec/openapi.yaml) — HTTP contract (stub; do not reintroduce deleted paths)
- [spec/enums.yaml](../spec/enums.yaml) — shared enumerations
- [.agents/skills/index.yaml](../.agents/skills/index.yaml) — agent skills

## Change rule

Cross-repo work: read `repos.yaml` → this file → the relevant spec → the matching skill in [`.agents/skills/`](../.agents/skills/index.yaml). Raise contract changes here before hacking a client.
