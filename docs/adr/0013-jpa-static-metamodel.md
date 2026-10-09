# 0013. Spring Data JPA + static metamodel instead of MyBatis

Date: 2026-10-08 (decision taken 2026-08; moved here from the docs site's ADR 0004)
Status: accepted

## Context

Comparable admin frameworks (RuoYi, JeecgBoot, AgileBoot) default to MyBatis / MyBatis-Plus, where XML mappers drift
from the entities. ArchForge already models its domain with JPA entities and Hibernate.

## Decision

Spring Data JPA + Hibernate static metamodel (`Entity_` classes) for type-safe field references, `QueryHelp` /
`SafeExpr` / JPA Specifications for dynamic filters, Flyway for the schema (`ddl-auto: validate` everywhere).

## Consequences

- Renaming a field breaks the compile instead of a query at runtime.
- One EntityManagerFactory per datasource group (ADR-0002); repositories are module-internal (ADR-0010).
- No MyBatis, no XML mappers, no `hbm2ddl.auto=update`.

## Alternatives considered

- MyBatis-Plus — rejected: mapper/entity drift and a second persistence model next to JPA.
- QueryDSL — used earlier, replaced by the metamodel + Specifications (one fewer code generator).
