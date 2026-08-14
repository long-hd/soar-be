# Decisions — Soar Backend

This directory contains two kinds of records:

- **`adr/`** — Architecture Decision Records: single-decision documents in Michael Nygard format
- **`tasks/`** — Work deliverables: documents that drove or audited a specific block of work

## Why both

ADRs answer **why** a decision was made (canonical, concise, ~50–120 lines each).

Task deliverables show **how** work proceeded, including alternatives tried and rejected (~200–500 lines each).

The same decision may appear in both — ADR for quick lookup, task doc for the full narrative including dead ends.

Convention mirrors `../../../soar-fe/docs/decisions/` so the two repos read the same way.

## ADR index

ADRs are numbered sequentially and append-only. Once accepted, a file's number never changes; a new ADR
supersedes an old one instead (and the old one's status becomes "Superseded by #X").

**Numbering is per-repo.** BE `0001` and FE `0001` are different decisions. When citing across repos,
write **"BE ADR 0003"** / **"FE ADR 0001"** — never a bare number.

| #   | Title | Status | Tags |
| --- | ----- | ------ | ---- |
| [0001](adr/0001-opaque-tokens-instead-of-jwt.md) | Opaque tokens instead of JWT | Accepted | auth, security |
| [0002](adr/0002-layered-modules-no-api-biz-split.md) | One Maven module per domain, layered inside — no api/biz split | Accepted | structure |
| [0003](adr/0003-id-references-only-no-jpa-associations.md) | ID references only — no JPA associations | Accepted | persistence |
| [0004](adr/0004-active-multi-tenancy-via-hibernate-tenantid.md) | Active multi-tenancy via Hibernate `@TenantId` | Accepted | tenancy, persistence |
| [0005](adr/0005-dynamic-queries-via-specification-and-specutils.md) | Dynamic queries via `Specification` + `SpecUtils` + Metamodel | Accepted | persistence |
| [0006](adr/0006-redis-and-caffeine-two-tier-caching.md) | Two cache technologies: Redis for data, Caffeine for live objects | Accepted | caching |
| [0007](adr/0007-data-permission-via-hibernate-statementinspector.md) | Data permission via Hibernate `StatementInspector` + JSqlParser | Accepted | authorization, persistence |
| [0008](adr/0008-service-layer-tenant-filter-for-assignments.md) | Tenant filtering for assignment operations belongs in the service layer | Accepted | tenancy, authorization |

**Not yet ADRs** — decisions that live in `CONVENTIONS.md` because they are narrow implementation rules
rather than cross-cutting architecture. Promote one if it starts getting re-litigated: mandatory
`IntEnumConverter` on enum-typed PO fields; MapStruct `disableBuilder` + null-value strategies;
the Spring Data `@Query` projection rule; the no-`DEFAULT` migration rule.

## Task index

| ID | Title | Date | Summary |
| -- | ----- | ---- | ------- |
| [DP-01](tasks/dp-01-sql-walker-parity.md) | Data permission SQL walker: Soar vs MyBatis-Plus (yudao) | 2026-08-14 | Audit of `DataPermissionStatementInspector` against MP 3.5.16. Finds 4 Soar-only walker gaps (WHERE subquery, join `ON` placement, SELECT-list subquery, sub-join), 3 points where Soar is safer, 2 shared limitations. Feeds `DP-TD1`…`DP-TD11` in `TECH_DEBT.md`. |

## Reading paths

- **Understand data permission end to end**:
  `../../soar-framework/soar-spring-boot-starter-biz-data-permission/README.md` → `tasks/dp-01-sql-walker-parity.md` → `../../TECH_DEBT.md` (DP-TD items)

- **Understand auth + permission checks**:
  `../../soar-framework/soar-spring-boot-starter-security/README.md` → `../../AGENTS.md § Permission System`

- **Understand what the repo looks like overall**:
  `../architecture.md` → `../../CONVENTIONS.md`

- **Work on anything the frontend consumes**:
  `../api-contract.md` → the relevant ADR → `../../../soar-fe/AGENTS.md § API Conventions`

## Adding a new ADR

1. Pick the next number (current highest + 1).
2. Create `adr/NNNN-short-kebab-title.md` using the Michael Nygard template:

```markdown
# NNNN. Title

Date: YYYY-MM-DD
Status: Proposed | Accepted | Superseded by #X | Deprecated

## Context

What's the situation, what's the problem.

## Decision

The choice made + rationale.

## Alternatives considered

What else was on the table and why it lost.

## Consequences

Positive, negative, risks, follow-ups.

## References

Code paths, yudao comparison, related ADRs.
```

3. Update the ADR index table above.
4. If the decision changes a fact the frontend depends on, follow the cross-repo checklist in
   `../../AGENTS.md` before closing.

## Adding a task deliverable

When a block ships (or an audit completes), write `tasks/<id>-<short-title>.md` and add a row to the
task index above.

IDs are short area-prefixed slugs (`dp-01`, `sec-02`, …), not phase-scoped — this repo has no phase
folder convention. Keep the prefix consistent with the `TECH_DEBT.md` area prefix for the same subject.
