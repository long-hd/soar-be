# Skill: Create CRUD Entity (System Module)

## When to Use
When creating a new CRUD entity in the system or infra module (Layered Architecture).
Do NOT use this for business modules (logistics) — those use DDD pattern.

## Input Needed
- Entity name (e.g., `DictType`)
- Table name (e.g., `system_dict_type`)
- Module (e.g., `system`)
- Fields list with types and constraints

## Steps

### 1. Entity
Create in `soar-module-{module}-biz/src/.../entity/`

- Class name: `{Name}Entity.java` (e.g., `DictTypeEntity.java`)
- Extend `BaseEntity`
- `@Table(name = "{table_name}")` matching RuoYi exactly
- ID references only, no `@ManyToOne`
- Include `tenantId` field if RuoYi table has it

### 2. Repository
Create in `soar-module-{module}-biz/src/.../repository/`

- Interface: `{Name}Repository extends JpaRepository<{Name}Entity, Long>`
- Add custom query methods as needed (e.g., `existsByUsername`)

### 3. Service
Create in `soar-module-{module}-biz/src/.../service/`

- Interface: `{Name}Service.java` with CRUD method signatures
- Impl: `{Name}ServiceImpl.java` in `service/impl/`
- Include validation methods (uniqueness checks, existence checks)
- Throw `ServiceException(ErrorCode.XXX)` for business errors

### 4. DTOs
Create in `soar-module-{module}-biz/src/.../controller/dto/`

- `{Name}CreateReqDTO.java` — fields for creation, with `@NotBlank`, `@NotNull` etc.
- `{Name}UpdateReqDTO.java` — same as create but with `id` field
- `{Name}RespDTO.java` — fields for response (may include joined data)
- `{Name}PageReqDTO.java` — extends `PageParam`, adds filter fields

### 5. Controller
Create in `soar-module-{module}-biz/src/.../controller/`

- `{Name}Controller.java`
- `@RequestMapping("/api/{module}/{entity-plural}")`
- 5 standard methods: page, get, create, update, delete
- Every method has `@PreAuthorize("@perm.has('{module}:{entity}:{action}')")`
- Every method returns `CommonResult<T>`

### 6. ErrorCode
Add to `ErrorCode.java`:

```java
{ENTITY}_NOT_FOUND({module_code}_{entity_code}_001, "{Entity} not found"),
{ENTITY}_NAME_DUPLICATE({module_code}_{entity_code}_002, "Name already exists"),
```

### 7. Menu Seed Data
Add Flyway migration or SQL insert:

```sql
-- MENU record (type=2)
INSERT INTO system_menu (name, parent_id, type, path, component, permission, icon, sort, status, deleted, tenant_id)
VALUES ('{Entity} Management', {parent_id}, 2, '{entity}', '{module}/{entity}/index', '{module}:{entity}:list', '{icon}', {sort}, 0, 0, 0);

-- BUTTON records (type=3), parent_id = the MENU above
INSERT INTO system_menu (name, parent_id, type, permission, sort, status, deleted, tenant_id)
VALUES ('Create', {menu_id}, 3, '{module}:{entity}:create', 1, 0, 0, 0);
INSERT INTO system_menu (name, parent_id, type, permission, sort, status, deleted, tenant_id)
VALUES ('Update', {menu_id}, 3, '{module}:{entity}:update', 2, 0, 0, 0);
INSERT INTO system_menu (name, parent_id, type, permission, sort, status, deleted, tenant_id)
VALUES ('Delete', {menu_id}, 3, '{module}:{entity}:delete', 3, 0, 0, 0);
```

## Verify
- [ ] `mvn compile` passes
- [ ] Entity extends BaseEntity, uses ID refs only
- [ ] `@PreAuthorize` on every controller method
- [ ] `CommonResult<T>` return type on every endpoint
- [ ] Permission codes follow `{module}:{entity}:{action}`
- [ ] Menu seed data includes MENU + BUTTON records

## Reference
Look at existing implementations in the same module for patterns to follow.
