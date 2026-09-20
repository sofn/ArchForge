# archforge-cli

Developer CLI for the ArchForge workspace. Launch with `./archforge` (bash)
or `archforge.bat` (Windows) at the repo root — the wrapper rebuilds
`archforge-cli.jar` automatically when sources changed and checks JDK ≥ 25.

Global flags: `--verbose` echoes every external command; `-h/--help` and
`-V/--version` (from the jar manifest) work everywhere. Bare command groups
(`db`, `infra`, `skills`) print help and exit 0.

## Commands

```bash
./archforge init --write        # one-time setup: .env secrets + deps + flyway migrate
                                # (no --write = dry-run, nothing is persisted)
./archforge dev                 # infra deps + detached bootRun ×2 + pnpm dev ×2
                                # (pids in run/*.pid, logs in logs/)
./archforge up                  # containerized full stack: deps + migrate + app
                                # (-p dev|allinone|fulljre|jlink|native|staging|prod)
./archforge down [-v]           # kill dev processes + stop containers
                                # (-v also drops named volumes → data loss)
./archforge status              # dev pid liveness + compose ps (infra + stack)
./archforge logs [-f] [name]    # tail logs/*.log; --infra / --stack for containers
./archforge restart             # stop + start the dev stack
./archforge doctor              # check JDK/docker/compose/pnpm/node/ports/repos/.env
./archforge build [-p tag]      # bootBuildImage backend + docker build frontends
./archforge build --allinone    # single-image build → archforge:allinone
./archforge new <project>       # scaffold a Spring Boot 4 app on the published BOM
                                # (Gradle wrapper + archforge/ + spec/ + AI rules)
./archforge module new <name>   # add archforge-module-<name> inside a project
                                # (auto-included by flat-prefix scan)
./archforge meta export         # DB → project-definition/meta/*.yaml
./archforge meta import         # YAML → DB (dry-run diff; --apply writes)
./archforge meta check          # file ↔ DB drift gate (exit 1 on drift)
```

### All-in-one image (`archforge:allinone`)

`build --allinone` stages `docker/allinone/context/` (both bootJars +
ArchForgeAdmin `dist` + ArchForgeWeb `.next/standalone`) then builds one image
running **nginx + server-admin + server-web + next.js** under s6-overlay.
`up -p allinone` starts it with external postgres/redis
(`docker/docker-compose.allinone.yml`).

Ports: `:80` → C-end web (nginx → next.js), `:8088` → admin console
(nginx → SPA; `/api/*` → `server-admin:8080`). Browser never sees `:8081`
— web API calls stay on the same-origin BFF proxy inside next.js.

### `db` — dev database (postgres container)

```bash
./archforge db init              # postgres up + password sync + flyway migrate
./archforge db migrate           # apply pending Flyway migrations (alias: update)
./archforge db backup            # pg_dump → backup/db/archforge_<ts>.sql
./archforge db restore <file>    # restore dump into the app db (asks YES, -y skips)
./archforge db shell             # interactive psql
```

User/db names resolve from `DB_USERNAME` / `DB_NAME` (env → .env → `archforge`).

### `infra` — dependency containers only (docker-compose.infra.yml)

```bash
./archforge infra up                     # postgres + redis, waits healthy
./archforge infra up --db-password xxx   # explicit password override
./archforge infra status                 # compose ps
./archforge infra logs [-f]              # container logs
./archforge infra stop                   # pause
./archforge infra down [-v] [-y]         # remove containers (-v: volumes too, asks YES)
./archforge infra clean [-y]             # down -v + delete mounted local files
                                         # (docker/logs*, docker/allinone/context)
```

Password resolution order: `--db-password` > `DB_PASSWORD` env > `.env` >
generated 16-char (written to `.env`, with a WARN). After `up` the resolved
password is synced into the postgres role — named volumes keep the first-init
password otherwise. Non-generated passwords print masked (`abcd…wxyz`).

### `skills` — AI tool snippets

```bash
./archforge skills tools          # tool → target file mapping
./archforge skills install claude # append skill block (positional)
./archforge skills remove codex
```

(`skills update` and `skills list` remain as hidden aliases.)

### Misc

```bash
./archforge mcp                   # MCP stdio server (same as root --mcp)
./archforge generate-completion bash|zsh   # shell completion script
./archforge help <command>        # same as <command> --help
```

## Notes

- `.env` lives at the repo root and is git-ignored; `docker compose` consumes
  it via `--env-file` automatically.
- After `init`/`infra up`, load the env vars before `bootRun`/`java -jar`
  (see root README Quick start for Linux/Windows syntax).
- `dev` processes write `run/<name>.pid` + `logs/<name>.log`; `down` uses the
  pid files, so ports are actually freed.
- Building: `./gradlew :archforge-cli:shadowJar`.
