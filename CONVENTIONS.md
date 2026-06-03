# CONVENTIONS.md — Soar Backend

> Detailed coding standards. Referenced by AGENTS.md.
> Update this file when new conventions are established.
> **Last synced with code: 2026-06-02 (post-S9).** This file was previously out of date; it now reflects the actual codebase.

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
| Auth token | JWT | **Opaque token** (UUID in DB/Redis) | Immediate revocation |
| Multi-tenancy | manual | **Active** Hibernate 6 `@TenantId` | Real isolation, not a stub |
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

No `api`/`biz` module split (unlike yudao). One module per domain; the `api/` package within a module holds `*CommonApi` impls. `*CommonApi` interfaces live in `soar-common`.

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
- ID references only (`Long deptId`), never JPA object relations.
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
Service impl lives **alongside** the interface in `service/{domain}/` (not in an `impl/` subpackage). Dynamic queries use JPA `Specification` + `SpecUtils.*IfPresent` + Metamodel `*PO_` field constants. Errors: `throw exception(ERROR_CODE_CONSTANT)`.

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
