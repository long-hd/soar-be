-- =============================================================================
-- 1110-1119 Role Management
-- =============================================================================
INSERT INTO system_menu (id, name, permission, type, sort, parent_id,
                         path, icon, component, component_name, tab_key,
                         status, visible, keep_alive, always_show,
                         creator, create_time, updater, update_time, deleted)
VALUES
    (1117, 'Assign User Role', 'system:permission:assign-user-role', 3, 3, 1110, NULL, NULL, NULL, NULL, NULL,
     0, true, true, true, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false);