# 0002. One Maven module per domain, layered inside — no api/biz split

Date: 2026-08-14 (retro-documented; decision predates this ADR)
Status: Accepted

## Context

yudao splits every business domain into **two** Maven modules: `yudao-module-system-api` (interfaces +
DTOs consumed by other modules) and `yudao-module-system-biz` (implementation). The split exists to
enforce that cross-module callers only see the API surface, and to keep the door open for extracting
modules into separate services.

Soar is a monolith built to learn enterprise patterns, not a system heading for microservice
extraction. The split doubles the module count, doubles POM maintenance, and forces a DTO to be
declared in a module that cannot see the entity it maps from.

## Decision

**One Maven module per domain**, layered internally:

```
Controller → Service → Repository
```

Cross-module calls go through `*CommonApi` **interfaces declared in `soar-common`** and implemented in
each module's `api/{domain}/` package as a `@Service`. There is no Feign, no remote call, no second
module.

Current modules: `soar-module-system`, `soar-module-infra`, `soar-module-pay` — all Layered.

**Architecture style per module**:

- System, infra, pay: Layered. No DDD.
- Greenfield business modules (e.g. a future logistics domain): DDD / simplified hexagonal — domain
  aggregates, use cases, adapters. This is an **intent**; no module demonstrates it yet.

`soar-module-pay` is a yudao port and deliberately kept yudao's layered shape rather than being
reworked into DDD during the port.

## Alternatives considered

**Mirror yudao's api/biz split.** Rejected: pays the cost of service extraction without the benefit.
The `*CommonApi`-in-`soar-common` arrangement already gives the encapsulation the split was protecting.

**Single module for everything.** Rejected: loses the compile-time boundary that stops `infra` from
reaching into `system` internals.

**DDD everywhere from the start.** Rejected: system/infra are CRUD-shaped admin domains where an
aggregate layer is pure overhead. Reserved for domains with real invariants.

## Consequences

**Positive**

- Half the modules, half the POMs, straightforward navigation.
- A DTO lives next to the entity it maps from; MapStruct mappers compile without cross-module gymnastics.
- Porting from yudao stays mechanical — same layering, fewer files.

**Negative**

- Nothing physically prevents a module from importing another module's internals; the boundary is
  convention plus `*CommonApi` discipline, not the compiler.
- If a module ever does need extraction, the api/biz split has to be introduced then.

**Follow-ups**

- The DDD intent for greenfield modules is undemonstrated. The first such module should get its own ADR
  rather than inheriting this one by default.

## References

- `AGENTS.md § Module Structure`, `§ Per-module architecture`
- `CONVENTIONS.md § Package Structure`
- `docs/architecture.md § Module inventory`
