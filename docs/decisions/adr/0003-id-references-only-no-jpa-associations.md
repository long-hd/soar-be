# 0003. ID references only — no JPA associations

Date: 2026-08-14 (retro-documented; decision predates this ADR)
Status: Accepted

## Context

Soar uses Spring Data JPA + Hibernate 6, while the reference implementation (yudao) uses MyBatis, where
object graphs do not exist — a `AdminUserDO` simply holds `deptId`, and joins are written by hand.

The natural JPA instinct is to model `AdminUserPO.dept` as `@ManyToOne`. That instinct interacts badly
with three things this codebase relies on:

1. **SQL rewriting.** Both multi-tenancy (`@TenantId`) and data permission
   (`DataPermissionStatementInspector`) inject predicates into emitted SQL. Lazy-loading proxies fire
   queries at unpredictable points — including outside the annotated method whose `@DataPermission`
   context was supposed to govern them.
2. **Porting fidelity.** Every yudao service is written against ID fields. Introducing associations
   makes each port a redesign instead of a translation.
3. **Predictability.** Association mapping brings N+1, cascade semantics, `LazyInitializationException`,
   and fetch-plan tuning — a large surface for a CRUD admin backend.

## Decision

**One entity = one table.** Entities (`*PO`) hold foreign keys as plain `Long` fields.

`@ManyToOne`, `@OneToMany`, `@ManyToMany` and `@OneToOne` are not used anywhere.

```java
@Entity
@Table(name = "system_users")
public class AdminUserPO extends TenantBasePO {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "dept_id")
    private Long deptId;      // ID reference — never @ManyToOne
}
```

Assembling related data is the **service or controller's** job: fetch the ID set, load the related
entities in one call, build a map, and merge into the response DTO.

```java
Set<Long> deptIds = pageResult.getList().stream().map(UserPO::getDeptId)...;
Map<Long, DeptPO> deptMap = deptService.getDeptMap(deptIds);
```

## Alternatives considered

**Full JPA associations with `FetchType.LAZY` + entity graphs.** Rejected: reintroduces lazy-load
queries that escape the `@DataPermission` / tenant-ignore context, and every response shape then needs
a fetch plan.

**Associations only for "safe" small lookups (e.g. dept).** Rejected: no stable line between safe and
unsafe; a single association normalises the pattern and the rest follow.

**DTO projections via JPQL joins.** Not rejected outright — allowed where a specific read genuinely
needs it — but not the default, because it puts SQL shape decisions in the repository layer where the
data-permission walker's limitations are easier to trip.

## Consequences

**Positive**

- Emitted SQL is predictable, which is a precondition for the tenant and data-permission rewriters to
  be reasoned about at all.
- No N+1, no `LazyInitializationException`, no cascade surprises.
- yudao ports stay mechanical.

**Negative**

- Manual assembly boilerplate in services and controllers (the `deptMap` pattern above repeats).
- No database-level referential navigation in code; developers must know which ID points where. Entity
  field Javadoc is expected to say so.

**Risks**

- A well-meaning contributor adds one `@ManyToOne` and the guarantees erode silently. This is listed in
  `AGENTS.md § Don't` and in the verification checklist for that reason.

## References

- `AGENTS.md § JPA Entity Rules (CRITICAL)`
- `CONVENTIONS.md § Entity Pattern (*PO)`
- BE ADR 0004 (multi-tenancy), BE ADR 0007 (data permission) — the two rewriters this decision protects
