# AGENTS.md

## Git Commit Rules

- Do NOT append `Co-Authored-By` lines to commit messages.

## Workflow: Plan Before Execute

For every new requirement:

1. **Write a plan first** — save to `/home/sofn/code/sofn/codeplans/ArchSmith/<date>-<topic>.md`
2. **Wait for user review** — do NOT start implementation until approved
3. **Track progress** — update plan file status after each step (pending / in_progress / done)
4. **Verify each step** — run `./gradlew build` after each change
5. **Verify before push** — run `./gradlew server-admin:bootRun` to confirm startup
6. **Push codeplans repo** after completion

## Verification Checklist

Before claiming work is complete:

- [ ] `./gradlew build` passes (includes spotlessCheck + all tests)
- [ ] `./gradlew server-admin:bootRun` starts without errors
- [ ] No new deprecation warnings introduced
- [ ] Plan file updated with final status
