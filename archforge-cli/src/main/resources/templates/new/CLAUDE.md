# CLAUDE.md

Spring Boot 4 / JDK 25 app on the ArchForge BOM (`com.lesofn.archforge`).

- Build: `./gradlew build` · Run: `./gradlew bootRun`
- Modules: `archforge module new <name>` → flat `archforge-module-*` dirs,
  auto-included by `settings.gradle.kts` (flat-prefix scan)
- Config prefix: `arch-forge.*` (`application.yaml`)
- Contract: `spec/openapi.yaml`
