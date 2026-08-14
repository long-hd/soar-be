# 0007. Data permission via Hibernate `StatementInspector` + JSqlParser

Date: 2026-08-14
Status: Accepted

## Context

Role-based **data scope** (a manager sees their department's rows, a sales rep sees only their own)
requires a row-level predicate on every query touching a scoped table. yudao implements this with
MyBatis-Plus's `DataPermissionInterceptor`, which hooks the MyBatis executor and rewrites SQL through
JSqlParser.

Soar has no MyBatis. Hibernate provides `@TenantId` for the tenant discriminator (BE ADR 0004) but
nothing equivalent for a predicate whose shape depends on the caller's roles at request time.

## Decision

Rewrite SQL at Hibernate's `StatementInspector` hook, using JSqlParser — the same parser MyBatis-Plus
uses, at a different hook point.

`DataPermissionStatementInspector` receives every SQL string Hibernate is about to execute, parses it,
walks the statement (SELECT / UPDATE / DELETE), and for each table registered with a rule injects the
rule's expression into the `WHERE` clause.

Supporting pieces (all a 1:1 port of yudao's **rule layer**):

- `@DataPermission(enable = false | includeRules | excludeRules)` — method/class-level opt-out, held in
  `DataPermissionContextHolder`
- `DataPermissionUtils.executeIgnore(...)` — block-level opt-out
- `DeptDataPermissionRule` + `DeptDataPermissionRuleCustomizer` — modules register `(table, dept column)`
  and `(table, user column)` pairs
- Scope resolution via `PermissionService.getDeptDataPermission(userId)` → `DeptDataPermissionRespDTO`

Only the SQL-rewriting layer was rewritten; the rule, annotation, and scope-resolution layers match
yudao.

## Alternatives considered

**Hibernate `@Filter` / `@FilterDef`.** Rejected: filters must be enabled per `Session` with parameters
known up front, they do not apply to `UPDATE`/`DELETE`, and expressing "dept in (…) OR user = ?" with a
scope set that varies per request does not fit the parameterisation model.

**Manual predicates in every `Specification`.** Rejected for the same reason as manual tenant predicates
in BE ADR 0004 — one omission is a silent data leak, unenforceable at review time.

**Port MyBatis-Plus's interceptor.** Not applicable; the hook does not exist without MyBatis.

**Spring Security ACL.** Rejected: per-row ACL tables for a scope that is derivable from the department
tree is disproportionate.

## Consequences

**Positive**

- Covers **every** query Hibernate emits, including derived queries, `Specification`s, JPQL and native
  queries — anything that reaches the JDBC layer. MyBatis-Plus's hook sees only mapper statements.
- Fails closed: a user with no scope gets `1 = 0` rather than an unfiltered result.
- Opt-out is explicit and greppable.

**Negative**

- Soar owns a SQL walker. Every AST shape it does not handle is a silent fail-open.
- Working at the SQL-string level means the inspector sees Hibernate's generated aliases, not entity
  names — debugging is a step removed from the Java code.
- Parsing cost on every statement (mitigated by the walker exiting early when no registered table appears).

**Risks — known walker gaps**

Audited against MyBatis-Plus 3.5.16 in `docs/decisions/tasks/dp-01-sql-walker-parity.md`. Four gaps where
MP handles a case Soar does not: subquery in `WHERE`, join predicate placed in `WHERE` instead of `ON`
(turns `LEFT JOIN` into an inner join), subquery in the `SELECT` list, and nested sub-joins. All are
latent today because only `system_users` is registered — they become live the moment a second table is
registered. Tracked as `DP-TD1`…`DP-TD11` in `TECH_DEBT.md`.

## References

- `soar-framework/soar-spring-boot-starter-biz-data-permission/README.md` — usage, flow, gotchas
- `docs/decisions/tasks/dp-01-sql-walker-parity.md` — full Soar vs MyBatis-Plus parity audit
- `TECH_DEBT.md` — `DP-TD1`…`DP-TD11`
- BE ADR 0004 — the tenant discriminator, which uses Hibernate's native mechanism instead
