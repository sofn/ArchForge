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
                                # (-p dev|fulljre|jlink|native|staging|prod)
./archforge down [-v]           # kill dev processes + stop containers
                                # (-v also drops named volumes → data loss)
./archforge status              # dev pid liveness + compose ps (infra + stack)
./archforge logs [-f] [name]    # tail logs/*.log; --infra / --stack for containers
./archforge restart             # stop + start the dev stack
./archforge doctor              # check JDK/docker/compose/pnpm/node/ports/.env
./archforge build [-p tag]      # bootBuildImage backend + docker build frontends
```

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
                                         # (hidden alias: infra clean = down --volumes)
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
