# archforge-cli

ArchForge developer CLI. Independent of Spring. Fat-jar via Gradle Shadow.

```bash
./archforge --help
./archforge init --write
./archforge infra up
./archforge db backup
./archforge skills install --tool claude
./archforge --mcp
```

If `archforge-cli/build/libs/archforge-cli.jar` is missing, `./archforge` builds it first.
