# CONVENTIONS.md — Soar Backend

> Detailed coding standards. Referenced by AGENTS.md.
> Update this file when new conventions are established.
> **Last synced with code: 2026-08-14.**
>
> **Scope**: this file owns **what** the convention is and **how** to apply it.
> The **why** behind cross-cutting decisions lives in `docs/decisions/adr/` — sections below link to the
> relevant ADR instead of restating its rationale. Index: [`docs/decisions/README.md`](docs/decisions/README.md).
> Cite cross-repo decisions as "BE ADR 0004" / "FE ADR 0001", never a bare number.

---

## Style Note: RuoYi/yudao vs Soar

yudao/RuoYi-Vue-Pro follows China-enterprise conventions (`*VO` for DTOs, `*DO` for entities, `*Convert` for mappers, `Integer` for status, ThreadLocal for context, China-cloud libs). Soar diverges to align with broader industry practice **and the actual code uses these Soar choices — not the yudao ones**:

| Aspect | yudao style | **Soar style (actual)** | Why |
|--------|------------|------------|-----|
| Request/Response objects | `*VO` | `*ReqDTO` / `*RespDTO` (`*SaveReqDTO`, `*PageReqDTO`, `*RespDTO`, `*SimpleRespDTO`) | DTO is universally understood; VO conflicts with DDD Value Object |
| Entity suffix | `*DO` | **`*PO`** (Persistence Object), e.g. `AdminUserPO`, `FileConfigPO` | Distinguishes from future DDD domain objects |
| Entity package | `dataobject/` | **`dal/entity/{sub}/`** | |
| Repository (DAL) | MyBatis `*Mapper` | **`*Repository`** in **`dal/postgres/{sub}/`** | Signals JPA, frees `*Mapper` for MapStruct |
| Object mapping | `*Convert` (MapStruct) | **`*Mapper`** (MapStruct) with static `INSTANCE`, in **`mapper/{sub}/`** | `*Mapper` is free since DAL uses `*Repository` |
| Status / type fields | `Integer` | **Enum + `IntEnumConverter`** (preferred) or `Integer` with mapping Javadoc | Type-safe |
| User context | `ThreadLocal` | `SecurityContextHolder` (auth); `TenantContextHolder` (tenant) | Spring-managed |
| Auth token | JWT | **Opaque token** (UUID in DB/Redis) | Immediate revocation — [BE ADR 0001](docs/decisions/adr/0001-opaque-tokens-instead-of-jwt.md) |
| Multi-tenancy | manual | **Active** Hibernate 6 `@TenantId` | Real isolation, not a stub — [BE ADR 0004](docs/decisions/adr/0004-active-multi-tenancy-via-hibernate-tenantid.md) |
| Permission SpEL | `@ss.hasPermission(...)` | **`@ss.hasPermission(...)`** (same) | |
| API prefix | `/admin-api`, `/app-api` | **same**, auto-prepended by package (`controller.admin.*` / `controller.app.*`) | |
| Mappers for diff/log | mzt-biz-log, java-object-diff | **JaVers** (when needed); custom `@OperateLog` aspect | China libs unmaintained |

When comparing with yudao source: mentally map `*DO` ↔ `*PO`, `*VO` ↔ `*DTO`, `*Mapper`(MyBatis) ↔ `*Repository`, `*Convert` ↔ `*Mapper`(MapStruct).

**Base package: `com.hdl.soar`** (not `com.soar`).

---

## Package Structure

```
com.hdl.soar.module.{module}/                     # e.g. module.system, module.infra
├── controller/admin/{domain}/                    # admin REST controllers → /admin-api/**
│   └── dto/                                       # *SaveReqDTO, *PageReqDTO, *RespDTO, *SimpleRespDTO
├── controller/app/{domain}/                       # app REST controllers → /app-api/**
├── service/{domain}/                              # {Name}Service + {Name}ServiceImpl (impl alongside, not in impl/)
├── dal/entity/{domain}/                           # JPA entities: *PO (mirror DB tables)
├── dal/postgres/{domain}/                         # Spring Data JPA repositories: *Repository
├── mapper/{domain}/                               # MapStruct mappers: *Mapper (static INSTANCE)
├── api/{domain}/                                  # cross-module *CommonApi implementations
├── framework/                                     # module-specific framework code (e.g. infra framework/file)
└── enums/                                         # ErrorCodeConstants, module enums (+ inner JpaConverter)

com.hdl.soar.framework/                            # shared starters
├── common/                                        # BasePO, TenantBasePO, CommonResult, PageResult/PageParam,
│                                                  #   exceptions, utils, *CommonApi interfaces, enums/converter
├── security/   redis/   web/   jpa/   tenant/   excel/   (+ apilog, operatelog framework code)
```

No `api`/`biz` module split (unlike yudao). One module per domain; the `api/` package within a module holds `*CommonApi` impls. `*CommonApi` interfaces live in `soar-common`. Rationale + which modules are Layered vs (future) DDD: [BE ADR 0002](docs/decisions/adr/0002-layered-modules-no-api-biz-split.md).

---

## Naming Conventions

### Java

| Type | Convention | Example |
|------|-----------|---------|
| JPA Entity | PascalCase + **PO** | `AdminUserPO`, `FileConfigPO`, `MenuPO` |
| Repository | {Name}Repository | `FileConfigRepository` |
| MapStruct mapper | {Name}Mapper (static `INSTANCE`) | `FileConfigMapper.INSTANCE` |
| Service interface / impl | {Name}Service / {Name}ServiceImpl | `FileService` / `FileServiceImpl` |
| Controller | {Name}Controller / App{Name}Controller | `FileController`, `AppFileController` |
| Save request DTO (create+update) | {Name}SaveReqDTO | `FileConfigSaveReqDTO` |
| Page request DTO | {Name}PageReqDTO | `FilePageReqDTO` |
| Response DTO | {Name}RespDTO / {Name}SimpleRespDTO | `FileRespDTO`, `RoleSimpleRespDTO` |
| Cross-module API | {Name}CommonApi / {Name}CommonApiImpl | `FileCommonApi`, `ConfigCommonApi` |
| Enum | PascalCase + Enum | `FileStorageEnum`, `MenuTypeEnum` |
| ErrorCode constants | in `ErrorCodeConstants`, UPPER_SNAKE | `FILE_NOT_EXISTS` |

> Note: many domains use a **single `*SaveReqDTO`** for both create and update (with nullable `id`), not separate Create/Update DTOs.

### Database (snake_case, module prefix)

`system_users`, `system_role`, `infra_file`, `infra_file_config`. FK column = `{referenced}_id` (`dept_id`, `config_id`).

### API Endpoints (REST-ish, yudao-aligned, NOT pure REST)

Soar follows yudao's action-path style, not pure REST resource paths:

| Action | Method | Path |
|--------|--------|------|
| Page | GET | `/{module}/{entity}/page` |
| Get one | GET | `/{module}/{entity}/get?id=` |
| Create | POST | `/{module}/{entity}/create` |
| Update | PUT | `/{module}/{entity}/update` |
| Delete | DELETE | `/{module}/{entity}/delete?id=` |
| Export | GET | `/{module}/{entity}/export-excel` |

Prefix `/admin-api` or `/app-api` is **auto-prepended** based on package (`controller.admin.*` / `controller.app.*`) — do NOT hardcode it in `@RequestMapping`.

### Permission Codes

Format `{module}:{entity}:{action}` — actions: `query` (not `list`), `create`, `update`, `delete`, `export`, `import`. E.g. `infra:file:query`, `infra:file-config:create`.

---

## API Response Format

All endpoints return `CommonResult<T>`: `{ "code": 0, "data": ..., "msg": "" }` on success; non-zero `code` + `msg` on error; paginated data is `PageResult<T>` = `{ "list": [...], "total": N }`.

---

## Entity Pattern (`*PO`)

```java
/**
 * File storage configuration. Global (extends BasePO, not TenantBasePO).
 */
@Entity
@Table(name = "infra_file_config")
@Data
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class FileConfigPO extends BasePO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)   // PostgreSQL bigserial
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    /**
     * Storage type. Maps to {@code FileStorageEnum.storage}: 1=DB, 10=LOCAL, 20=S3.
     */
    @Column(name = "storage", nullable = false)
    private Integer storage;

    @Column(name = "master", nullable = false)
    private Boolean master;
}
```

Key rules:
- Extend **`BasePO`** (global) or **`TenantBasePO`** (has `tenant_id`). `BasePO` provides `creator/createTime/updater/updateTime/deleted` + `@SQLRestriction("deleted = false")`.
- Lombok: `@Data @SuperBuilder(toBuilder=true) @EqualsAndHashCode(callSuper=true) @NoArgsConstructor @AllArgsConstructor`.
- `@GeneratedValue(IDENTITY)` (PostgreSQL `bigserial`).
- ID references only (`Long deptId`), never JPA object relations — [BE ADR 0003](docs/decisions/adr/0003-id-references-only-no-jpa-associations.md).
- Timestamps: `Instant` + `timestamptz`. Audit creator/updater: `Long`. `deleted`: `Boolean`.
- **Enum field** → enum type + an `IntEnumConverter.JpaConverter` (see Enum Converter). **Never** rely on `@Enumerated(ORDINAL)`.
- **`Integer`/`String` field that encodes an enum/dict** → Javadoc must list the value mapping (e.g. `1=DB, 10=LOCAL, 20=S3`).

### Enum Converter (mandatory for enum-typed PO fields)

```java
@Getter
@AllArgsConstructor
public enum FooEnum {
    A(1), B(2);
    private final Integer value;

    @Converter(autoApply = true)
    public static class JpaConverter extends IntEnumConverter<FooEnum> {
        public JpaConverter() { super(FooEnum.class, FooEnum::getValue); }
    }
}
```
Without this, Hibernate maps the enum by ORDINAL and persisted business values (e.g. 3) blow up on read.

---

## Controller Pattern

```java
@Tag(name = "Admin Backend - File Storage")
@RestController
@RequestMapping("/infra/file")          // NO /admin-api prefix — auto-prepended by package
@Validated
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FileController {

    FileService fileService;

    @GetMapping("/page")
    @Operation(summary = "Get file page")
    @PreAuthorize("@ss.hasPermission('infra:file:query')")
    public CommonResult<PageResult<FileRespDTO>> getFilePage(@Valid FilePageReqDTO reqDTO) {
        PageResult<FilePO> page = fileService.getFilePage(reqDTO);
        return success(new PageResult<>(FileMapper.INSTANCE.toDTOList(page.getList()), page.getTotal()));
    }
}
```
Rules: every method `@PreAuthorize("@ss.hasPermission('...')")` (or `@PermitAll` for public), returns `CommonResult<T>`, `@Valid` on request bodies, no business logic (delegate to service), `@RequiredArgsConstructor` + `@FieldDefaults(makeFinal=true)` for injection.

**File upload (springdoc):** `MultipartFile` alone renders as a text field in Swagger. Use `consumes = MediaType.MULTIPART_FORM_DATA_VALUE` + `@RequestPart("file")` + an explicit binary `@Schema(type="string", format="binary")`. (Swagger-form rendering for multipart is finicky in this project's springdoc; backend works regardless.)

---

## Service Pattern

```java
public interface FileService {
    String createFile(String name, String directory, byte[] content);
    PageResult<FilePO> getFilePage(FilePageReqDTO reqDTO);
}

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FileServiceImpl implements FileService {
    FileRepository fileRepository;
    FileConfigService fileConfigService;

    @Override
    public PageResult<FilePO> getFilePage(FilePageReqDTO reqDTO) {
        Specification<FilePO> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            likeIfPresent(predicates, cb, root, FilePO_.name, reqDTO.getName());
            betweenIfPresent(predicates, cb, root, FilePO_.createTime, reqDTO.getCreateTime());
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        Page<FilePO> page = fileRepository.findAll(spec,
                PageUtils.toPageable(reqDTO, Sort.by(Sort.Direction.DESC, FilePO_.ID)));
        return PageUtils.toPageResult(page);
    }
}
```
Service impl lives **alongside** the interface in `service/{domain}/` (not in an `impl/` subpackage). Dynamic queries use JPA `Specification` + `SpecUtils.*IfPresent` (`eqIfPresent`, `likeIfPresent`, `gteIfPresent`, `betweenIfPresent`) + Metamodel `*PO_` field constants — [BE ADR 0005](docs/decisions/adr/0005-dynamic-queries-via-specification-and-specutils.md). Errors: `throw exception(ERROR_CODE_CONSTANT)`.

---

## Error Handling

ErrorCodes are constants in each module's `ErrorCodeConstants` (interface with `ErrorCode` constants), numbered `{module}_{group}_{seq}` (e.g. infra file metadata = `1_001_003_00x`, file config = `1_001_006_00x`). Throw via `ServiceExceptionUtil.exception(CONSTANT)`. `GlobalExceptionHandler` wraps into `CommonResult`.

---

## Migrations (Flyway)

- DB is **not yet in production** → migrations are edited **in place** (rewrite `V1_0_x`) rather than adding ALTER scripts.
- **No `DEFAULT ''` / `DEFAULT '[]'`** and no Java `= ""` — meaningless with Hibernate (it always sends all columns). Optional → nullable, no DEFAULT. Required → `NOT NULL` without DEFAULT; code sets the value.
- Seed/dict INSERTs: **no hardcoded ids** (let the sequence assign). Menus use explicit ids only where `parent_id` references are needed, followed by a `setval(...)` reset.
- Empty optional columns → `NULL`, not `''`.
- Current migrations: `V1_0_1` … `V1_0_8`.

---

## Caching

- Redis for serializable data (tokens, permissions, dict).
- **Caffeine** (in-memory) for non-serializable live objects (e.g. file storage clients holding S3 SDK objects). Built via `CacheUtils.buildAsyncReloadingCaffeine(...)`. Guava cache is being phased out (still in ~13 files; refactor deferred).

Deciding question: serializable data → Redis; live object → Caffeine. See [BE ADR 0006](docs/decisions/adr/0006-redis-and-caffeine-two-tier-caching.md).

---

## Comment Conventions

**English only** (public portfolio). **No Chinese comments** in generated code (the yudao reference is Chinese — strip it when porting).

Comment: class-level Javadoc (responsibility), public-method Javadoc (contract/params/return/throws), "why" comments for non-obvious logic, entity field Javadoc (dict/enum mapping, relationships, `{@link}`). Don't restate code, comment trivial lines, or leave commented-out code.

Entity field examples:
```java
/** Encrypted password. Uses {@link BCryptPasswordEncoder}. */
private String password;

/** Account status. See {@link CommonStatusEnum}. */          // enum-typed: link the enum
private CommonStatusEnum status;

/** Storage type. Maps to FileStorageEnum.storage: 1=DB, 10=LOCAL, 20=S3. */  // Integer-coded: list mapping
private Integer storage;
```

---

## Library Vetting (mandatory)

Before adopting any library referenced by yudao, verify maintenance status (last release, maintainer activity, Spring Boot 3.5 / Java 21 compatibility). yudao uses China-ecosystem libs that may be abandoned.
- **Rejected**: mzt-biz-log (stale 2023), java-object-diff (dead 2018), MinIO OSS (maintenance mode Dec 2025).
- **Approved**: JaVers (object diff, when needed), SeaweedFS (S3-compatible storage for dev/demo), Caffeine (cache), AWS SDK v2 (`s3` — includes `S3Presigner`, no separate artifact).

---

## Working Style (how AI should operate on this project)

1. **Analyze first.** Clarify open points, surface trade-offs, get decisions — **do not write code until explicitly asked.**
2. Deliver implementations as **markdown files** (the user copies into the project), block by block when the change is large.
3. **No Chinese comments.** English Javadoc/comments only.
4. Verify any new dependency's version/maintenance before proposing it; don't assume artifact names or versions — check Maven Central.
5. Follow the actual code (this file) over the yudao reference when they differ.

---

## Git Conventions

- Branch: `feature/{phase}-{description}` (e.g., `feature/phase4-file-storage`)
- Commit: Conventional Commits (`feat:`, `fix:`, `refactor:`, `docs:`)

## MapStruct DTO → PO Mapping (BE)

### The Lombok @Builder gotcha

When mapping `DTO → PO` where PO uses Lombok `@Builder` with field default values (e.g., `private Boolean keepAlive = Boolean.TRUE`), MapStruct's default generated code uses the builder pattern which OVERWRITES Lombok field defaults with null source values, causing `nullable=false` constraint violations.

**Symptoms**:
- BE returns 500 on create when FE strips optional boolean fields
- `keep_alive cannot be null` or similar PSQL constraint errors
- Field appears null in PO despite Java default

**Root cause**: 
- MapStruct generates `MenuPO.builder().keepAlive(dto.getKeepAlive()).build()` — builder methods called unconditionally
- `@BeanMapping(nullValuePropertyMappingStrategy = IGNORE)` doesn't work with builder pattern
- Lombok field defaults run in constructor — bypassed by builder

**Fix**: disable builder + explicit null check strategy:

```java
import org.mapstruct.Builder;
import org.mapstruct.BeanMapping;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    uses = { EnumMapper.class, SystemEnumMapper.class },
    builder = @Builder(disableBuilder = true)   // force setter-based
)
public interface MenuMapper {
    
    @BeanMapping(
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
    )
    MenuPO toPO(MenuSaveReqDTO dto);
    
    @BeanMapping(
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
    )
    void updatePO(MenuSaveReqDTO dto, @MappingTarget MenuPO po);
}
```

**Required PO annotations**: `@Data @Builder @NoArgsConstructor @AllArgsConstructor` (all 4 needed for both Lombok builder + MapStruct setter pattern).

**Why both `NullValueCheckStrategy.ALWAYS` and `NullValuePropertyMappingStrategy.IGNORE`?**
- `ALWAYS`: check source for null before assignment (covers `toPO` create case where target is fresh + has Java defaults)
- `IGNORE`: preserve target value when source null (covers `updatePO` case where target has existing DB values)

Both together = "missing field = preserve target/default". Semantic match for PATCH-style operations.

**Verify after change**: read `target/generated-sources/annotations/.../MenuMapperImpl.java` — should contain `if (dto.getField() != null)` guards before setters.

**When to extract `SoarMapperConfig`**: When 2nd mapper hits same issue (Rule of Two). Then centralize:

```java
@MapperConfig(
    nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    builder = @Builder(disableBuilder = true)
)
public interface SoarMapperConfig {}
```

Then `@Mapper(config = SoarMapperConfig.class)` on each mapper.

Location: `soar-framework/soar-spring-boot-starter-jpa/src/main/java/com/hdl/soar/framework/jpa/mapping/SoarMapperConfig.java`.

## Spring Data `@Query` projection rule

**Rule**: When a Spring Data JPA repository method's return type does NOT match the entity type, you MUST add an explicit `@Query` annotation with projection. Spring Data derived queries (`findAllBy...`, `findBy...`) return the entity type by default; type-erasure casting to a primitive collection (`Set<Long>`, `List<String>`, etc.) at runtime causes silent failures (Hibernate IN-clause binding errors, ClassCastException on iteration, TypeMismatchException).

### Anti-pattern

```java
// ❌ Looks fine, fails at runtime when result is non-empty
Set<Long> findAllByRoleIdIn(Collection<Long> roleIds);
```

### Pattern

```java
// ✅ Explicit projection
@Query("SELECT rm.menuId FROM RoleMenuPO rm WHERE rm.roleId IN :roleIds")
Set<Long> findAllByRoleIdIn(@Param("roleIds") Collection<Long> roleIds);
```

OR rename + return entity:

```java
// ✅ Conventional naming, caller projects
List<RoleMenuPO> findAllByRoleIdIn(Collection<Long> roleIds);
// caller: convertSet(result, RoleMenuPO::getMenuId)
```

### Audit checklist when adding a repository method

1. Does method follow `findAll<X>By...` / `findBy...` derived-query naming?
2. Is the declared return type `<Entity>`, `Collection<Entity>`, `Optional<Entity>`, `Stream<Entity>`?
3. If NO to #2 → must have explicit `@Query` annotation. If missing → BUG.
---

## Service-layer tenant filter

**Rule**: For multi-tenant safety on operations that grant cross-cutting access (role-menu, role-dept, user-role, etc.), the tenant filter belongs in the **service layer**, not the controller. Filtering is immutable — the service produces a new filtered `Set` instead of mutating the caller's collection.

Rationale and the rejected yudao alternative: [BE ADR 0008](docs/decisions/adr/0008-service-layer-tenant-filter-for-assignments.md).

### Pattern

```java
@Override
@Transactional(rollbackFor = Exception.class)
@Caching(evict = { ... })
public void assignFoo(Long entityId, Set<Long> targetIds) {
    Set<Long> safe = CollUtil.emptyIfNull(targetIds);
 
    // Tenant safety
    Set<Long> tenantAllowed = tenantService.getTenantFooIds();   // null = no filter
    if (tenantAllowed != null) {
        safe = safe.stream()
                .filter(tenantAllowed::contains)
                .collect(Collectors.toSet());
    }
 
    // ... diff-based assign logic
}
```

`tenantService.get<Resource>Ids()` returns `null` to mean "no filter applies" (system tenant, or tenancy disabled) — distinct from an empty set, which would mean "nothing allowed". Always handle this branch.

### Anti-pattern (yudao-style controller filter)

```java
// ❌ Controller mutates DTO + bypassable by internal callers
public CommonResult<Boolean> assignFoo(@RequestBody Req req) {
    tenantService.handleTenantFoo(allowed ->
        req.getIds().removeIf(id -> !allowed.contains(id))   // mutation!
    );
    service.assign(req.getId(), req.getIds());
    return success(true);
}
```

Soar deliberately diverges from yudao here — keep the filter in the service.
 
---

## Missing `@CacheEvict` audit checklist

**Bug class**: a mutation modifies data backing a `@Cacheable` read method but does not `@CacheEvict` the corresponding cache → stale reads.

### Convention going forward

Every `@Cacheable` method should have a comment listing mutations that should evict it. Example:

```java
/**
 * Caches: ROLE/{id}
 * Mutations that must @CacheEvict: createRole, updateRole, deleteRole,
 *   deleteRoleList, updateRoleStatus, updateRoleDataScope.
 */
@Cacheable(value = RedisKeyConstants.ROLE, key = "#id", unless = "#result == null")
public RolePO getRoleFromCache(Long id) { ... }
```

This makes the dependency explicit in code — anyone adding a mutation sees the comment and remembers to add `@CacheEvict`.

### Audit procedure (run before any release)

```bash
# Find all @Cacheable annotations
grep -rn "@Cacheable" soar-module-system/src/main/java
grep -rn "@Cacheable" soar-module-infra/src/main/java
```

For each `@Cacheable` cache name (e.g. `ROLE`, `MENU_ROLE_ID_LIST`):
1. Identify ALL mutations changing the underlying data (insert/update/delete on the backing entity or relationship)
2. Verify each mutation has a `@CacheEvict` for that cache name
3. If missing → fix, or record it in `TECH_DEBT.md`
### Common patterns

- Single-row evict: `@CacheEvict(value = X, key = "#id")`
- Cross-cutting evict (bulk + join tables): `@CacheEvict(value = X, allEntries = true)`
- Multi-cache evict requires `@Caching`:
```java
@Caching(evict = {
    @CacheEvict(value = CACHE_A, key = "#id"),
    @CacheEvict(value = CACHE_B, allEntries = true)
})
```