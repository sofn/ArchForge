# review — independent review of implemented changes

Use the `.agents/agents/spec-reviewer.md` or `code-quality-reviewer.md` persona.

- Reviewer is read-only: verify claims by reading code, never by trusting execution-log alone.
- Check: spec coverage (every feature implemented?), contract sync (openapi.yaml), security rules, naming/style, test coverage vs test-spec.
- Output: findings list with severity (blocker / should-fix / nit) and `file:line` references.
- Blockers must be resolved and re-verified before merge.
