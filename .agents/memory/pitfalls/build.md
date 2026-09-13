# Pitfalls — build / Gradle / local run

- **`bootRun` can go UP-TO-DATE after a failed start** and silently skip a real restart. For manual smoke runs prefer `bootJar` + `java -jar build/libs/*.jar`.
- **`--enable-preview` is a JVM flag**: it must come BEFORE `-jar` (`java --enable-preview -jar app.jar`), otherwise `UnsupportedClassVersionError`.
- **Rebuilding a jar overwrites the file a running process mapped** → `ClassNotFoundException` on inner classes. Kill the old process before/after `bootJar`, then restart.
- **`pkill -f java -jar` can match your own shell command line** and kill the shell. Use `pgrep -f` + explicit PID, or `jps`.
- **Dev datasource needs `DB_PASSWORD` env** — the compose PG password, not a guess; missing it surfaces as `PSQLException` at startup.
