# Decisions — session-level decision log

Architecture-level decisions belong in `docs/adr/`. This file logs smaller, session-made choices that future agents should not silently undo.

| Date | Decision | Context |
|------|----------|---------|
| 2026-09-13 | `.agents/` is the single canonical AI-asset dir; tool-specific configs are thin pointers | multi-tool compatibility (Devin/Claude/Codex/OpenCode) |
| 2026-09-13 | Change plans live in `.agents/changes/` inside the repo, not an external repo | external-repo references proved fragile (plans written to wrong path) |
| 2026-09-13 | sa-token super-admin wildcard is `"*"`, not `"*:*:*"` | per-segment literal matching fails on single-segment perms |
| 2026-09-13 | Meta-table import uses dual-layer defense: compatibility reason + independent fail-fast in `importTable` | preview path alone is refactorable away |
