-- =====================================================================
-- V1_0_7: Seed File Storage data (Infrastructure module)
--   1. Master LOCAL file config (zero-setup dev uploads)
--   2. File Storage menus + button permissions
--   3. infra_file_storage dict (type + data) for the FE storage-type dropdown
--
-- Notes:
--   - Super admin (role 1) is granted all permissions in code, so no
--     system_role_menu rows are seeded here.
--   - Dict seeds use no hardcoded ids (sequence auto-assigns).
--   - Menus use explicit ids for parent_id references, with a setval reset.
-- =====================================================================

-- ---------------------------------------------------------------------
-- 1. Master LOCAL file config (config JSON carries no @class)
-- ---------------------------------------------------------------------
INSERT INTO infra_file_config (name, storage, master, config, remark,
                               creator, create_time, updater, update_time, deleted)
VALUES ('Local Dev Storage', 10, true,
        '{"basePath":"/tmp/soar-files","domain":"http://127.0.0.1:48080"}',
        'Default local storage for development',
        1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false);

-- ---------------------------------------------------------------------
-- 2. Menus + permissions
--    Parent menu (DIR/MENU) + button permissions for file + file-config.
--    Ids 2100..2110 chosen in a high range to avoid colliding with future
--    system menus; adjust if your menu id plan differs.
-- ---------------------------------------------------------------------
-- 2.1 Parent menu: "File Storage" (type=2 MENU), under an infra parent if you have one;
--     here parent_id=0 (top-level) — move under your Infra directory id if it exists.
--     permission is NULL (MENU/DIR rows carry no permission string).
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name,
                         status, visible, keep_alive, always_show,
                         creator, create_time, updater, update_time, deleted)
VALUES (2100, 'File Storage', NULL, 2, 10, 0, 'file', 'ep:files', 'infra/file/index', 'InfraFile',
        0, true, true, true,
        1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false);

-- 2.2 File button permissions (type=3 BUTTON, parent = 2100).
--     path/icon/component/component_name are NULL — buttons have no route/component.
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name,
                         status, visible, keep_alive, always_show,
                         creator, create_time, updater, update_time, deleted)
VALUES
    (2101, 'File Query',  'infra:file:query',  3, 1, 2100, NULL, NULL, NULL, NULL,
     0, true, true, true, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false),
    (2102, 'File Create', 'infra:file:create', 3, 2, 2100, NULL, NULL, NULL, NULL,
     0, true, true, true, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false),
    (2103, 'File Delete', 'infra:file:delete', 3, 3, 2100, NULL, NULL, NULL, NULL,
     0, true, true, true, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false);

-- 2.3 File config menu: "File Config" (type=2 MENU, parent = 2100). permission NULL.
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name,
                         status, visible, keep_alive, always_show,
                         creator, create_time, updater, update_time, deleted)
VALUES (2105, 'File Config', NULL, 2, 20, 2100, 'file-config', 'ep:setting', 'infra/fileConfig/index', 'InfraFileConfig',
        0, true, true, true,
        1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false);

-- 2.4 File config button permissions (type=3 BUTTON, parent = 2105). path/icon/component NULL.
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name,
                         status, visible, keep_alive, always_show,
                         creator, create_time, updater, update_time, deleted)
VALUES
    (2106, 'Config Query',  'infra:file-config:query',  3, 1, 2105, NULL, NULL, NULL, NULL,
     0, true, true, true, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false),
    (2107, 'Config Create', 'infra:file-config:create', 3, 2, 2105, NULL, NULL, NULL, NULL,
     0, true, true, true, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false),
    (2108, 'Config Update', 'infra:file-config:update', 3, 3, 2105, NULL, NULL, NULL, NULL,
     0, true, true, true, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false),
    (2109, 'Config Delete', 'infra:file-config:delete', 3, 4, 2105, NULL, NULL, NULL, NULL,
     0, true, true, true, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false);

-- ---------------------------------------------------------------------
-- 3. Dict: infra_file_storage (type + data) — no hardcoded ids
-- ---------------------------------------------------------------------
INSERT INTO system_dict_type (name, type, status, remark, creator, create_time, updater, update_time, deleted) VALUES
    ('File Storage Type', 'infra_file_storage', 0,
     'File storage backend type', 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false);

INSERT INTO system_dict_data (sort, label, value, dict_type, status, color_type, css_class, remark,
                              creator, create_time, updater, update_time, deleted) VALUES
    (1, 'DB',    '1',  'infra_file_storage', 0, 'default', '', 'Store bytes in infra_file_content',
     1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false),
    (2, 'Local', '10', 'infra_file_storage', 0, 'primary', '', 'Local disk',
     1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false),
    (3, 'S3',    '20', 'infra_file_storage', 0, 'success', '', 'S3-compatible (SeaweedFS, AWS S3)',
     1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false);

-- ---------------------------------------------------------------------
-- Reset sequences after explicit-id inserts
--   infra_file_config + system_menu used explicit values above.
--   dict tables did NOT use explicit ids, so no reset needed for them.
-- ---------------------------------------------------------------------
SELECT setval('infra_file_config_id_seq', (SELECT COALESCE(MAX(id), 1) FROM infra_file_config));
SELECT setval('system_menu_id_seq',       (SELECT COALESCE(MAX(id), 1) FROM system_menu));