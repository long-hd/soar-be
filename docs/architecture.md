# Architecture — Soar Backend

> **Living snapshot + navigation map.** Answers "what exists and where do I read more".
> It does not restate rules (`AGENTS.md`), coding standards (`CONVENTIONS.md`), or rationale (`docs/decisions/adr/`).
>
> Mirrors the role of `../soar-fe/docs/architecture.md` so an agent familiar with one repo navigates the other identically.

**Last updated**: 2026-08-14

---

## Stack

| Layer | Choice |
| ----- | ------ |
| Language / runtime | Java 21 |
| Framework | Spring Boot 3.5 |
| Persistence | Spring Data JPA + Hibernate 6 + PostgreSQL |
| Cache | Redis (serializable) + Caffeine (in-memory live objects) |
| Auth | **Opaque tokens** (UUID in DB/Redis) — not JWT |
| Multi-tenancy | **Active** Hibernate 6 `@TenantId` |
| Data permission | Hibernate `StatementInspector` + JSqlParser |
| Migrations | Flyway (`soar-server/src/main/resources/db/migration/`) |
| API docs | SpringDoc (`/swagger-ui/index.html`, `/v3/api-docs`) |
| Base package | `com.hdl.soar` |

Origin: rebuilt from **RuoYi-Vue-Pro / yudao** (`yudao-boot-mini`). DB table/column names cloned, adapted to PostgreSQL.

Paired frontend: `../soar-fe` — React 19 + TypeScript + Ant Design v6 + Redux Toolkit + TanStack Query.

---

## Module inventory

### Framework starters (`soar-framework/`)

| Module | Responsibility | Module doc |
| ------ | -------------- | ---------- |
| `soar-common` | `BasePO`, `TenantBasePO`, `CommonResult`, `PageResult`, exceptions, utils, `*CommonApi` interfaces | — |
| `soar-spring-boot-starter-web` | CORS, `GlobalExceptionHandler`, API prefix, SpringDoc, apilog framework | — |
| `soar-spring-boot-starter-security` | Opaque-token filter, `SecurityConfig`, `@ss` permission bean, operatelog framework | **[README](../soar-framework/soar-spring-boot-starter-security/README.md)** (452 lines) |
| `soar-spring-boot-starter-redis` | `RedisTemplate`, `CacheManager` | **[README](../soar-framework/soar-spring-boot-starter-redis/README.md)** (549 lines) |
| `soar-spring-boot-starter-jpa` | `SoftDeleteRepository`, auditing, `SpecUtils`, Metamodel | — |
| `soar-spring-boot-starter-biz-tenant` | `@TenantId` resolver, web filters, `@TenantIgnore` AOP, `TenantUtils` | — |
| `soar-spring-boot-starter-biz-data-permission` | `@DataPermission`, rule factory, `DataPermissionStatementInspector` | **[README](../soar-framework/soar-spring-boot-starter-biz-data-permission/README.md)** |
| `soar-spring-boot-starter-job` | Quartz scheduling | — |
| `soar-spring-boot-starter-mq` | Redis-based messaging (pub/sub + stream) | — |
| `soar-spring-boot-starter-websocket` | WebSocket session + message dispatch | — |
| `soar-spring-boot-starter-excel` | `ExcelUtils` | — |

`soar-dependencies/` is a standalone BOM (`<dependencyManagement>` only), imported by the root POM with `scope=import`.

### Business modules

| Module | Architecture | Domains (`controller/admin/`) |
| ------ | ------------ | ----------------------------- |
| `soar-module-system` | Layered | `auth`, `dept` (dept + post), `dict`, `logger`, `oauth2`, `permission` (menu + role + permission), `tenant`, `user` |
| `soar-module-infra` | Layered | `config`, `file`, `job`, `logger` |
| `soar-module-pay` | Layered (yudao port) | `app`, `channel`, `notify`, `order` |
| `soar-server` | — | Spring Boot app; aggregates modules; holds `application.yaml` + `db/migration` |

Cross-module calls go through `*CommonApi` interfaces in `soar-common`, implemented per module in `api/{domain}/`. No Feign in the monolith.

---

## Cross-cutting concerns — where each lives

| Concern | Entry points |
| ------- | ------------ |
| Authentication | `starter-security`: `TokenAuthenticationFilter`, `LoginUser`, `SecurityFrameworkUtils`. Opaque token stored in DB + Redis. |
| Authorization | `@PreAuthorize("@ss.hasPermission('...')")` → `SecurityFrameworkServiceImpl` → `PermissionCommonApi`. Super-admin short-circuits in `PermissionService.hasAnySuperAdmin`. |
| Multi-tenancy | `starter-biz-tenant`: `TenantContextHolder`, `TenantContextWebFilter`, `TenantSecurityWebFilter`, `@TenantIgnore`, `TenantUtils.executeIgnore`. |
| Data permission | `starter-biz-data-permission`: `@DataPermission`, `DataPermissionContextHolder`, `DeptDataPermissionRule`, `DataPermissionStatementInspector`. Table registration in `soar-module-system/.../framework/datapermission/config/SoarDataPermissionConfiguration.java`. |
| Caching | Redis via `starter-redis` for serializable data; Caffeine for non-serializable live objects. Eviction rules in `CONVENTIONS.md`. |
| Logging | apilog framework in `starter-web`; operatelog framework in `starter-security`; persistence in `soar-module-infra` (`ApiAccessLogApiImpl`, `ApiErrorLogApiImpl`, wrapped in `TenantUtils.executeIgnore`). |
| File storage | `soar-module-infra/framework/file/core` storage clients; `FilePO` is global (`BasePO`, not tenant-scoped). |
| Scheduled jobs | `starter-job` (Quartz) + `soar-module-infra` job domain. |
| Error handling | `GlobalExceptionHandler` in `starter-web`; `throw exception(ErrorCodeConstants.XXX)` in services. |

---

## Database state

Flyway migrations `V1_0_1` → `V1_2_0` (20 scripts). Broad phases:

| Range | Content |
| ----- | ------- |
| `V1_0_1` – `V1_0_3` | System tables + seed + dict tables |
| `V1_0_4` – `V1_0_7` | Infra tables, dict seed, operate log, file storage seed |
| `V1_0_8` – `V1_1_4` | Menu evolution: `tab_key`, menu reseed, dict/component renames, file menu restructure |
| `V1_1_5` – `V1_1_7` | Quartz + job + job log |
| `V1_1_8` – `V1_2_0` | Pay: app/channel, order, notify |

DB is **not in production** → migrations are edited **in place**; no ALTER-only scripts yet.

`V1_0_8__add_tab_key_to_system_menu.sql` is the FE contract seam: `system_menu.tab_key` drives `soar-fe`'s flat `?tab=` routing (FE ADR 0001).

---

## Documentation map

```
soar-be/
├── AGENTS.md                        # Agent rules — entry point, read first
├── CONVENTIONS.md                   # Coding standards detail
├── TECH_DEBT.md                     # Debt tracker
├── skills/                          # Agent task recipes
│   ├── add-permission.md
│   ├── audit-dependencies.md
│   └── crud-entity.md
├── docs/
│   ├── architecture.md              # This file — map + inventory
│   ├── api-contract.md              # Wire contract with soar-fe — canonical
│   └── decisions/
│       ├── README.md                # ADR + task index
│       ├── adr/                     # Why a decision was made (Nygard format) — 0001…0008
│       └── tasks/                   # How a block was built
└── soar-framework/*/README.md       # Module-level deep docs (see inventory above)
```

Module-level READMEs stay **next to their code** on purpose — colocation minimises drift. This file is
their index; there is no copy under `docs/`.

---

## Where to find more

| Question | File |
| -------- | ---- |
| Agent operational rules? | `../AGENTS.md` |
| Code style, naming, patterns? | `../CONVENTIONS.md` |
| Why was decision X made? | `decisions/adr/<NNNN>-*.md` — index in `decisions/README.md` |
| How was work item Y built? | `decisions/tasks/<id>-*.md` |
| Tech debt items? | `../TECH_DEBT.md` |
| How security / tokens work? | `../soar-framework/soar-spring-boot-starter-security/README.md` |
| How caching is wired? | `../soar-framework/soar-spring-boot-starter-redis/README.md` |
| How data permission rewrites SQL? | `../soar-framework/soar-spring-boot-starter-biz-data-permission/README.md` |
| Data permission gaps vs yudao? | `decisions/tasks/dp-01-sql-walker-parity.md` |
| API contract shared with FE? | `api-contract.md` |
| yudao reference for a feature? | Compare directly against the `yudao-boot-mini` checkout |
| FE architecture, decisions, phases? | `../../soar-fe/docs/architecture.md` |

---

## Maintenance

Update this file when:

- A module or framework starter is added or removed (inventory tables)
- A new Flyway migration range lands (database state)
- A module-level README is added (documentation map + inventory)
- An ADR is added (no need to list ADRs here — `decisions/README.md` owns that index)
- A cross-cutting concern moves between modules

Keep it a **map**, not a duplicate. If a fact belongs in `CONVENTIONS.md`, an ADR, or a module README,
link to it rather than restating it here.
