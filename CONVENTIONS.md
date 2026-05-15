# CONVENTIONS.md — Soar Backend

> Detailed coding standards. Referenced by AGENTS.md.
> Update this file when new conventions are established.

---

## Style Note: RuoYi vs Soar

RuoYi-Vue-Pro follows China enterprise conventions (VO suffix for DTOs, DO suffix for entities,
Integer for status fields, ThreadLocal for context). Soar diverges in several places to align
with broader industry practices:

| Aspect | RuoYi style | Soar style | Why |
|--------|------------|------------|-----|
| Request/Response objects | `*ReqDTO` / `*RespDTO` | `*ReqDTO` / `*RespDTO` | DTO is universally understood; VO conflicts with DDD Value Object |
| Entity suffix | `*DO` (Data Object) | `*Entity` (`SystemUserEntity`) | Clearer intent, distinguishes from DDD domain model in business modules |
| Status fields | `Integer` | `Integer` (system), `Enum` (new code) | Enums are type-safe; keep Integer for RuoYi-cloned tables |
| User context | `ThreadLocal` | `SecurityContextHolder` | Managed by Spring, async-safe |
| Package for DTOs | `controller/vo/` | `controller/dto/` | Matches DTO naming |

When comparing with RuoYi source code, mentally map: `*DO` ↔ `*Entity`, `*VO` ↔ `*DTO`.

---

## Package Structure

```
com.soar.module.{module}/
├── controller/           # REST controllers
│   └── dto/              # Request/Response DTOs
│       ├── {Entity}CreateReqDTO.java
│       ├── {Entity}UpdateReqDTO.java
│       ├── {Entity}RespDTO.java
│       └── {Entity}PageReqDTO.java
├── service/              # Service interfaces
│   └── impl/             # Service implementations
├── repository/           # Spring Data JPA repositories
├── entity/               # JPA entities (mirror DB tables exactly)
└── convert/              # MapStruct or manual DTO ↔ Entity converters

com.soar.framework/
├── common/               # Shared base classes
│   ├── pojo/             # CommonResult, PageParam, PageResult
│   ├── exception/        # ServiceException, ErrorCode
│   └── entity/           # BaseEntity
├── security/             # JWT, TokenFilter, PermissionChecker
├── redis/                # Redis config, cache utils
└── web/                  # CORS, GlobalExceptionHandler
```

## Naming Conventions

### Java

| Type | Convention | Example |
|------|-----------|---------|
| JPA Entity | PascalCase + Entity suffix | `SystemUserEntity`, `SystemRoleEntity`, `SystemMenuEntity` |
| Repository | {Name}Repository | `SystemUserRepository` |
| Service interface | {Name}Service | `SystemUserService` |
| Service impl | {Name}ServiceImpl | `SystemUserServiceImpl` |
| Controller | {Name}Controller | `SystemUserController` |
| Create request DTO | {Name}CreateReqDTO | `SystemUserCreateReqDTO` |
| Update request DTO | {Name}UpdateReqDTO | `SystemUserUpdateReqDTO` |
| Response DTO | {Name}RespDTO | `SystemUserRespDTO` |
| Page request DTO | {Name}PageReqDTO | `SystemUserPageReqDTO` |
| Enum | PascalCase | `UserStatusEnum`, `MenuTypeEnum` |
| ErrorCode | UPPER_SNAKE_CASE | `USER_NOT_FOUND`, `ROLE_NAME_DUPLICATE` |

### Database (follows RuoYi)

| Type | Convention | Example |
|------|-----------|---------|
| Table | snake_case with module prefix | `system_users`, `system_role` |
| Column | snake_case | `dept_id`, `created_at` |
| FK column | {referenced_table}_id | `dept_id`, `role_id` |

### API Endpoints

| Action | Method | Path | Example |
|--------|--------|------|---------|
| List (paginated) | GET | /api/{module}/{entity-plural} | `GET /api/system/users?pageNo=1&pageSize=20` |
| Get by ID | GET | /api/{module}/{entity-plural}/{id} | `GET /api/system/users/42` |
| Create | POST | /api/{module}/{entity-plural} | `POST /api/system/users` |
| Update | PUT | /api/{module}/{entity-plural}/{id} | `PUT /api/system/users/42` |
| Delete | DELETE | /api/{module}/{entity-plural}/{id} | `DELETE /api/system/users/42` |
| Simple list (no page) | GET | /api/{module}/{entity-plural}/simple-list | `GET /api/system/roles/simple-list` |

### Permission Codes

Format: `{module}:{entity}:{action}`

| Action | Code | Example |
|--------|------|---------|
| View list | {m}:{e}:list | `system:user:list` |
| Create | {m}:{e}:create | `system:user:create` |
| Update | {m}:{e}:update | `system:user:update` |
| Delete | {m}:{e}:delete | `system:user:delete` |
| Export | {m}:{e}:export | `system:user:export` |

---

## API Response Format

All endpoints return `CommonResult<T>`:

```java
// Success
{
  "code": 0,
  "data": { ... },
  "msg": "success"
}

// Error
{
  "code": 1001,
  "data": null,
  "msg": "User not found"
}

// Paginated list
{
  "code": 0,
  "data": {
    "list": [ ... ],
    "total": 42
  },
  "msg": "success"
}
```

---

## Comment Conventions

Language: **English** for all comments (public GitHub portfolio, international readability).

### What to comment

**Class-level Javadoc** — Every class gets a Javadoc describing its responsibility:
```java
/**
 * Service for managing system users.
 * Handles CRUD operations, password management, and role assignment.
 */
@Service
public class SystemUserServiceImpl implements SystemUserService {
```

**Public method Javadoc** — Every public method describes contract, params, return, and throws:
```java
/**
 * Creates a new user with the given details.
 * Validates username uniqueness and encodes password before saving.
 *
 * @param req the user creation request
 * @return the ID of the created user
 * @throws ServiceException if username already exists (USER_USERNAME_DUPLICATE)
 */
public Long create(SystemUserCreateReqDTO req) {
```

**"Why" comments** — Explain non-obvious business logic or technical decisions:
```java
// Data scope filter: SELF_ONLY means user can only see records they created.
// This is controlled by role.data_scope, not by checking role name.
if (dataScopeType == DataScopeEnum.SELF_ONLY) {
    query.eq("created_by", currentUserId);
}

// We invalidate the entire user's permission cache rather than surgically updating it,
// because role-menu assignments can cascade in complex ways.
redisCache.delete("perm:user:" + userId);
```

**TODO / FIXME** — Mark technical debt and known issues:
```java
// TODO: Add pagination support for large department trees
// FIXME: Race condition when two admins update the same role simultaneously
```

**Field Javadoc on entities** — Use `/** */` doc comments for all fields. Explain dict references, relationships, and constraints. IDE will display on hover, and can use `{@link}` references:
```java
/**
 * Encrypted password.
 * Uses {@link BCryptPasswordEncoder}, no manual salt handling needed.
 */
private String password;

/** Gender. Dict type: system_user_sex (0=unknown, 1=male, 2=female) */
private Integer sex;

/** Account status. 0=disabled, 1=enabled */
private Integer status;

/** Department ID. References {@link SystemDeptEntity#id} */
private Long deptId;

/** Multi-tenant ID. Unused in Soar (always 0, kept for RuoYi schema compatibility) */
private Long tenantId;
```

### What NOT to comment

```java
// ❌ Restating the code
// Get user by ID
public SystemUserRespDTO get(Long id) { ... }

// ❌ Commenting trivial code
count++; // increment counter

// ❌ Commenting getters/setters (Lombok generates them anyway)

// ❌ Commented-out code — delete it, git has history
// userRepository.findByEmail(email);
```

### AI workflow for comments
- **When coding**: AI adds class Javadoc + public method Javadoc + "why" comments automatically.
- **When reviewing**: AI suggests missing Javadoc on public methods, missing "why" on complex logic, and flags commented-out code for deletion.

---

## Entity Pattern

```java
/**
 * System user entity.
 * Represents an admin account that can log in and be assigned roles.
 */
@Entity
@Table(name = "system_users")
@Getter
@Setter
public class SystemUserEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Login username. Unique across the system. */
    @Column(nullable = false, unique = true, length = 50)
    private String username;

    /**
     * Encrypted password.
     * Uses {@link BCryptPasswordEncoder}, no manual salt handling needed.
     */
    @Column(nullable = false, length = 100)
    private String password;

    /** Display name shown in UI */
    @Column(nullable = false, length = 50)
    private String nickname;

    private String email;
    private String phone;
    private String avatar;

    /** Gender. Dict type: system_user_sex (0=unknown, 1=male, 2=female) */
    private Integer sex;

    /** Account status. 0=disabled, 1=enabled */
    private Integer status;

    /** Department ID. References {@link SystemDeptEntity#id} */
    private Long deptId;

    private String remark;

    /** Multi-tenant ID. Unused in Soar (always 0) */
    private Long tenantId;
}
```

Key rules:
- Extend `BaseEntity` (provides createdBy, createdAt, updatedBy, updatedAt, deleted)
- Use `@Getter @Setter` (Lombok), not manual getters/setters
- ID references only (`Long deptId`), never object references
- `@Table(name = "...")` must match RuoYi table name exactly

---

## Controller Pattern

```java
@RestController
@RequestMapping("/api/system/users")
@RequiredArgsConstructor
public class SystemUserController {

    private final SystemUserService userService;

    @GetMapping
    @PreAuthorize("@perm.has('system:user:list')")
    public CommonResult<PageResult<SystemUserRespDTO>> page(SystemUserPageReqDTO req) {
        return CommonResult.success(userService.page(req));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@perm.has('system:user:list')")
    public CommonResult<SystemUserRespDTO> get(@PathVariable Long id) {
        return CommonResult.success(userService.get(id));
    }

    @PostMapping
    @PreAuthorize("@perm.has('system:user:create')")
    public CommonResult<Long> create(@Valid @RequestBody SystemUserCreateReqDTO req) {
        return CommonResult.success(userService.create(req));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@perm.has('system:user:update')")
    public CommonResult<Boolean> update(@PathVariable Long id,
                                         @Valid @RequestBody SystemUserUpdateReqDTO req) {
        userService.update(id, req);
        return CommonResult.success(true);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@perm.has('system:user:delete')")
    public CommonResult<Boolean> delete(@PathVariable Long id) {
        userService.delete(id);
        return CommonResult.success(true);
    }
}
```

Key rules:
- Every method has `@PreAuthorize`
- Every method returns `CommonResult<T>`
- Use `@Valid` on request body
- Controller has NO business logic — delegates to service
- Use `@RequiredArgsConstructor` for dependency injection

---

## Service Pattern (System Module — Layered)

```java
// Interface
public interface SystemUserService {
    PageResult<SystemUserRespDTO> page(SystemUserPageReqDTO req);
    SystemUserRespDTO get(Long id);
    Long create(SystemUserCreateReqDTO req);
    void update(Long id, SystemUserUpdateReqDTO req);
    void delete(Long id);
}

// Implementation
@Service
@RequiredArgsConstructor
public class SystemUserServiceImpl implements SystemUserService {

    private final SystemUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Long create(SystemUserCreateReqDTO req) {
        // 1. Validate
        validateUsernameUnique(req.getUsername());

        // 2. Convert DTO → Entity
        SystemUserEntity user = new SystemUserEntity();
        user.setUsername(req.getUsername());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setNickname(req.getNickname());
        // ... other fields

        // 3. Save
        userRepository.save(user);
        return user.getId();
    }

    private void validateUsernameUnique(String username) {
        if (userRepository.existsByUsername(username)) {
            throw new ServiceException(ErrorCode.USER_USERNAME_DUPLICATE);
        }
    }
}
```

---

## Error Handling

```java
// ErrorCode enum — centralized error definitions
public enum ErrorCode {
    // System
    USER_NOT_FOUND(1_001_001, "User not found"),
    USER_USERNAME_DUPLICATE(1_001_002, "Username already exists"),
    USER_PASSWORD_INCORRECT(1_001_003, "Incorrect password"),
    ROLE_NOT_FOUND(1_002_001, "Role not found"),
    ROLE_NAME_DUPLICATE(1_002_002, "Role name already exists"),

    // Format: {module}{entity}{sequence}
    // 1_001_xxx = system module, user entity
    // 1_002_xxx = system module, role entity
    // 2_001_xxx = infra module, config entity
    ;

    private final int code;
    private final String message;
}

// Usage in service
throw new ServiceException(ErrorCode.USER_NOT_FOUND);

// GlobalExceptionHandler catches and wraps in CommonResult
```

---

## Redis Caching Convention

| Cache Key Pattern | TTL | Invalidation |
|------------------|-----|-------------|
| `auth:token:{userId}` | 30min (access), 7d (refresh) | On logout |
| `perm:user:{userId}` | Until invalidated | On role/menu change |
| `dict:data:{dictType}` | Until invalidated | On dict CRUD |
| `auth:login-lock:{username}` | 15min | Auto-expire |

---

## Git Conventions

- Branch: `feature/{phase}-{description}` (e.g., `feature/phase1-auth`)
- Commit: Conventional Commits format
  - `feat: add user CRUD API`
  - `fix: correct permission check in role service`
  - `refactor: extract BaseEntity audit fields`
  - `docs: update PHASE_PLAN task status`
