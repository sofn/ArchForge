# 0003. Shared contracts live in common-base, never infrastructure

Date: 2026-09-12
Status: accepted

## Context

`SystemLoginUser`, `RoleInfo`, `AuthRequest`, `UserProvider` and the
enum-dictionary contract types lived in `archforge-infrastructure`, so domain
modules depended on infrastructure to compile — an inverted dependency edge
(infrastructure exists to serve domains, not the reverse).

## Decision

Types shared between a domain module and infrastructure sit in
`common-base`:

- `common.auth` — `AuthRequest`, `BaseLoginUser`, `SystemLoginUser`,
  `LoginInfo`, `RoleInfo`, `DataScopeEnum`, `UserProvider`
- `common.dictionary` — `EnumDictionary`, `EnumDictionaryItem`,
  `EnumDictionaryRegistry`, `DictionaryProperties`

Infrastructure consumes these; domains own them. `archforge-admin-user` no
longer depends on `archforge-infrastructure`.

## Consequences

- Putting a contract type into `infrastructure` re-inverts the edge — the
  ArchUnit layering rules (`domain ↛ infrastructure`) will fail the build.
- New shared SPI types go to `common-base` (or a dedicated contract module),
  chosen by which side defines the semantics.

## Alternatives considered

- A dedicated `archforge-contract` module — rejected: adds a module for ten
  classes; `common-base` is already the shared-kernel home.
- Leave types in infrastructure and let domains depend on it — rejected:
  that is the inversion this ADR removes.
