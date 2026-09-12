# Architecture Decision Records

ADRs record **structural decisions that are expensive to reverse** — module
boundaries, persistence topology, analysis gates, contract ownership. They do
not record feature work, bugfixes, or tunable configuration.

## When to write one

Write an ADR when a change:

- adds/removes/merges a Gradle module, top-level package, or persistence unit
- changes a cross-module contract or a `@NamedInterface` boundary
- introduces or tightens a static-analysis / CI gate
- picks one of two plausible designs where the loser stays tempting

Do not write ADRs for: local refactors, dependency bumps, DTO/field changes,
test additions.

## Format

- File: `docs/adr/NNNN-kebab-title.md`, sequential number, never reuse.
- Use `0000-template.md`. Keep it under ~60 lines — the point is the *why*,
  not a second architecture doc.
- Status: `accepted` | `deprecated` | `superseded by NNNN`.
- An ADR is a snapshot of a decision at merge time. When reality changes,
  mark it `superseded` and write a new one — never edit history silently
  (typo/clarity fixes are fine).

## Index

| # | Title | Status |
|---|-------|--------|
| 0000 | [ADR template](0000-template.md) | — |
| 0001 | [Domain modules: api + internal only](0001-api-internal-layout.md) | accepted |
| 0002 | [One EntityManagerFactory per datasource group](0002-single-emf-per-datasource.md) | accepted |
| 0003 | [Shared contracts live in common-base, never infrastructure](0003-contracts-in-common-base.md) | accepted |
| 0004 | [NullAway enforced at ERROR on all source sets](0004-nullaway-error.md) | accepted |
| 0005 | [Service placement: api.service + internal.service, runtime via ports](0005-service-placement.md) | accepted |
