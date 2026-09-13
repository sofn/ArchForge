# propose — turn a requirement into a change spec

Input: a requirement, bug report, or idea.

1. Read `repos.yaml`, `AGENTS.md`, relevant `docs/specs/*` and `.agents/memory/pitfalls.md` first.
2. Investigate the codebase: every current-state claim must cite `path + Class.method`. No guessing.
3. Create `.agents/changes/<date>-<topic>/` from `_templates/`: fill `spec.md` and `tasks.md`.
4. Resolve open questions with the user BEFORE marking spec ready (HARD-GATE list must be fully checked).
5. STOP. Do not write implementation code. Wait for approval.
