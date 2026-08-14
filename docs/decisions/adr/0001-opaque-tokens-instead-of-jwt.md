# 0001. Opaque tokens instead of JWT

Date: 2026-08-14 (retro-documented; decision predates this ADR)
Status: Accepted

## Context

Soar is rebuilt from yudao, which issues **JWT** access tokens. An admin platform with RBAC, tenant
isolation and an "admin bans a user" workflow needs one property JWT does not give for free:
**revocation takes effect on the next request**.

With a self-contained JWT, a token stays valid until it expires. Disabling a user, revoking a role, or
logging out cannot invalidate tokens already in the wild without adding a server-side deny list — at
which point the statelessness that motivated JWT is gone anyway.

## Decision

Issue **opaque tokens**: a UUID with no embedded claims, stored server-side.

- Persisted in PostgreSQL (`system_oauth2_access_token`, `system_oauth2_refresh_token`)
- Cached in Redis for the hot lookup path
- Resolved per request by `TokenAuthenticationFilter` in `soar-spring-boot-starter-security`, which
  materialises a `LoginUser` into both Spring Security's context and `WebFrameworkUtils`
- Expired rows reaped by a scheduled job (`OAuth2AccessTokenRepository.deleteByExpiresTimeLt`)

The token carries no information. Every authorization fact (permissions, roles, tenant, data scope) is
resolved server-side at request time.

## Alternatives considered

**JWT as yudao does.** Rejected: revocation requires a deny list, which reintroduces the state we were
avoiding while keeping JWT's downsides (token size, key rotation, claim staleness after a role change).

**JWT with a short TTL + refresh.** Rejected: shrinks the revocation window but does not close it, and
makes every permission change eventually-consistent by up to the TTL. Not acceptable for an admin
console where an operator expects "revoke" to mean now.

**Spring Session / server-side sessions.** Rejected: couples the API to a servlet session model; the
frontend is a separate origin SPA and the API is also consumed by `app-api` clients.

## Consequences

**Positive**

- Logout, ban, and role changes take effect on the very next request.
- Tokens are opaque to the client — no accidental reliance on embedded claims.
- Permission and tenant data are always fresh; no claim-staleness class of bug.

**Negative**

- Every authenticated request needs a token lookup. Mitigated by the Redis cache in front of the DB.
- Auth state is now a stateful dependency: Redis or PostgreSQL being down means no one authenticates.

**Risks**

- The token table grows without the cleanup job. It exists, but is a scheduled job — if it stops, the
  table grows silently.

## References

- `soar-framework/soar-spring-boot-starter-security/README.md` — full request flow, filter placement, dual storage
- `soar-module-system/.../dal/postgres/oauth2/OAuth2AccessTokenRepository.java`
- `CONVENTIONS.md § Style Note` — the yudao/Soar comparison row this ADR expands
