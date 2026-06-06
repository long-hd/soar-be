-- Add tab_key column for flat-URL routing dispatcher
ALTER TABLE system_menu ADD COLUMN tab_key VARCHAR(100);

-- Partial unique index: enforce uniqueness only when tab_key is set
-- (type=1 directories and type=3 buttons have tab_key NULL)
CREATE UNIQUE INDEX system_menu_tab_key_uk
    ON system_menu (tab_key)
    WHERE tab_key IS NOT NULL AND deleted = false;

-- Document deprecated fields (kept for seed import compatibility)
COMMENT ON COLUMN system_menu.path IS
  'DEPRECATED since V1_0_8. Kept for seed import compatibility. Use tab_key for FE routing.';

COMMENT ON COLUMN system_menu.component_name IS
  'DEPRECATED since V1_0_8.';

COMMENT ON COLUMN system_menu.keep_alive IS
  'Used by FE to wrap route in <Activity> (React 19.2). Same semantic as Vue keep-alive.';

COMMENT ON COLUMN system_menu.tab_key IS
  'URL dispatcher key for FE flat-URL routing. Required for type=2 menus (pages). NULL for type=1 (directory) and type=3 (button).';