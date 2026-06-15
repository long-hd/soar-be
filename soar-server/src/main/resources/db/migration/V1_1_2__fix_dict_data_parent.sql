-- Move dict-data from being a child of Dictionary (1150) to being a child of System (1)
-- Reasoning: dict-data is a sibling entity to dict-type, linked by reference (FK on type code),
-- not a hierarchical sub-feature. Independent parent_id allows separate permission grants
-- and reflects the true sibling relationship.
UPDATE system_menu
SET parent_id = 1
WHERE id = 1160;