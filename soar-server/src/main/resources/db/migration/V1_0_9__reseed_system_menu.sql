-- =============================================================================
-- V1_0_9: Reseed system_menu with tab_key (Phase 5 scope)
-- Replaces piecemeal seeds. All type=2 menus include tab_key for FE routing.
-- Prerequisite: system_role_menu must be empty (no FK to break).
-- 1: System directory
-- 2: Infrastructure directory
-- 1100-1109: User Management
-- 1110-1119: Role Management
-- 1120-1129: Dept Management
-- 1130-1139: Menu Management
-- 1140-1149: Post Management
-- 1150-1169: Dict Type + Dict Data (hidden)
-- 1170-1179: Tenant Management
-- 1500-1509: Login Log
-- 1510-1519: Operate Log
-- 1990: User Profile (hidden)
-- 2000-2009: Infra Config
-- 2100-2109: File + File Config (giữ IDs Long đã seed)
-- 2200-2219: Job + Job Log
-- 2300-2319: API Access + API Error Log
-- =============================================================================

DELETE FROM system_menu;

-- Common timestamp + creator/updater for all seed rows (system bootstrap user id=1).
-- Postgres allows reusing CURRENT_TIMESTAMP throughout the migration; same instant.

-- =============================================================================
-- Top-level directories
-- =============================================================================
INSERT INTO system_menu (id, name, permission, type, sort, parent_id,
                         path, icon, component, component_name, tab_key,
                         status, visible, keep_alive, always_show,
                         creator, create_time, updater, update_time, deleted)
VALUES
    (1, 'System',         NULL, 1, 1, 0, NULL, 'ep:setting',  NULL, NULL, NULL,
     0, true, true, true, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false),
    (2, 'Infrastructure', NULL, 1, 2, 0, NULL, 'ep:platform', NULL, NULL, NULL,
     0, true, true, true, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false);

-- =============================================================================
-- 1100-1109 User Management
-- =============================================================================
INSERT INTO system_menu (id, name, permission, type, sort, parent_id,
                         path, icon, component, component_name, tab_key,
                         status, visible, keep_alive, always_show,
                         creator, create_time, updater, update_time, deleted)
VALUES
    (1100, 'User Management', NULL, 2, 1, 1, 'user', 'ep:user',
     'system/user/index', NULL, 'system-user',
     0, true, true, true, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false),
    (1101, 'User Query',         'system:user:query',         3, 1, 1100, NULL, NULL, NULL, NULL, NULL,
     0, true, true, true, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false),
    (1102, 'User Create',        'system:user:create',        3, 2, 1100, NULL, NULL, NULL, NULL, NULL,
     0, true, true, true, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false),
    (1103, 'User Update',        'system:user:update',        3, 3, 1100, NULL, NULL, NULL, NULL, NULL,
     0, true, true, true, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false),
    (1104, 'User Delete',        'system:user:delete',        3, 4, 1100, NULL, NULL, NULL, NULL, NULL,
     0, true, true, true, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false),
    (1105, 'User Reset Password','system:user:update-password',3, 5, 1100, NULL, NULL, NULL, NULL, NULL,
     0, true, true, true, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false);

-- =============================================================================
-- 1110-1119 Role Management
-- =============================================================================
INSERT INTO system_menu (id, name, permission, type, sort, parent_id,
                         path, icon, component, component_name, tab_key,
                         status, visible, keep_alive, always_show,
                         creator, create_time, updater, update_time, deleted)
VALUES
    (1110, 'Role Management', NULL, 2, 2, 1, 'role', 'ep:lock',
     'system/role/index', NULL, 'system-role',
     0, true, true, true, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false),
    (1111, 'Role Query',           'system:role:query',           3, 1, 1110, NULL, NULL, NULL, NULL, NULL,
     0, true, true, true, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false),
    (1112, 'Role Create',          'system:role:create',          3, 2, 1110, NULL, NULL, NULL, NULL, NULL,
     0, true, true, true, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false),
    (1113, 'Role Update',          'system:role:update',          3, 3, 1110, NULL, NULL, NULL, NULL, NULL,
     0, true, true, true, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false),
    (1114, 'Role Delete',          'system:role:delete',          3, 4, 1110, NULL, NULL, NULL, NULL, NULL,
     0, true, true, true, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false),
    (1115, 'Role Assign Menu',     'system:permission:assign-role-menu',     3, 5, 1110, NULL, NULL, NULL, NULL, NULL,
     0, true, true, true, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false),
    (1116, 'Role Assign DataScope','system:permission:assign-role-data-scope',3, 6, 1110, NULL, NULL, NULL, NULL, NULL,
     0, true, true, true, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false);

-- =============================================================================
-- 1120-1129 Department Management
-- =============================================================================
INSERT INTO system_menu (id, name, permission, type, sort, parent_id,
                         path, icon, component, component_name, tab_key,
                         status, visible, keep_alive, always_show,
                         creator, create_time, updater, update_time, deleted)
VALUES
    (1120, 'Department Management', NULL, 2, 3, 1, 'dept', 'ep:office-building',
     'system/dept/index', NULL, 'system-dept',
     0, true, true, true, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false),
    (1121, 'Dept Query',  'system:dept:query',  3, 1, 1120, NULL, NULL, NULL, NULL, NULL,
     0, true, true, true, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false),
    (1122, 'Dept Create', 'system:dept:create', 3, 2, 1120, NULL, NULL, NULL, NULL, NULL,
     0, true, true, true, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false),
    (1123, 'Dept Update', 'system:dept:update', 3, 3, 1120, NULL, NULL, NULL, NULL, NULL,
     0, true, true, true, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false),
    (1124, 'Dept Delete', 'system:dept:delete', 3, 4, 1120, NULL, NULL, NULL, NULL, NULL,
     0, true, true, true, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false);

-- =============================================================================
-- 1130-1139 Menu Management
-- =============================================================================
INSERT INTO system_menu (id, name, permission, type, sort, parent_id,
                         path, icon, component, component_name, tab_key,
                         status, visible, keep_alive, always_show,
                         creator, create_time, updater, update_time, deleted)
VALUES
    (1130, 'Menu Management', NULL, 2, 4, 1, 'menu', 'ep:menu',
     'system/menu/index', NULL, 'system-menu',
     0, true, true, true, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false),
    (1131, 'Menu Query',  'system:menu:query',  3, 1, 1130, NULL, NULL, NULL, NULL, NULL,
     0, true, true, true, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false),
    (1132, 'Menu Create', 'system:menu:create', 3, 2, 1130, NULL, NULL, NULL, NULL, NULL,
     0, true, true, true, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false),
    (1133, 'Menu Update', 'system:menu:update', 3, 3, 1130, NULL, NULL, NULL, NULL, NULL,
     0, true, true, true, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false),
    (1134, 'Menu Delete', 'system:menu:delete', 3, 4, 1130, NULL, NULL, NULL, NULL, NULL,
     0, true, true, true, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false);

-- =============================================================================
-- 1140-1149 Post Management
-- =============================================================================
INSERT INTO system_menu (id, name, permission, type, sort, parent_id,
                         path, icon, component, component_name, tab_key,
                         status, visible, keep_alive, always_show,
                         creator, create_time, updater, update_time, deleted)
VALUES
    (1140, 'Post Management', NULL, 2, 5, 1, 'post', 'ep:postcard',
     'system/post/index', NULL, 'system-post',
     0, true, true, true, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false),
    (1141, 'Post Query',  'system:post:query',  3, 1, 1140, NULL, NULL, NULL, NULL, NULL,
     0, true, true, true, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false),
    (1142, 'Post Create', 'system:post:create', 3, 2, 1140, NULL, NULL, NULL, NULL, NULL,
     0, true, true, true, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false),
    (1143, 'Post Update', 'system:post:update', 3, 3, 1140, NULL, NULL, NULL, NULL, NULL,
     0, true, true, true, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false),
    (1144, 'Post Delete', 'system:post:delete', 3, 4, 1140, NULL, NULL, NULL, NULL, NULL,
     0, true, true, true, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false);

-- =============================================================================
-- 1150-1169 Dictionary Management (type + data)
-- =============================================================================
INSERT INTO system_menu (id, name, permission, type, sort, parent_id,
                         path, icon, component, component_name, tab_key,
                         status, visible, keep_alive, always_show,
                         creator, create_time, updater, update_time, deleted)
VALUES
    -- Dict Type (visible)
    (1150, 'Dictionary Management', NULL, 2, 6, 1, 'dict', 'ep:reading',
     'system/dict/index', NULL, 'system-dict-type',
     0, true, true, true, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false),
    (1151, 'Dict Query',  'system:dict:query',  3, 1, 1150, NULL, NULL, NULL, NULL, NULL,
     0, true, true, true, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false),
    (1152, 'Dict Create', 'system:dict:create', 3, 2, 1150, NULL, NULL, NULL, NULL, NULL,
     0, true, true, true, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false),
    (1153, 'Dict Update', 'system:dict:update', 3, 3, 1150, NULL, NULL, NULL, NULL, NULL,
     0, true, true, true, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false),
    (1154, 'Dict Delete', 'system:dict:delete', 3, 4, 1150, NULL, NULL, NULL, NULL, NULL,
     0, true, true, true, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false),
    -- Dict Data (hidden — opened with ?tab=system-dict-data&dictType=...)
    (1160, 'Dictionary Data', NULL, 2, 1, 1150, 'dict/data', NULL,
     'system/dict/data/index', NULL, 'system-dict-data',
     0, false, true, true, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false);

-- =============================================================================
-- 1170-1179 Tenant Management
-- =============================================================================
INSERT INTO system_menu (id, name, permission, type, sort, parent_id,
                         path, icon, component, component_name, tab_key,
                         status, visible, keep_alive, always_show,
                         creator, create_time, updater, update_time, deleted)
VALUES
    (1170, 'Tenant Management', NULL, 2, 7, 1, 'tenant', 'ep:office-building',
     'system/tenant/index', NULL, 'system-tenant',
     0, true, true, true, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false),
    (1171, 'Tenant Query',  'system:tenant:query',  3, 1, 1170, NULL, NULL, NULL, NULL, NULL,
     0, true, true, true, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false),
    (1172, 'Tenant Create', 'system:tenant:create', 3, 2, 1170, NULL, NULL, NULL, NULL, NULL,
     0, true, true, true, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false),
    (1173, 'Tenant Update', 'system:tenant:update', 3, 3, 1170, NULL, NULL, NULL, NULL, NULL,
     0, true, true, true, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false),
    (1174, 'Tenant Delete', 'system:tenant:delete', 3, 4, 1170, NULL, NULL, NULL, NULL, NULL,
     0, true, true, true, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false);

-- =============================================================================
-- 1500-1509 Login Log
-- =============================================================================
INSERT INTO system_menu (id, name, permission, type, sort, parent_id,
                         path, icon, component, component_name, tab_key,
                         status, visible, keep_alive, always_show,
                         creator, create_time, updater, update_time, deleted)
VALUES
    (1500, 'Login Log', NULL, 2, 10, 1, 'login-log', 'ep:key',
     'system/loginlog/index', NULL, 'system-login-log',
     0, true, true, true, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false),
    (1501, 'Login Log Query',  'system:login-log:query',  3, 1, 1500, NULL, NULL, NULL, NULL, NULL,
     0, true, true, true, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false),
    (1502, 'Login Log Export', 'system:login-log:export', 3, 2, 1500, NULL, NULL, NULL, NULL, NULL,
     0, true, true, true, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false);

-- =============================================================================
-- 1510-1519 Operate Log
-- =============================================================================
INSERT INTO system_menu (id, name, permission, type, sort, parent_id,
                         path, icon, component, component_name, tab_key,
                         status, visible, keep_alive, always_show,
                         creator, create_time, updater, update_time, deleted)
VALUES
    (1510, 'Operate Log', NULL, 2, 11, 1, 'operate-log', 'ep:edit',
     'system/operatelog/index', NULL, 'system-operate-log',
     0, true, true, true, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false),
    (1511, 'Operate Log Query',  'system:operate-log:query',  3, 1, 1510, NULL, NULL, NULL, NULL, NULL,
     0, true, true, true, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false),
    (1512, 'Operate Log Export', 'system:operate-log:export', 3, 2, 1510, NULL, NULL, NULL, NULL, NULL,
     0, true, true, true, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false);

-- =============================================================================
-- 1990 User Profile (hidden — for top-right user menu)
-- =============================================================================
INSERT INTO system_menu (id, name, permission, type, sort, parent_id,
                         path, icon, component, component_name, tab_key,
                         status, visible, keep_alive, always_show,
                         creator, create_time, updater, update_time, deleted)
VALUES
    (1990, 'User Profile', NULL, 2, 99, 1, 'user-profile', NULL,
     'user-profile/index', NULL, 'user-profile',
     0, false, true, true, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false);

-- =============================================================================
-- 2000-2009 Infra Config Management
-- =============================================================================
INSERT INTO system_menu (id, name, permission, type, sort, parent_id,
                         path, icon, component, component_name, tab_key,
                         status, visible, keep_alive, always_show,
                         creator, create_time, updater, update_time, deleted)
VALUES
    (2000, 'Config Management', NULL, 2, 1, 2, 'config', 'ep:tools',
     'infra/config/index', NULL, 'infra-config',
     0, true, true, true, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false),
    (2001, 'Config Query',  'infra:config:query',  3, 1, 2000, NULL, NULL, NULL, NULL, NULL,
     0, true, true, true, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false),
    (2002, 'Config Create', 'infra:config:create', 3, 2, 2000, NULL, NULL, NULL, NULL, NULL,
     0, true, true, true, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false),
    (2003, 'Config Update', 'infra:config:update', 3, 3, 2000, NULL, NULL, NULL, NULL, NULL,
     0, true, true, true, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false),
    (2004, 'Config Delete', 'infra:config:delete', 3, 4, 2000, NULL, NULL, NULL, NULL, NULL,
     0, true, true, true, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false);

-- =============================================================================
-- 2100-2109 File Management + File Config (giữ IDs đã seed)
-- =============================================================================
INSERT INTO system_menu (id, name, permission, type, sort, parent_id,
                         path, icon, component, component_name, tab_key,
                         status, visible, keep_alive, always_show,
                         creator, create_time, updater, update_time, deleted)
VALUES
    (2100, 'File Management', NULL, 2, 10, 2, 'file', 'ep:folder',
     'infra/file/index', NULL, 'infra-file',
     0, true, true, true, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false),
    (2101, 'File Query',  'infra:file:query',  3, 1, 2100, NULL, NULL, NULL, NULL, NULL,
     0, true, true, true, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false),
    (2102, 'File Create', 'infra:file:create', 3, 2, 2100, NULL, NULL, NULL, NULL, NULL,
     0, true, true, true, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false),
    (2103, 'File Delete', 'infra:file:delete', 3, 3, 2100, NULL, NULL, NULL, NULL, NULL,
     0, true, true, true, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false),
    -- File Config sub-menu
    (2105, 'File Config', NULL, 2, 20, 2100, 'file-config', 'ep:setting',
     'infra/fileConfig/index', NULL, 'infra-file-config',
     0, true, true, true, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false),
    (2106, 'Config Query',  'infra:file-config:query',  3, 1, 2105, NULL, NULL, NULL, NULL, NULL,
     0, true, true, true, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false),
    (2107, 'Config Create', 'infra:file-config:create', 3, 2, 2105, NULL, NULL, NULL, NULL, NULL,
     0, true, true, true, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false),
    (2108, 'Config Update', 'infra:file-config:update', 3, 3, 2105, NULL, NULL, NULL, NULL, NULL,
     0, true, true, true, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false),
    (2109, 'Config Delete', 'infra:file-config:delete', 3, 4, 2105, NULL, NULL, NULL, NULL, NULL,
     0, true, true, true, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false);

-- =============================================================================
-- 2200-2219 Job Management + Job Log (hidden)
-- =============================================================================
INSERT INTO system_menu (id, name, permission, type, sort, parent_id,
                         path, icon, component, component_name, tab_key,
                         status, visible, keep_alive, always_show,
                         creator, create_time, updater, update_time, deleted)
VALUES
    (2200, 'Job Manager', NULL, 2, 20, 2, 'job', 'ep:alarm-clock',
     'infra/job/index', NULL, 'infra-job',
     0, true, true, true, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false),
    (2201, 'Job Query',   'infra:job:query',   3, 1, 2200, NULL, NULL, NULL, NULL, NULL,
     0, true, true, true, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false),
    (2202, 'Job Create',  'infra:job:create',  3, 2, 2200, NULL, NULL, NULL, NULL, NULL,
     0, true, true, true, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false),
    (2203, 'Job Update',  'infra:job:update',  3, 3, 2200, NULL, NULL, NULL, NULL, NULL,
     0, true, true, true, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false),
    (2204, 'Job Delete',  'infra:job:delete',  3, 4, 2200, NULL, NULL, NULL, NULL, NULL,
     0, true, true, true, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false),
    (2205, 'Job Trigger', 'infra:job:trigger', 3, 5, 2200, NULL, NULL, NULL, NULL, NULL,
     0, true, true, true, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false),
    -- Job Log (hidden — opened with ?tab=infra-job-log&jobId=...)
    (2210, 'Job Log', NULL, 2, 1, 2200, 'job/log', NULL,
     'infra/job/log/index', NULL, 'infra-job-log',
     0, false, true, true, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false);

-- =============================================================================
-- 2300-2319 API Access Log + API Error Log
-- =============================================================================
INSERT INTO system_menu (id, name, permission, type, sort, parent_id,
                         path, icon, component, component_name, tab_key,
                         status, visible, keep_alive, always_show,
                         creator, create_time, updater, update_time, deleted)
VALUES
    (2300, 'API Access Log', NULL, 2, 30, 2, 'api-access-log', 'ep:document',
     'infra/apiAccessLog/index', NULL, 'infra-api-access-log',
     0, true, true, true, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false),
    (2301, 'API Access Log Query', 'infra:api-access-log:query', 3, 1, 2300, NULL, NULL, NULL, NULL, NULL,
     0, true, true, true, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false),
    (2310, 'API Error Log', NULL, 2, 31, 2, 'api-error-log', 'ep:warning',
     'infra/apiErrorLog/index', NULL, 'infra-api-error-log',
     0, true, true, true, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false),
    (2311, 'API Error Log Query',   'infra:api-error-log:query',           3, 1, 2310, NULL, NULL, NULL, NULL, NULL,
     0, true, true, true, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false),
    (2312, 'API Error Log Process', 'infra:api-error-log:update-status',   3, 2, 2310, NULL, NULL, NULL, NULL, NULL,
     0, true, true, true, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false);

-- =============================================================================
-- Validation: every type=2 menu must have tab_key
-- =============================================================================
SELECT id, name, component
FROM system_menu
WHERE type = 2 AND tab_key IS NULL AND deleted = false;
-- Expected: 0 rows