# 0004. Active multi-tenancy via Hibernate `@TenantId`

Date: 2026-08-14 (retro-documented; decision predates this ADR)
Status: Accepted

## Context

yudao implements multi-tenancy by intercepting SQL and appending `tenant_id = ?` through a MyBatis-Plus
inner interceptor, with an annotation to opt out. Soar has no MyBatis, and a shared-schema model was
chosen over schema- or database-per-tenant for operational simplicity.

Hibernate 6 ships a first-class discriminator-based mechanism: annotate a field with `@TenantId`,
supply a `CurrentTenantIdentifierResolver`, and Hibernate both **filters reads** and **populates writes**
automatically.

## Decision

Use Hibernate 6's native `@TenantId` discriminator, wired end to end — not a stub, not advisory.

- `TenantBasePO` carries the `@TenantId Long tenantId` field. Tenant-scoped tables extend it; global
  tables (menu, oauth2 client, infra config, files) extend `BasePO` instead.
- `SoarTenantIdentifierResolver` implements `CurrentTenantIdentifierResolver<Long>` and reads
  `TenantContextHolder`.
- `TenantContextWebFilter` extracts the `tenant-id` request header; `TenantSecurityWebFilter` rejects
  requests that should carry one and do not.
- Escape hatches: `@TenantIgnore` (AOP on a method, or on a controller class to auto-register the URL as
  tenant-ignored at startup) and `TenantUtils.executeIgnore(...)` for a code block.

The field is **never set by application code** — `TenantBasePO`'s Javadoc says so explicitly. Hibernate
populates it on insert; jobs that must span tenants iterate tenants explicitly (see `DemoTenantJob`).

## Alternatives considered

**Manual `tenant_id` predicates in every query / Specification.** Rejected: one forgotten predicate is a
cross-tenant data leak, and there is no way to enforce it at review time.

**Port yudao's interceptor approach onto Hibernate (custom `StatementInspector`).** Rejected for tenancy
specifically: Hibernate already solves this natively and correctly, including the write path. (Note that
data permission *did* take the `StatementInspector` route — see BE ADR 0007 — because Hibernate has no
equivalent native mechanism for role-scoped row filtering.)

**Schema-per-tenant or database-per-tenant.** Rejected: migration and connection-pool complexity out of
proportion to the project's goals.

## Consequences

**Positive**

- Filtering and population are automatic and uniform — including on paths nobody remembered to review.
- The write path is covered, which manual predicates typically miss.
- Opt-out is explicit and greppable (`@TenantIgnore`, `executeIgnore`).

**Negative**

- Global vs tenant-scoped is decided by which base class an entity extends — a decision made once at
  entity creation and easy to get wrong.
- Background work (jobs, MQ consumers, async callbacks) has no ambient tenant. Code must set or iterate
  tenants deliberately; `PayNotifyServiceImpl` carries `tenantId` explicitly across an async boundary
  for exactly this reason.

**Risks**

- `@TenantIgnore` on a controller both clears the security filter and sets
  `TenantContextHolder.setIgnore(true)`, which disables Hibernate filtering for that request. Correct
  for global data, dangerous if applied to a tenant-scoped endpoint by mistake.

## References

- `soar-framework/soar-spring-boot-starter-biz-tenant/.../core/db/TenantBasePO.java`
- `soar-framework/soar-spring-boot-starter-biz-tenant/.../core/db/SoarTenantIdentifierResolver.java`
- `soar-framework/soar-spring-boot-starter-biz-tenant/.../config/SoarTenantAutoConfiguration.java`
- `AGENTS.md § Multi-Tenancy (active)`
- BE ADR 0008 — tenant filtering for cross-cutting *assignment* operations, which `@TenantId` cannot cover
