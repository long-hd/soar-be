-- V1_1_1__rename_dict_components.sql
UPDATE system_menu
SET component = 'system/dict-type/index'
WHERE id = 1150;

UPDATE system_menu
SET component = 'system/dict-data/index'
WHERE id = 1160;