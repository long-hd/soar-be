# Skill: Add Permission to Existing Entity

> Synced with code 2026-06-02.

## When to Use
Adding a new action/button permission to an entity that already has CRUD (e.g. `export`, `reset-password`).

## Input Needed
Module, entity, action name, display name.

## Steps

### 1. Menu seed (BUTTON, type=3)
Add to the appropriate migration (DB not in prod → in-place edit). path/icon/component = NULL.
```sql
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name,
                         status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
VALUES ({newId}, '{Action}', '{module}:{entity}:{action}', 3, {sort}, {parentMenuId}, NULL, NULL, NULL, NULL,
        0, true, true, true, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false);
SELECT setval('system_menu_id_seq', (SELECT COALESCE(MAX(id),1) FROM system_menu));
```

### 2. Controller method
```java
@PostMapping("/{action}")     // or appropriate verb/path, yudao action-style
@Operation(summary = "...")
@PreAuthorize("@ss.hasPermission('{module}:{entity}:{action}')")
public CommonResult<Boolean> {action}(@RequestParam("id") Long id) {
    {entity}Service.{action}(id);
    return success(true);
}
```

### 3. Service method
Add to interface + impl.

### 4. Assign to roles (optional)
Super-admin already has everything (granted in code). For other roles, bind `system_role_menu(role_id, menu_id)`.

### 5. Cache
Role/menu changes go through the service which evicts the permission cache; no manual Redis key manipulation needed.

## Verify
- [ ] Permission code `{module}:{entity}:{action}`
- [ ] `@PreAuthorize` present; BUTTON seed type=3 with correct parent_id; NULL (not '') for unused columns
