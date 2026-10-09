# 0012. Four sibling repositories, not a monorepo

Date: 2026-10-08 (supersedes the docs site's ADR 0003 "Five sibling repos")
Status: accepted

## Context

A monorepo would put Java, Vue, Next.js, VitePress and the OpenAPI contract in one tree: one release cadence, one
CI image, and every agent loading everything. ArchForge started as five repositories; the fifth, ArchForgeSpec,
held the contract and the standards and drifted from the code it described.

## Decision

Four Git repositories cloned side by side, no submodules: **ArchForge** (backend; owns the contract in `spec/`,
the standards in `docs/specs/` and the AI assets in `.agents/`), **ArchForgeAdmin**, **ArchForgeWeb** and
**ArchForgeDocs**. ArchForgeSpec is retired — its content lives in ArchForge. `repos.yaml` in ArchForge is the
machine-readable map; each repo's `AGENTS.md` states what it may modify.

## Consequences

- The contract is generated from the backend (`./gradlew generateOpenApi`) and checked by the clients' CI.
- The docs site describes the other repos; it is never a contract source.
- Anything that still reads ArchForgeSpec reads a frozen, outdated copy.

## Alternatives considered

- Monorepo — rejected for the coupling above.
- Keeping a separate spec repository — rejected: a contract maintained apart from the code that serves it drifts.
