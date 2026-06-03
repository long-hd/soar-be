# Skill: Create CRUD Entity (System / Infra Module)

> Synced with code 2026-06-02. Use for system/infra modules (Layered). NOT for business modules (DDD).

## Input Needed
- Entity name (e.g. `DictType`), table name (e.g. `system_dict_type`), module (`system`/`infra`), fields with types/constraints, domain sub-package name.

## Steps

### 1. Entity — `dal/entity/{domain}/{Name}PO.java`
- Class `{Name}PO`, package `com.hdl.soar.module.{module}.dal.entity.{domain}`.
- Extend **`BasePO`** (global) or **`TenantBasePO`** (table has `tenant_id`).
- Lombok: `@Data @SuperBuilder(toBuilder=true) @EqualsAndHashCode(callSuper=true) @NoArgsConstructor @AllArgsConstructor`.
- `@Table(name="{table}")`, `@Id @GeneratedValue(IDENTITY)`. ID references only (no `@ManyToOne`).
- Enum field → enum + inner `IntEnumConverter.JpaConverter`. Integer/String enum-coded field → Javadoc with value mapping. Timestamps `Instant`; audit `Long`; `deleted` `Boolean`.

### 2. Repository — `dal/postgres/{domain}/{Name}Repository.java`
- `interface {Name}Repository extends JpaRepository<{Name}PO, Long>, JpaSpecificationExecutor<{Name}PO>`.
- Add derived finders as needed (`findBy...`, `existsBy...`). **No `deleteBy*`** (bypasses soft delete) — load then `delete(po)`.

### 3. Mapper — `mapper/{domain}/{Name}Mapper.java`
- MapStruct `@Mapper interface {Name}Mapper { {Name}Mapper INSTANCE = Mappers.getMapper(...); ... }`.
- `toPO(SaveReqDTO)`, `updatePO(SaveReqDTO, @MappingTarget {Name}PO)` with `@BeanMapping(nullValuePropertyMappingStrategy=IGNORE)`, `toRespDTO`, `toRespDTOList`.

### 4. DTOs — `controller/admin/{domain}/dto/`
- `{Name}SaveReqDTO` (create+update, nullable `id`, bean validation), `{Name}PageReqDTO extends PageParam`, `{Name}RespDTO`, optional `{Name}SimpleRespDTO`.

### 5. Service — `service/{domain}/`
- `{Name}Service` interface + `{Name}ServiceImpl` (alongside, not `impl/`). `@Service @RequiredArgsConstructor @FieldDefaults(makeFinal=true)`.
- CRUD + validation; `throw exception(ErrorCodeConstants.XXX)`. Page via `Specification` + `SpecUtils.*IfPresent` + Metamodel `{Name}PO_` + `PageUtils`.

### 6. Controller — `controller/admin/{domain}/{Name}Controller.java`
- `@Tag`, `@RestController`, `@RequestMapping("/{module}/{entity}")` (**no** `/admin-api` prefix — auto-added), `@Validated @RequiredArgsConstructor @FieldDefaults(makeFinal=true)`.
- Endpoints (yudao action-style): `POST /create`, `PUT /update`, `DELETE /delete?id=`, `GET /get?id=`, `GET /page`, `GET /export-excel`.
- Every method `@PreAuthorize("@ss.hasPermission('{module}:{entity}:{action}')")` and returns `CommonResult<T>`. Actions: `query`/`create`/`update`/`delete`/`export`.

### 7. ErrorCode — module `ErrorCodeConstants`
```java
ErrorCode {ENTITY}_NOT_EXISTS = new ErrorCode({m}_{grp}_{seq}, "{Entity} not found");
```

### 8. Migration seed (menu + permission [+ dict])
Follow current migration conventions (no hardcoded dict ids; menus use explicit ids only for parent_id refs + setval; no `DEFAULT ''`; empty optional → NULL). `system_menu.type`: MENU=2, BUTTON=3. Super-admin needs no `role_menu` rows.
```sql
-- MENU (type=2)
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name,
                         status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
VALUES ({menuId}, '{Entity}', NULL, 2, {sort}, {parentId}, '{path}', '{icon}', '{module}/{entity}/index', '{Comp}',
        0, true, true, true, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false);
-- BUTTON (type=3), parent = the MENU; path/icon/component NULL
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name,
                         status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
VALUES ({menuId}+1, 'Query',  '{module}:{entity}:query',  3, 1, {menuId}, NULL, NULL, NULL, NULL, 0, true, true, true, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false),
       ({menuId}+2, 'Create', '{module}:{entity}:create', 3, 2, {menuId}, NULL, NULL, NULL, NULL, 0, true, true, true, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false),
       ({menuId}+3, 'Update', '{module}:{entity}:update', 3, 3, {menuId}, NULL, NULL, NULL, NULL, 0, true, true, true, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false),
       ({menuId}+4, 'Delete', '{module}:{entity}:delete', 3, 4, {menuId}, NULL, NULL, NULL, NULL, 0, true, true, true, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false);
SELECT setval('system_menu_id_seq', (SELECT COALESCE(MAX(id),1) FROM system_menu));
```

## Verify
- [ ] `./mvnw -pl soar-module-{module} -am compile` passes
- [ ] `*PO` extends BasePO/TenantBasePO, ID refs only, enum fields have IntEnumConverter
- [ ] `@PreAuthorize` + `CommonResult<T>` on every method; no `/admin-api` in `@RequestMapping`
- [ ] Permission codes `{module}:{entity}:{action}`; menu (MENU+BUTTON) seed; migration follows NULL/no-DEFAULT rules
- [ ] English Javadoc; no Chinese

## Reference
Mirror an existing implementation in the same module (e.g. `ConfigController`/`ConfigService` in infra, `FileConfig*` for file storage).
