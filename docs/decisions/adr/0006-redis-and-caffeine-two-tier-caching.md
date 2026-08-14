# 0006. Two cache technologies: Redis for data, Caffeine for live objects

Date: 2026-08-14 (retro-documented; decision predates this ADR)
Status: Accepted

## Context

Redis is already a hard dependency (opaque token storage, see BE ADR 0001), so it is the obvious cache.
But some things worth caching **cannot be serialised**: the file-storage layer caches an initialised
`FileClient` per config, and an S3 client holds live AWS SDK objects, connection pools and credentials
providers. Those cannot round-trip through Redis, and their identity matters — two nodes each holding
their own client is correct, not a cache-coherency bug.

## Decision

Two cache technologies, split by what is being cached:

| Cache | Used for | Examples |
| ----- | -------- | -------- |
| **Redis** (`@Cacheable` / `CacheManager` from `soar-spring-boot-starter-redis`) | Serializable data shared across nodes | tokens, role/menu permission sets, dict data |
| **Caffeine** (in-memory, per node) | Non-serializable live objects, and hot local lookups | `FileClient` instances, dict lookups in `DictFrameworkUtils`, tenant lookups in `TenantFrameworkServiceImpl` |

Caffeine caches are built through `CacheUtils.buildAsyncReloadingCaffeine(...)` in `soar-common`, which
gives asynchronous refresh — a stale value is served while the reload happens, so a cache miss never
blocks a request thread.

Guava's cache is being phased out in favour of Caffeine; it still appears in roughly a dozen files and
the refactor is deferred.

## Alternatives considered

**Redis only.** Not possible for the live-object cases — an S3 client is not serialisable, and making it
so would mean re-initialising the client on every cache hit, which defeats the purpose.

**Caffeine only.** Rejected: token and permission data must be shared across nodes; a per-node cache
would make revocation node-local, undermining BE ADR 0001.

**Keep Guava instead of adopting Caffeine.** Rejected: Caffeine is Guava cache's maintained successor
with a better eviction policy and native async reloading.

## Consequences

**Positive**

- Each cache is used for what it is good at; no attempt to serialise things that cannot be serialised.
- Async reloading keeps request threads off the slow path.

**Negative**

- Two caching APIs in one codebase; developers must know which applies. The rule "serializable → Redis,
  live object → Caffeine" is the deciding question.
- Caffeine caches are per node, so a config change needs a mechanism to reach other nodes (or tolerate
  the refresh interval).

**Follow-ups**

- Finish the Guava → Caffeine migration (~13 files remaining).
- Cache invalidation correctness is tracked separately: see `CONVENTIONS.md § Missing @CacheEvict audit
  checklist`, which requires every `@Cacheable` method to document the mutations that must evict it.

## References

- `soar-framework/soar-common/.../util/cache/CacheUtils.java`
- `soar-module-infra/.../service/file/FileConfigServiceImpl.java` — live `FileClient` cache
- `soar-framework/soar-spring-boot-starter-redis/README.md`
- `CONVENTIONS.md § Caching`
