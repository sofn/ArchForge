# AGENTS.md

## Git Commit Rules

- Do NOT append `Co-Authored-By` lines to commit messages.

## Workflow: Plan Before Execute

For every new requirement:

1. **Write a plan first** — save as a Markdown file under `/home/sofn/code/sofn/codeplans/ArchSmith/<date>-<topic>.md`
2. **Wait for user review** — do NOT start implementation until the user approves the plan
3. **Track progress in the plan** — update the plan file with status (pending / in_progress / done) after each step
4. **Push the codeplans repo** after execution is complete: `cd /home/sofn/code/sofn/codeplans && git add -A && git commit && git push`
