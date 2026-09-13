# test — run the verification matrix for a change

1. Execute `test-spec.md` scope matrix for the change dir.
2. Minimum gates: `./gradlew spotlessApply compileJava compileTestJava checkstyleMain checkstyleTest forbiddenApisMain spotbugsMain` + targeted tests.
3. If Docker is available: run Testcontainers ITs + `:archforge-server-admin:bootRun` smoke.
4. Append every command + result to `execution-log.md` (including failures and retries).
5. Update `test-spec.md` status; mark skipped cases with reasons.
