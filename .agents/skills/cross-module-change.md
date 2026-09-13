# Skill: cross-module change

Use when a change touches the contract plus its implementation, or spans more
than one of ArchForge / ArchForgeAdmin / ArchForgeWeb.

The contract used to live in a separate `ArchForgeSpec` repository. It now lives
in this repo at `spec/openapi.yaml`, so a contract change and its backend
implementation belong in the **same** commit.

## Order

```
1. spec/openapi.yaml (+ spec/enums.yaml)   contract / enum
2. Backend implementation                  same commit as 1
3. ArchForgeAdmin and/or ArchForgeWeb      regenerate types, own commit
```

Never start in a client and "make the backend fit later".

## Checklist

- [ ] Read [`../repos.yaml`](../repos.yaml) and
      [`../docs/architecture.md`](../docs/architecture.md)
- [ ] Paths follow [`../docs/specs/api-path.md`](../docs/specs/api-path.md)
- [ ] Response format follows
      [`../docs/specs/api-response.md`](../docs/specs/api-response.md)
- [ ] Enums updated in [`../spec/enums.yaml`](../spec/enums.yaml)
- [ ] `spec/openapi.yaml` updated in the **same commit** as the backend change
- [ ] CI: `oasdiff` breaking check against the target branch passes
- [ ] Frontend clients regenerate types (`pnpm gen:api`) and commit in their own
      repo, one commit each (no submodule, no `Co-Authored-By`)
- [ ] Do not modify a sibling repo unless the task requires it

## Ports (do not swap)

`8080` admin API · `8081` web API · `8848` admin UI · `3000` C-end UI
