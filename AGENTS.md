# AGENTS.md — Soar Backend

> Cross-tool standard. Read by Claude Code, Cursor, Codex, and any AI coding agent.
> This file is the **single source of rules** for AI working on this repo.
> For detailed rationale, see docs/ARCHITECTURE_DECISIONS.md.

## Project Overview

Soar is a full-stack admin platform rebuilt from **RuoYi-Vue-Pro** (github.com/YunaiV/ruoyi-vue-pro).
Goal: learning enterprise patterns (RBAC, dynamic menus, data permissions, audit logging) by rebuilding with a modern stack.

- Backend: Spring Boot 3 + Spring Data JPA + PostgreSQL + Redis
- Frontend (separate repo `soar-fe`): React + TypeScript + shadcn/ui + TanStack Query
- Database schema: cloned from RuoYi-Vue-Pro as-is (same table/column names)
- Reference: when stuck, compare with RuoYi source. See docs/RUOYI_REFERENCE_MAP.md

## Build & Run

```bash
# Build
mvn clean package -DskipTests

# Run (requires PostgreSQL + Redis running)
mvn spring-boot:run -pl soar-server

# Test
mvn test

# Database
# PostgreSQL: localhost:5432/soar
# Redis: localhost:6379
```

## Module Structure

```
soar-be/
├── soar-dependencies/           # BOM only (<dependencyManagement>)
├── soar-framework/
│   ├── soar-common/             # BaseEntity, CommonResult, exceptions, utils
│   ├── soar-spring-boot-starter-security/  # JWT, TokenFilter, permission checker
│   ├── soar-spring-boot-starter-redis/     # Redis config, cache utils
│   └── soar-spring-boot-starter-web/       # CORS, GlobalExceptionHandler
├── soar-module-system/
│   ├── soar-module-system-api/  # DTOs, enums shared with other modules
│   └── soar-module-system-biz/  # Controllers, services, repos, entities
├── soar-module-infra/
│   ├── soar-module-infra-api/
│   └── soar-module-infra-biz/
├── soar-module-logistics/       # Phase 5 — DDD architecture
│   ├── soar-module-logistics-api/
│   └── soar-module-logistics-biz/
└── soar-server/                 # Shell: aggregates all modules into 1 JAR
```

## Architecture Rules

### Per-module architecture
- **System module** (user, role, menu, dept, dict): **Layered Architecture**
  - Controller → Service → Repository. No DDD. No domain layer.
  - This matches RuoYi's pattern and is appropriate for pure CRUD.
- **Infra module** (config, file, log): **Layered Architecture** (same as system)
- **Business modules** (logistics): **DDD / Simplified Hexagonal**
  - Has domain layer with aggregates, domain events, value objects.
  - UseCase layer for orchestration. Adapters for web/persistence.

### JPA Entity Rules (CRITICAL)
- Entities use **ID references ONLY**. NEVER use `@ManyToOne`, `@OneToMany`, `@ManyToMany`.
- Entity mirrors database table exactly. One entity = one table.
- This eliminates N+1 problems, lazy loading traps, and cascade surprises.
- When related data is needed, use explicit JPQL JOIN queries or JdbcTemplate.

```java
// ✅ CORRECT — ID reference, Entity suffix
@Entity
@Table(name = "system_users")
public class SystemUserEntity extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private Long deptId;       // ID reference to system_dept
}

// ❌ WRONG — Object reference
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "dept_id")
private SystemDept dept;
```

### Database Rules
- Table names follow RuoYi convention: `system_users`, `system_role`, `system_menu`, etc.
- `tenant_id` column exists in most tables but is unused (default=0). Do NOT filter by it.
- Never modify RuoYi system tables. For extra fields, create 1:1 extension table.
- Migrations managed by Flyway. Scripts in `soar-server/src/main/resources/db/migration/`.

### Permission System
- Menu table (`system_menu`) has 3 types: DIRECTORY(1), MENU(2), BUTTON(3).
- BUTTON records carry `permission` code (e.g., `system:user:create`).
- Permission code format: `{module}:{entity}:{action}`
- Backend checks: `@PreAuthorize("@perm.has('system:user:create')")`
- Data scope on roles: ALL(1), DEPT_AND_CHILDREN(2), DEPT_ONLY(3), SELF_ONLY(4)
- Backend is single source of truth. Frontend only reads permission flags.

## Coding Conventions

See CONVENTIONS.md for full details. Key rules:

- Package: `com.soar.module.{module}.controller/service/repository/entity`
- All controllers return `CommonResult<T>` wrapper
- All controller methods have `@PreAuthorize` annotation
- Exceptions: `throw new ServiceException(ErrorCode.XXX)`
- No magic numbers. Use enums or dict system.
- Service layer: interface + impl in same module
- Comments in **English**. Javadoc on every class and public method. "Why" comments on non-obvious logic.

## Don't

- Don't use `@ManyToOne`, `@OneToMany`, `@ManyToMany` in entities
- Don't put business logic in controllers
- Don't hardcode role checks (`if role == ADMIN`). Use permission codes.
- Don't modify existing RuoYi system tables. Use extension tables.
- Don't create endpoints without `@PreAuthorize`
- Don't use ThreadLocal for user context. Use SecurityContextHolder / CurrentUserProvider.
- Don't use Integer for status fields in new code. Use enums.

## Verification Checklist

After any code change, verify:
- [ ] `mvn compile` passes
- [ ] Every new controller method has `@PreAuthorize`
- [ ] Every new controller method returns `CommonResult<T>`
- [ ] New entities use ID references only (no `@ManyToOne`)
- [ ] New entities extend `BaseEntity`
- [ ] Permission codes follow `{module}:{entity}:{action}` format
- [ ] If new entity: corresponding menu seed data exists (MENU + BUTTON records)
- [ ] Every new class has Javadoc describing its responsibility
- [ ] Every new public method has Javadoc (params, return, throws)
- [ ] No commented-out code left behind

## Deep Context

- Architecture rationale: see `docs/ARCHITECTURE_DECISIONS.md`
- Current phase & tasks: see `docs/PHASE_PLAN.md`
- RuoYi code comparison: see `docs/RUOYI_REFERENCE_MAP.md`
- Full project context for web chat: see `docs/PROJECT_CONTEXT.md`
- Task templates: see `skills/` directory
