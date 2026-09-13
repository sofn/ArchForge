# archforge-cli

Developer CLI for the ArchForge backend — picocli + Gradle Shadow fat jar,
no Spring runtime. Invoked via the repo-root launcher:

```bash
./archforge <command>          # Linux/macOS — builds the jar on first run
archforge.bat <command>        # Windows — same commands via gradlew.bat
```

## Command reference

### `init` — first-time setup

```bash
./archforge init --write                 # recommended for a fresh clone
./archforge init --write --profile dev   # dev is the default
```

Does three things for `dev`:

1. **Secrets** — generates `JWT_SECRET`, `DB_PASSWORD`, RSA key pair,
   `AES_KEY`. `--write` persists missing keys into `./.env`
   (idempotent: existing keys are kept; without `--write` it is a dry-run).
2. **Infra** — `docker compose -f docker/docker-compose.infra.yml up -d --wait`
   for postgres + redis, then applies Flyway migrations
   (`:archforge-server-admin:flywayMigrate`).
3. **DB password alignment** — resolves `DB_PASSWORD` (flag/env/.env/generated)
   and syncs it into the live postgres role via `ALTER USER`, so a reused
   data volume can never silently keep an old password.

### `infra` — dependency containers only

```bash
./archforge infra up                     # postgres + redis, waits healthy
./archforge infra up --db-password xxx   # explicit password override
./archforge infra stop                   # pause
./archforge infra down                   # remove containers
```

Password resolution order: `--db-password` > `DB_PASSWORD` env > `.env` >
generated 16-char (written to `.env`, with a WARN). After `up` the resolved
password is synced into postgres — see `init` above.

### `db` — database operations

```bash
./archforge db init        # start postgres + apply migrations
./archforge db update      # apply latest Flyway migrations only
./archforge db backup      # pg_dump -> backup/db/archforge_<timestamp>.sql
./archforge db recovery --file backup/db/xxx.sql          # interactive confirm
./archforge db recovery --file backup/db/xxx.sql --yes    # automation
```

### `up` / `down` — full stack (app + deps via compose)

```bash
./archforge up [--profile dev]   # dev = infra only + instructions
./archforge down [--profile dev]
```

### `docker` — business images

```bash
./archforge docker up [--profile prod]   # deps + migrate + app image
./archforge docker down [--profile prod]
./archforge build [--profile prod]       # build images
```

### `skills` — install agent skills into AI tools

```bash
./archforge skills list                      # supported tools
./archforge skills install --tool claude     # append managed block
./archforge skills update --tool claude      # re-apply snippets
./archforge skills remove --tool claude      # remove the block
```

### MCP server

```bash
./archforge --mcp    # stdio MCP server exposing CLI operations to AI agents
```

## Notes

- `.env` lives at the repo root and is git-ignored; `docker compose` consumes
  it via `--env-file` automatically.
- After `init`/`infra up`, load the env vars before `bootRun`/`java -jar`
  (see root README Quick start for Linux/Windows syntax).
- Building: `./gradlew :archforge-cli:shadowJar`.
