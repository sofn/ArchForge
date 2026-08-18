# AGENTS.md

## Git Commit Rules

- Do NOT append `Co-Authored-By` lines to commit messages.

## Workflow: Plan Before Execute

For every new requirement:

1. **Write a plan first** — save to `../codeplans/ArchForge/<date>-<topic>.md`
2. **Wait for user review** — do NOT start implementation until approved
3. **Track progress** — update plan file status after each step (pending / in_progress / done)
4. **Verify each step** — run `./gradlew build` after each change
5. **Verify before push** — run `./gradlew server-admin:bootRun` to confirm startup
6. **Push codeplans repo** after completion

## Agent Loop Files

- Do NOT create or keep `.agent-loop/` inside this repository.
- Place all agent-loop related files in `../codeplans/ArchForge/.agent-loop/`.

## Verification Checklist

Before claiming work is complete:

- [ ] `./gradlew build` passes (includes spotlessCheck + all tests)
- [ ] `./gradlew server-admin:bootRun` starts without errors
- [ ] No new deprecation warnings introduced
- [ ] Plan file updated with final status

## Project Context

This repository is part of the **ArchForge multi-repository project** (five
independent Git repositories, cloned side by side, no submodules). For the
machine-readable project map, read `../ArchForgeSpec/repos.yaml` first.

```
archforge/
├── ArchForge/          # backend (this repo): server-admin :8080 + server-web :8081
├── ArchForgeWeb/       # C-end web client (Next.js)  — consumes server-web :8081
├── ArchForgeAdmin/     # admin client (vue-pure-admin) — consumes server-admin :8080
├── ArchForgeDocs/      # documentation site (VitePress)
└── ArchForgeSpec/      # contracts / architecture / AI context
```

- Contracts are owned by `../ArchForgeSpec` (`api/openapi.yaml` OpenAPI 3.1,
  `schemas/` JSON Schema 2020-12). This repo implements them.
- Cross-repository behavior: read `../ArchForgeSpec/repos.yaml`, then
  `../ArchForgeSpec/architecture.md` before changing anything that affects the
  Web / Admin clients or the contract.
- Do not modify another repository unless explicitly required.
- This repo exposes two applications: `server-admin` (port 8080) and
  `server-web` (port 8081).
