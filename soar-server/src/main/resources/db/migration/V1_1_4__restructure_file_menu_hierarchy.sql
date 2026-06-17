-- =====================================================================
-- V1_1_4: Restructure File menu hierarchy
--
-- (same goal as before — split conflated 2100 into pure DIR + new MENU)
--
-- ORDER MATTERS: must free 2100's `tab_key='infra-file'` BEFORE inserting
-- new 2104 with the same tab_key. PostgreSQL unique constraints check
-- per-statement (IMMEDIATE), not per-transaction.
--
-- BEFORE:
--   2100 File Storage     (type=MENU, component=infra/file/index)
--   ├── 2101..2103 buttons
--   └── 2105 File Config  (type=MENU, component=infra/file-config/index)
--       └── 2106..2109 buttons
--
-- AFTER:
--   2100 File Management  (type=DIR, no component)
--   ├── 2104 File List    (type=MENU, component=infra/file/index)  -- NEW
--   │   └── 2101..2103 buttons (reparented to 2104)
--   └── 2105 File Config  (type=MENU, component=infra/file-config/index)
--       └── 2106..2109 buttons (unchanged)
-- =====================================================================

-- 1) Convert 2100 from MENU to DIR FIRST (clears tab_key — frees it for 2104)
UPDATE system_menu
SET name           = 'File Management',
    type           = 1,             -- DIR
    path           = 'file',
    icon           = 'ep:files',
    component      = NULL,
    component_name = NULL,
    tab_key        = NULL,          -- frees 'infra-file' for 2104
    update_time    = CURRENT_TIMESTAMP
WHERE id = 2100;

-- 2) Insert new "File List" menu (id 2104, child of 2100) — now claims freed tab_key
INSERT INTO system_menu (id, name, permission, type, sort, parent_id,
                         path, icon, component, component_name, tab_key,
                         status, visible, keep_alive, always_show,
                         creator, create_time, updater, update_time, deleted)
VALUES (2104, 'File List', NULL, 2, 10, 2100,
        'list', 'ep:document', 'infra/file/index', 'InfraFile', 'infra-file',
        0, true, true, true,
        1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false);

-- 3) Reparent file button permissions from 2100 → 2104 (no constraint issue)
UPDATE system_menu
SET parent_id   = 2104,
    update_time = CURRENT_TIMESTAMP
WHERE id IN (2101, 2102, 2103);

-- 4) Reset sequence (id 2104 inserted explicitly)
SELECT setval('system_menu_id_seq', (SELECT COALESCE(MAX(id), 1) FROM system_menu));