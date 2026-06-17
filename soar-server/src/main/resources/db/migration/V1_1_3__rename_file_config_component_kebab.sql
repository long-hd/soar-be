-- Rename component path 2105 from camelCase to kebab-case for FE pages glob consistency.
--
-- FE pages glob: `src/pages/infra/file-config/index.tsx` will match after this change.

UPDATE system_menu
SET component = 'infra/file-config/index',
    update_time = CURRENT_TIMESTAMP
WHERE id = 2105 AND component = 'infra/fileConfig/index';

-- Menu 2100 already uses 'infra/file/index' (single-word, kebab-compatible) — no change needed.