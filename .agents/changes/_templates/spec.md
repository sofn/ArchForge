# Spec: <topic>

Date: <YYYY-MM-DD> · Status: draft

## Background & Goals

<why this change exists; link issue/req if any>

## Current State (verified)

<each finding MUST cite `path/to/File.java` + `Class.method` — no unsourced claims>

## Scope

### In
- <feature/rule>

### Out
- <explicit non-goals>

## Business Rules

<invariants, edge cases, permission/audit requirements>

## Data & API Changes

- Contract: `spec/openapi.yaml` sections touched
- DB: Flyway migration needed? entities/columns
- Config: new `arch-forge.*` keys

## Risks

<security surface, rollback, cross-repo impact>

## Open Questions

- [ ] <question → owner>

## HARD-GATE Confirmations

- [ ] Contract-sync rule checked (openapi.yaml updated in same change)
- [ ] No deleted-API resurrection (`repos.yaml` → contract.deleted_paths)
- [ ] Verification matrix planned (per `../README.md`)
