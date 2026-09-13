# archive — close out a completed change

1. Confirm `tasks.md` all done and `execution-log.md` shows green verification.
2. Fold durable learnings outward:
   - recurring mistakes → `.agents/memory/pitfalls/<topic>.md` (+ index entry)
   - lasting conventions → `.agents/skills/` or `docs/specs/`
   - architectural decisions → `docs/adr/` (create ADR if none exists)
3. Mark the change `status: done` in its spec.md header.
4. The change dir stays in `.agents/changes/` as the permanent archive — do not delete.
