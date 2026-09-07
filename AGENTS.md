# AGENTS.md

## Git Commit Rules

- Do NOT append `Co-Authored-By` lines to commit messages.

## Workflow: Plan Before Execute

For every new requirement:

1. **Write a plan first** — save to `../codeplans/ArchForge/<date>-<topic>.md`
2. **Wait for user review** — do NOT start implementation until approved
3. **Track progress** — update plan file status after each step (pending / in_progress / done)
4. **Verify each step** — run `./gradlew build` after each change
5. **Verify before push** — run `./gradlew :archforge-server-admin:bootRun` to confirm startup
6. **Push codeplans repo** after completion

## Agent Loop Files

- Do NOT create or keep `.agent-loop/` inside this repository.
- Place all agent-loop related files in `../codeplans/ArchForge/.agent-loop/`.

## Verification Checklist

Before claiming work is complete:

- [ ] `./gradlew build` passes (includes spotlessCheck + all tests)
- [ ] `./gradlew :archforge-server-admin:bootRun` starts without errors
- [ ] No new deprecation warnings introduced
- [ ] Plan file updated with final status

## Project Context

This repository is part of the **ArchForge multi-repository project** (three
independent Git repositories, cloned side by side, no submodules). For the
machine-readable project map, read `repos.yaml` first.

```
archforge/
├── ArchForge/          # backend + contracts (this repo)
│   ├── spec/           # openapi.yaml, enums.yaml, schemas/
│   ├── docs/specs/     # API / naming / error-code / security standards
│   ├── docs/architecture.md
│   └── skills/         # agent skills + backend standard
├── ArchForgeWeb/       # C-end web client (Next.js)  — consumes server-web :8081
└── ArchForgeAdmin/     # admin client (vue-pure-admin) — consumes server-admin :8080
```

- **This repo owns the contract**: `spec/openapi.yaml` (OpenAPI 3.1) and
  `spec/enums.yaml`. `spec/schemas/` holds JSON Schema 2020-12 definitions.
  The old `ArchForgeSpec` repository is retired — its contents live here now.
- Canonical backend standard: `skills/archforge-project-standard/standard.md`
  (pointer: `docs/specs/backend-standard.md`).
- Cross-repository behavior: read `repos.yaml`, then `docs/architecture.md`
  before changing anything that affects the Web / Admin clients or the contract.
- Do not modify another repository unless explicitly required.
- This repo exposes two applications: `server-admin` (port 8080) and
  `server-web` (port 8081).
- **Do not invent deleted APIs**: `/system/menu` and `/system/role` no longer
  exist (see `repos.yaml` → `contract.deleted_paths`).
- **Contract sync rule**: whenever the backend API changes (paths, parameters,
  request/response schemas, auth), update `spec/openapi.yaml` in the same
  change. CI diffs the live export against it and blocks breaking changes.
