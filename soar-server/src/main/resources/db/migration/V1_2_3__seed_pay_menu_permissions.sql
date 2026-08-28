-- =====================================================================
-- Pay module: menu tree + button permissions.
-- Permission strings are FINAL (match @PreAuthorize in pay controllers).
-- Menu grouping / tab_key / path / component are PROVISIONAL — pay FE
-- not built yet; FE may restructure menus later WITHOUT touching the
-- permission strings. type: 1=DIR, 2=MENU, 3=BUTTON.
-- =====================================================================

-- 1. Top-level directory: "Payment" (type=1 DIR, permission NULL, tab_key NULL)
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, tab_key,
                         status, visible, keep_alive, always_show,
                         creator, create_time, updater, update_time, deleted)
VALUES (2400, 'Payment', NULL, 1, 60, 0, 'pay', 'ep:money', NULL, NULL, NULL,
        0, true, true, true, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false);

-- 2. Applications page (type=2 MENU). App + Channel are managed together (channel is edited
--    within the app page), so both permission sets hang off this menu.
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, tab_key,
                         status, visible, keep_alive, always_show,
                         creator, create_time, updater, update_time, deleted)
VALUES (2410, 'Applications', NULL, 2, 10, 2400, 'app', 'ep:menu', 'pay/app/index', NULL, 'pay-app',
        0, true, true, true, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false);

INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, tab_key,
                         status, visible, keep_alive, always_show,
                         creator, create_time, updater, update_time, deleted)
VALUES
    (2411, 'App Query',      'pay:app:query',      3, 1, 2410, NULL, NULL, NULL, NULL, NULL, 0, true, true, true, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false),
    (2412, 'App Create',     'pay:app:create',     3, 2, 2410, NULL, NULL, NULL, NULL, NULL, 0, true, true, true, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false),
    (2413, 'App Update',     'pay:app:update',     3, 3, 2410, NULL, NULL, NULL, NULL, NULL, 0, true, true, true, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false),
    (2414, 'App Delete',     'pay:app:delete',     3, 4, 2410, NULL, NULL, NULL, NULL, NULL, 0, true, true, true, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false),
    (2415, 'Channel Query',  'pay:channel:query',  3, 5, 2410, NULL, NULL, NULL, NULL, NULL, 0, true, true, true, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false),
    (2416, 'Channel Create', 'pay:channel:create', 3, 6, 2410, NULL, NULL, NULL, NULL, NULL, 0, true, true, true, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false),
    (2417, 'Channel Update', 'pay:channel:update', 3, 7, 2410, NULL, NULL, NULL, NULL, NULL, 0, true, true, true, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false),
    (2418, 'Channel Delete', 'pay:channel:delete', 3, 8, 2410, NULL, NULL, NULL, NULL, NULL, 0, true, true, true, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false);

-- 3. Orders page (type=2 MENU). Order query + notify-task query (notify tasks are viewed in the
--    order context) hang off this menu.
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, tab_key,
                         status, visible, keep_alive, always_show,
                         creator, create_time, updater, update_time, deleted)
VALUES (2420, 'Orders', NULL, 2, 20, 2400, 'order', 'ep:list', 'pay/order/index', NULL, 'pay-order',
        0, true, true, true, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false);

INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, tab_key,
                         status, visible, keep_alive, always_show,
                         creator, create_time, updater, update_time, deleted)
VALUES
    (2421, 'Order Query',  'pay:order:query',  3, 1, 2420, NULL, NULL, NULL, NULL, NULL, 0, true, true, true, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false),
    (2422, 'Notify Query', 'pay:notify:query', 3, 2, 2420, NULL, NULL, NULL, NULL, NULL, 0, true, true, true, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false);

-- 4. Refunds page (type=2 MENU).
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, tab_key,
                         status, visible, keep_alive, always_show,
                         creator, create_time, updater, update_time, deleted)
VALUES (2430, 'Refunds', NULL, 2, 30, 2400, 'refund', 'ep:refresh-left', 'pay/refund/index', NULL, 'pay-refund',
        0, true, true, true, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false);

INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, tab_key,
                         status, visible, keep_alive, always_show,
                         creator, create_time, updater, update_time, deleted)
VALUES
    (2431, 'Refund Query', 'pay:refund:query', 3, 1, 2430, NULL, NULL, NULL, NULL, NULL, 0, true, true, true, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false);

-- 5. Repair the sequence after explicit-id inserts (mirrors V1_0_7).
SELECT setval(pg_get_serial_sequence('system_menu', 'id'), (SELECT MAX(id) FROM system_menu));