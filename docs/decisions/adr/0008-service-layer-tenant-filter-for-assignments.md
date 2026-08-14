# 0008. Tenant filtering for assignment operations belongs in the service layer

Date: 2026-08-14 (retro-documented; decision predates this ADR)
Status: Accepted

## Context

Hibernate's `@TenantId` (BE ADR 0004) filters **tenant-scoped tables**. It cannot help with a whole class
of operations that grant access to **global** resources:

- assign menus to a role (`system_menu` is global — `BasePO`, no `tenant_id`)
- assign departments to a role's data scope
- assign roles to a user

A tenant subscribes to a *package* that enumerates which menus it may use. Nothing in the tenant
discriminator stops tenant A from POSTing menu IDs belonging to tenant B's package — the menu rows are
global and visible to everyone.

yudao solves this **in the controller**: the controller calls a tenant helper that mutates the incoming
request DTO's ID collection in place, then hands the trimmed request to the service.

## Decision

Apply the tenant-package filter **in the service layer**, immutably.

```java
@Override
@Transactional(rollbackFor = Exception.class)
public void assignRoleMenu(Long roleId, Set<Long> menuIds) {
    Set<Long> safe = CollUtil.emptyIfNull(menuIds);

    // Multi-tenant safety: drop any menu outside the current tenant's package.
    // Null means "no filter applies" (system tenant or tenancy disabled).
    Set<Long> tenantMenuIds = tenantService.getTenantMenuIds();
    if (tenantMenuIds != null) {
        safe = safe.stream().filter(tenantMenuIds::contains).collect(Collectors.toSet());
    }
    // ... diff-based assign
}
```

`tenantService.get<Resource>Ids()` returns `null` to mean **"no filter applies"** (system tenant, or
tenancy disabled) — distinct from an empty set, which would mean "nothing allowed". Every caller must
handle the `null` branch; the contract is stated in `TenantService`'s Javadoc.

The filter produces a **new** collection rather than mutating the caller's.

## Alternatives considered

**yudao's controller-level filter.** Rejected on two counts. It mutates the request DTO, so the object
the framework deserialised no longer matches what the client sent — confusing in logs and in any
downstream use. And it is bypassable: any internal caller (bulk import, migration, scheduled job,
another service) reaches the service directly and skips the filter entirely.

**An AOP aspect on assignment methods.** Rejected: which parameter holds the IDs and which resource type
they refer to differs per method, so the aspect would need per-method configuration — more machinery
than the three lines it replaces, and less visible at the call site.

**Validate and reject instead of silently filtering.** Considered. Rejected to stay behaviour-compatible
with yudao, where an out-of-package ID is dropped rather than failing the request. Worth revisiting: a
silent drop means an operator sees the save succeed and the permission not appear.

## Consequences

**Positive**

- The invariant ("a tenant cannot grant assets outside its package") lives with the domain logic that
  owns it, so every caller is protected, not just the HTTP one.
- No mutation of caller-owned collections.

**Negative**

- Deliberate divergence from yudao — porting an assignment method requires moving the filter, not
  copying it.
- Repeated in each assignment method rather than centralised.

**Risks**

- Silent filtering hides misconfiguration from the operator. Revisit if it causes support confusion.
- A new assignment method that forgets the filter has no automated guard.

## References

- `soar-module-system/.../service/permission/PermissionServiceImpl.java` — role-menu assignment
- `soar-module-system/.../service/permission/MenuServiceImpl.java` — tenant-package filtering on read
- `soar-module-system/.../service/tenant/TenantService.java` — the `null` = no-filter contract
- `CONVENTIONS.md § Service-layer tenant filter` — the pattern and anti-pattern
- BE ADR 0004 — what `@TenantId` covers, and why it does not cover this
