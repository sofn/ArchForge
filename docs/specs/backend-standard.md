# Backend standard

The canonical backend standard lives **next to the code** in the same
repository — this doc is only a pointer to it.

**Read:** `skills/archforge-project-standard/standard.md`
([link](../../skills/archforge-project-standard/standard.md))

That file owns Java / Gradle / testing / deployment conventions for `ArchForge`.

## Decision

Keep `standard.md` in the `skills/` tree so it versions with the code it
describes. This spec **references** it; it does not copy or fork it.

When the backend standard changes, update `skills/archforge-project-standard/standard.md`.
Only add a spec under `docs/specs/` when the rule is cross-repo (paths, enums,
response format, naming across clients).
