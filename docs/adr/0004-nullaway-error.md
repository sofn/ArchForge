# 0004. NullAway enforced at ERROR on all source sets

Date: 2026-09-12
Status: accepted

## Context

NullAway ran at WARN severity with a ~500-warning baseline — noise that hid
roughly 50 real NPE risks and let new violations land silently.

## Decision

NullAway is `CheckSeverity.ERROR` for every `compileJava` /
`compileTestJava`. `@Nullable` annotations are the single source of truth
for nullability contracts. Supporting configuration:

- `NullAway:ExcludedFieldAnnotations` covers framework-injected fields
  (Spring DI, JPA, picocli binding) — read as one comma-separated flag value.
- Root `lombok.config` sets
  `lombok.copyableAnnotations += org.jspecify.annotations.Nullable` so
  generated getters/setters inherit the field's contract.
- Generated sources (`build/generated/sources/**`, MapStruct impls) are
  excluded via `XepExcludedPaths`.

## Consequences

- A nullness violation anywhere fails the compile — zero-warning budget.
- When a callee is marked `@Nullable`, callers that assumed non-null break:
  fix by null-checking, `Objects.requireNonNull`, or narrowing — never by
  removing the annotation to silence the error.
- Tests annotate honestly too; `requireNonNull` at deref sites is the idiom.

## Alternatives considered

- Per-module WARN→ERROR ratchet lists — used during the cleanup, deleted on
  completion; keeping them would have left a permanent two-speed gate.
- `@SuppressWarnings` on noisy call sites — rejected: suppressing the
  annotation instead of the contract erases information.
