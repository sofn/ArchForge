# .agents/changes/ — Change-archive workflow

One directory per non-trivial change: `.agents/changes/<date>-<topic>/`

## File set (templates in `_templates/`)

| File | Contents |
|---|---|
| `spec.md` | Background & goals / current-state findings (every claim cites `file path + Class.method`) / feature list / business rules / data & API changes / risks / open questions / HARD-GATE confirmations |
| `tasks.md` | Task breakdown ordered: data model → API contract → low-level impl → orchestration → entry points. Each task names exact file paths + function signatures; atomic at 3–5 files |
| `test-spec.md` | Verification scope matrix, cases, and skip list for this round |
| `execution-log.md` | Appended per round: timestamp / changed files / commands + results / API or DB findings / whether warnings block / skipped items + reasons / service PID started this round |
| `verification/` | Evidence attachments — screenshots, logs (optional) |

## Rules

- **No Spec No Code**: do not touch code before the spec is confirmed (trivial fixes/docs exempt)
- **Spec is Truth**: if reality diverges from spec mid-execution, fix the spec first, then the code
- **Incremental verification**: reuse existing test-spec/execution-log; append only this round's delta — never rewrite the baseline
- **Evidence over claims**: never write "passed" without the actual command and key output

## Lifecycle

`propose` → user approves → `apply` (task loop with per-step verification) → `review` → `test` → merge → `archive` (fold durable learnings into memory/skills, mark change done)
