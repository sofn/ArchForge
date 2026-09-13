# apply — execute an approved change spec

Precondition: `.agents/changes/<name>/spec.md` is approved. If reality diverges from spec, update spec first (Spec is Truth).

Per task in `tasks.md` order:
1. Implement exactly the listed files/signatures — no scope creep.
2. Verify per the per-edit protocol in `AGENTS.md` (`compileJava` + `checkstyleMain` minimum).
3. Append a block to `execution-log.md`: commands, real output, findings, skips.
4. Update task status in `tasks.md`.

Never claim "passed" without evidence in execution-log. Never batch unverified edits.
