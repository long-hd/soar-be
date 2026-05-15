# Skill: Add Permission to Existing Entity

## When to Use
When adding a new action/button permission to an entity that already has CRUD set up.
Example: adding "export" or "reset-password" action to User entity.

## Input Needed
- Module (e.g., `system`)
- Entity (e.g., `user`)
- Action name (e.g., `export`, `reset-password`)
- Description (e.g., "Export users to Excel")

## Steps

### 1. Add Menu Seed Data (BUTTON record)
```sql
INSERT INTO system_menu (name, parent_id, type, permission, sort, status, deleted, tenant_id)
VALUES ('{Action Display Name}', {parent_menu_id}, 3, '{module}:{entity}:{action}', {sort}, 0, 0, 0);
```

### 2. Add Controller Method
```java
@PostMapping("/{id}/reset-password")  // or appropriate HTTP method + path
@PreAuthorize("@perm.has('{module}:{entity}:{action}')")
public CommonResult<Boolean> resetPassword(@PathVariable Long id, @RequestBody ...) {
    // delegate to service
    return CommonResult.success(true);
}
```

### 3. Add Service Method
Add to service interface + implementation.

### 4. Assign to Roles
If this permission should be given to existing roles, add role_menu records:
```sql
INSERT INTO system_role_menu (role_id, menu_id) VALUES ({role_id}, {new_menu_id});
```

### 5. Invalidate Permission Cache
After changing role_menu assignments, invalidate Redis cache:
`perm:user:{userId}` for affected users.

## Verify
- [ ] Permission code follows `{module}:{entity}:{action}` format
- [ ] Controller method has `@PreAuthorize`
- [ ] Menu seed data has type=3 (BUTTON)
- [ ] Menu parent_id points to correct MENU record
