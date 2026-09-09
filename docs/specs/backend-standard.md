# Backend standard

The canonical backend standard lives **next to the code** in the same
repository — this doc is only a pointer to it.

**Read:** `skills/archforge-project-standard/standard.md`
([link](../../skills/archforge-project-standard/standard.md))

That file owns Java / Gradle / testing / deployment conventions for `ArchForge`.

## Dual source (do not fork)

| Tree | Role | Audience |
|------|------|----------|
| `docs/specs/` | **Gate**: short, CI-consumed, cross-repo (paths, enums, naming, security) | humans + CI |
| `skills/archforge-project-standard/standard.md` | **Tutorial**: full-stack backend panorama for agents | AI |

`docs/specs/backend-standard.md` is only a pointer. Do not copy `standard.md`
into `docs/specs/`. When a rule is enforced by CI, put it (or a one-line
pointer) under `docs/specs/`; keep the longer explanation in `standard.md`.

## Decision

Keep `standard.md` in the `skills/` tree so it versions with the code it
describes. This spec **references** it; it does not copy or fork it.

When the backend standard changes, update `skills/archforge-project-standard/standard.md`.
Only add a spec under `docs/specs/` when the rule is cross-repo (paths, enums,
response format, naming across clients).
