# AGENTS.md — Soar Backend

> Cross-tool standard. Read by Claude Code, Cursor, Codex, and any AI coding agent.
> This file is the **single source of rules** for AI working on this repo.
> For detailed rationale, see docs/ARCHITECTURE_DECISIONS.md and CONVENTIONS.md.
> **Last synced with code: 2026-08-14.** Earlier versions of this file described conventions that do not match the code; this version reflects the actual codebase.

## Project Overview

Soar is a full-stack admin platform rebuilt from **RuoYi-Vue-Pro / yudao** (gitee.com/yudaocode/yudao-boot-mini). Goal: learning enterprise patterns (RBAC, dynamic menus, multi-tenancy, data permissions, audit logging, file storage) by rebuilding with a modern stack.

- Backend: Java 21 + Spring Boot 3.5 + Spring Data JPA + Hibernate 6 + PostgreSQL + Redis
- Auth: **opaque tokens** (UUID in DB/Redis), not JWT
- Multi-tenancy: **active** Hibernate 6 `@TenantId` (not a stub)
- Frontend (separate repo `soar-fe`, Phase 5): React 19 + TypeScript + **Ant Design v6** + Redux Toolkit + TanStack Query. Note: shadcn/ui was **rejected** by FE — do not assume it. See `../soar-fe/AGENTS.md`.
- DB schema: cloned from yudao (same table/column names), adapted to PostgreSQL
- Reference: when stuck, compare with yudao source. See docs/RUOYI_REFERENCE_MAP.md
- **Base package: `com.hdl.soar`**

## Build & Run

```bash
# Build the BOM first if it changed (flatten plugin; run from the module dir, non-recursive)
cd soar-dependencies && ../mvnw install -N && cd ..

# Compile a module + its deps
./mvnw -pl soar-module-infra -am compile

# Run (requires PostgreSQL + Redis)
./mvnw -pl soar-server spring-boot:run

# PostgreSQL: localhost:5432/soar   Redis: localhost:6379
# Swagger: /swagger-ui/index.html   OpenAPI JSON: /v3/api-docs
```

## Module Structure (single module per domain — NO api/biz split)

```
soar-be/
├── soar-dependencies/           # BOM only (<dependencyManagement>); standalone, imported by root via scope=import
├── soar-framework/
│   ├── soar-common/             # BasePO, TenantBasePO, CommonResult, PageResult, exceptions, utils, *CommonApi
│   ├── soar-spring-boot-starter-web/       # CORS, GlobalExceptionHandler, API prefix, SpringDoc, apilog framework
│   ├── soar-spring-boot-starter-security/  # opaque-token filter, SecurityConfig, operatelog framework  [README]
│   ├── soar-spring-boot-starter-redis/     # RedisTemplate, CacheManager                                [README]
│   ├── soar-spring-boot-starter-jpa/       # SoftDeleteRepository, BasePO, auditing, SpecUtils, Metamodel
│   ├── soar-spring-boot-starter-biz-tenant/         # @TenantId resolver, web filters, AOP
│   ├── soar-spring-boot-starter-biz-data-permission/# @DataPermission, rule factory, Hibernate StatementInspector  [README]
│   ├── soar-spring-boot-starter-job/       # Quartz scheduling
│   ├── soar-spring-boot-starter-mq/        # Redis-based message queue (pub/sub + stream)
│   ├── soar-spring-boot-starter-websocket/ # WebSocket session + message dispatch
│   └── soar-spring-boot-starter-excel/     # ExcelUtils
├── soar-module-system/          # system domain (Layered): user, role, menu, dept, post, dict, auth, oauth2
├── soar-module-infra/           # infra domain (Layered): config, logging (access/error/operate), file storage
├── soar-module-pay/             # pay domain (Layered): order, refund, channel, app, notify
└── soar-server/                 # Spring Boot app; aggregates modules; holds application.yaml + db/migration
```

Starters marked `[README]` carry a module-level README next to the code — read it before touching that
starter. Indexed in `docs/architecture.md`.

Cross-module calls go through `*CommonApi` interfaces in `soar-common`, implemented in each module's `api/{domain}/` package (`@Service`). No Feign in the monolith.

## Architecture Rules

### Per-module architecture
- **System module** (user, role, menu, dept, post, dict, auth): **Layered** — Controller → Service → Repository. No DDD.
- **Infra module** (config, file, logs): **Layered** (same). Infra also has `framework/` for cross-cutting infra code (e.g. `framework/file/core` storage clients).
- **Pay module**: **Layered** (same as system/infra). It is a yudao port, so it kept yudao's shape.
- **Greenfield business modules** (e.g. future logistics): DDD / simplified hexagonal — domain aggregates, use cases, adapters. This is an intent, not yet demonstrated by any module in the repo.

### JPA Entity Rules (CRITICAL)
- Suffix **`*PO`** (Persistence Object), package `dal/entity/{domain}/`.
- Extend **`BasePO`** (global) or **`TenantBasePO`** (table has `tenant_id`).
- **ID references ONLY** — never `@ManyToOne`/`@OneToMany`/`@ManyToMany`. One entity = one table.
- `@Id @GeneratedValue(IDENTITY)` (PostgreSQL bigserial). Timestamps `Instant`+`timestamptz`; audit `Long`; `deleted` `Boolean`.
- **Enum field** → enum type + inner `IntEnumConverter.JpaConverter` (`@Converter(autoApply=true)`). NEVER `@Enumerated(ORDINAL)`. An `Integer`/`String` column encoding an enum/dict gets a Javadoc listing the mapping.

```java
@Entity @Table(name = "infra_file")
@Data @SuperBuilder(toBuilder = true) @EqualsAndHashCode(callSuper = true)
@NoArgsConstructor @AllArgsConstructor
public class FilePO extends BasePO {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "config_id") private Long configId;   // ID reference, no @ManyToOne
}
```

### Multi-Tenancy (active)
- Tables with `tenant_id` → extend `TenantBasePO`; Hibernate `@TenantId` auto-filters/populates. Global tables (menu, oauth2 client, config, file*) extend `BasePO`.
- Tenant resolved per request via `TenantContextHolder` + web filters. Do NOT disable or ignore tenant filtering.

### Database Rules
- yudao table/column names, adapted to PostgreSQL. Flyway scripts in `soar-server/src/main/resources/db/migration/` (`V1_0_1` … current).
- DB **not in production** → edit migrations **in place** (no ALTER scripts yet).
- **No `DEFAULT ''`/`DEFAULT '[]'`**, no Java `= ""`. Optional → nullable. Required → `NOT NULL` without DEFAULT (code sets it). Seed/dict INSERTs: no hardcoded ids; menus use explicit ids only for `parent_id` refs + `setval`. Empty optional columns → `NULL`, not `''`.

### Permission System
- `system_menu.type`: DIR(1), MENU(2), BUTTON(3) — mapped via `MenuTypeEnum` **with `IntEnumConverter`** (values 1/2/3, not ordinal).
- BUTTON rows carry a `permission` code; format `{module}:{entity}:{action}` (actions: `query`, `create`, `update`, `delete`, `export`, `import`).
- Check with `@PreAuthorize("@ss.hasPermission('infra:file:query')")`.
- Super-admin (role `super_admin`) is granted everything in code (`PermissionService.hasAnySuperAdmin`) — no `role_menu` rows needed for it.
- **Public endpoints need BOTH `@PermitAll` and `@TenantIgnore`.** `@PermitAll` clears Spring Security; `@TenantIgnore` clears `TenantSecurityWebFilter` (otherwise the request fails with 400 "Missing tenant-id request header" because a browser/anonymous caller sends no `tenant-id`). `@TenantIgnore` on a controller method also auto-registers its URL in the tenant ignore list at startup and sets `TenantContextHolder.setIgnore(true)` so Hibernate skips tenant filtering — correct for global (`BasePO`) data like the file download endpoint.
- Data scope on roles (`DataScopeEnum`): ALL(1), DEPT_CUSTOM(2), DEPT_ONLY(3), DEPT_AND_CHILD(4), SELF(5).
- Data permission is **implemented and on by default** — `DataPermissionStatementInspector` (Hibernate `StatementInspector` + JSqlParser) injects a `WHERE` clause for tables registered via `DeptDataPermissionRuleCustomizer`. Only `system_users` is registered today. Opt out with `@DataPermission(enable = false)` or `DataPermissionUtils.executeIgnore(...)`. Known walker gaps: `docs/decisions/tasks/dp-01-sql-walker-parity.md`.

## Coding Conventions (see CONVENTIONS.md for full detail)

- Package: `com.hdl.soar.module.{module}.{controller.admin|controller.app|service|dal.entity|dal.postgres|mapper|api}.{domain}`
- DTOs: `*SaveReqDTO` (create+update, nullable id), `*PageReqDTO`, `*RespDTO`, `*SimpleRespDTO` in `controller/.../dto/`
- Mapping: MapStruct `*Mapper` with static `INSTANCE`, package `mapper/`
- Repositories: `*Repository` in `dal/postgres/` (no derived `deleteBy*` — bypasses soft delete; load then `delete(po)`)
- Controllers return `CommonResult<T>`; `@RequestMapping` WITHOUT `/admin-api`|`/app-api` (auto-prepended by package)
- Service impl alongside interface in `service/{domain}/` (not `impl/`)
- Errors: `throw exception(ErrorCodeConstants.XXX)`
- Comments **English only, no Chinese**. Javadoc on classes + public methods + "why" comments.
- Dynamic queries: JPA `Specification` + `SpecUtils.*IfPresent` + Metamodel `*PO_` constants.
- Cache: Redis (serializable data); **Caffeine** (in-memory, non-serializable live objects).

## Working Style (IMPORTANT)

- **Analyze first, code only when explicitly asked.** Clarify open points and trade-offs, get decisions, then produce output — typically as **markdown files** the user copies in (block by block for large changes).
- **Verify any new dependency** (version exists on Maven Central, maintenance status, Spring Boot 3.5 / Java 21 compat) before proposing it. Don't trust remembered version numbers.
- Follow this repo's actual code over the yudao reference when they differ.

## Don't

- Don't use `@ManyToOne`/`@OneToMany`/`@ManyToMany`.
- Don't put business logic in controllers.
- Don't hardcode role checks (`if role == ADMIN`); use permission codes.
- Don't create endpoints without `@PreAuthorize` (or `@PermitAll` for intentionally public ones).
- Don't rely on `@Enumerated(ORDINAL)` for enum fields — add the `IntEnumConverter`.
- Don't hardcode the `/admin-api`/`/app-api` prefix in `@RequestMapping`.
- Don't write Chinese comments.
- Don't add a library without checking its maintenance status.
- Don't write code before it's been asked for.

## Verification Checklist

- [ ] `./mvnw -pl <module> -am compile` passes
- [ ] Every new controller method has `@PreAuthorize` (or `@PermitAll`) and returns `CommonResult<T>`
- [ ] `@RequestMapping` has no `/admin-api`|`/app-api` prefix
- [ ] New entities extend `BasePO`/`TenantBasePO`, use ID references only, `*PO` suffix
- [ ] Enum-typed PO fields have an `IntEnumConverter`; Integer/String enum-coded fields have a mapping Javadoc
- [ ] Permission codes follow `{module}:{entity}:{action}`
- [ ] New entity → menu/permission seed exists (MENU + BUTTON), dict seed if needed (no hardcoded dict ids)
- [ ] Migrations follow the no-DEFAULT / nullable-vs-NOT-NULL rules
- [ ] English Javadoc on new classes + public methods; no commented-out code; no Chinese

## Deep Context

### Documents

| Question | File |
| -------- | ---- |
| What exists in this repo and where? | `docs/architecture.md` — inventory + navigation map |
| Why was a decision made? | `docs/decisions/README.md` → `docs/decisions/adr/<NNNN>-*.md` |
| How was a specific block built or audited? | `docs/decisions/tasks/<id>-*.md` |
| What does the frontend depend on? | `docs/api-contract.md` — **canonical** for the wire contract |
| Coding standards, naming, patterns? | `CONVENTIONS.md` |
| Known shortcuts and gaps? | `TECH_DEBT.md` |
| How does starter X work? | `soar-framework/<starter>/README.md` (indexed in `docs/architecture.md`) |
| Task templates? | `skills/` |
| What does yudao do here? | Compare directly against the `yudao-boot-mini` checkout |

> Earlier revisions of this file referenced `docs/ARCHITECTURE_DECISIONS.md`, `docs/PHASE_PLAN.md`,
> `docs/RUOYI_REFERENCE_MAP.md` and `docs/PROJECT_CONTEXT.md`. Those documents never existed in this
> repo. The table above replaces them.

### Documentation protocol

Which artifact to write, and when:

| Situation | Artifact |
| --------- | -------- |
| A cross-cutting architectural choice with real alternatives | **ADR** in `docs/decisions/adr/`. Next number, append-only. Never renumber; supersede instead. |
| A narrow implementation rule (naming, an annotation combination, a bug-class checklist) | A section in `CONVENTIONS.md`. Do not create an ADR. |
| A block of work shipped, or an audit completed | **Task deliverable** in `docs/decisions/tasks/<id>-*.md`, plus a row in the task index. |
| A known shortcut or gap being accepted for now | A row in `TECH_DEBT.md` with an area-prefixed ID. |
| Anything the frontend consumes changes | Update `docs/api-contract.md` **in the same change**, including its "Last verified" date. |
| A module's internals need explaining at length | A `README.md` next to the code, indexed from `docs/architecture.md`. |

Ownership boundaries: `AGENTS.md` holds rules an agent must follow; `CONVENTIONS.md` holds what a
convention is and how to apply it; `docs/decisions/adr/` holds why. Do not restate one in another —
link instead.

### Cross-repo coordination with `soar-fe`

- ADR numbering is **per-repo**. Cite as **"BE ADR 0004"** / **"FE ADR 0001"**, never a bare number.
- `docs/api-contract.md` is the single source of truth for the wire contract. When the FE documentation
  and this repo disagree, this repo wins and the FE doc gets corrected.
- Before merging a breaking contract change, either land the matching FE change or record the gap in
  `soar-fe/TECH_DEBT.md`. See `docs/api-contract.md § 12`.
- FE architecture and decisions: `../soar-fe/docs/architecture.md`, `../soar-fe/docs/decisions/`
