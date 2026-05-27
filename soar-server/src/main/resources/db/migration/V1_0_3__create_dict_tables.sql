-- =====================================================================
-- Soar V1.0.3 — Dictionary tables (DictType + DictData)
--
-- No tenant_id — dict data is shared across tenants (extends BasePO)
-- =====================================================================

-- =====================================================================
-- Dict Type (dictionary category)
-- =====================================================================

CREATE TABLE system_dict_type (
    id              bigserial       PRIMARY KEY,
    name            varchar(100)    NOT NULL,
    type            varchar(100)    NOT NULL,
    status          int4            NOT NULL DEFAULT 0,
    remark          varchar(500),
    creator         int8,
    create_time     timestamptz     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updater         int8,
    update_time     timestamptz     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted         boolean         NOT NULL DEFAULT false
);

-- Unique type code among active records
CREATE UNIQUE INDEX uk_dict_type_type ON system_dict_type (type) WHERE deleted = false;

COMMENT ON TABLE  system_dict_type          IS 'Dictionary type';
COMMENT ON COLUMN system_dict_type.name     IS 'Dictionary name';
COMMENT ON COLUMN system_dict_type.type     IS 'Dictionary type code (unique key)';
COMMENT ON COLUMN system_dict_type.status   IS 'Status: 0=enabled, 1=disabled';

-- =====================================================================
-- Dict Data (dictionary entries)
-- =====================================================================

CREATE TABLE system_dict_data (
    id              bigserial       PRIMARY KEY,
    sort            int4            NOT NULL DEFAULT 0,
    label           varchar(100)    NOT NULL,
    value           varchar(100)    NOT NULL,
    dict_type       varchar(100)    NOT NULL,
    status          int4            NOT NULL DEFAULT 0,
    color_type      varchar(100)    DEFAULT '',
    css_class       varchar(100)    DEFAULT '',
    remark          varchar(500),
    creator         int8,
    create_time     timestamptz     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updater         int8,
    update_time     timestamptz     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted         boolean         NOT NULL DEFAULT false
);

-- Unique value per dict_type among active records
CREATE UNIQUE INDEX uk_dict_data_type_value ON system_dict_data (dict_type, value) WHERE deleted = false;

COMMENT ON TABLE  system_dict_data              IS 'Dictionary data';
COMMENT ON COLUMN system_dict_data.sort         IS 'Display order';
COMMENT ON COLUMN system_dict_data.label        IS 'Display label';
COMMENT ON COLUMN system_dict_data.value        IS 'Data value (stored in business tables)';
COMMENT ON COLUMN system_dict_data.dict_type    IS 'Dictionary type code (FK to system_dict_type.type)';
COMMENT ON COLUMN system_dict_data.status       IS 'Status: 0=enabled, 1=disabled';
COMMENT ON COLUMN system_dict_data.color_type   IS 'UI color: default/primary/success/info/warning/danger';
COMMENT ON COLUMN system_dict_data.css_class    IS 'CSS class for frontend styling';

-- =====================================================================
-- Seed: Dict Types
-- =====================================================================

INSERT INTO system_dict_type (id, name, type, status, remark, creator, create_time, updater, update_time, deleted) VALUES
    (1,  'Common Status',    'common_status',        0, 'Enabled/Disabled status used across modules', 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false),
    (2,  'User Sex',         'system_user_sex',      0, NULL, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false),
    (3,  'Menu Type',        'system_menu_type',      0, NULL, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false),
    (4,  'Role Type',        'system_role_type',      0, NULL, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false),
    (5,  'Data Scope',       'system_data_scope',     0, NULL, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false),
    (6,  'User Type',        'user_type',             0, NULL, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false),
    (7,  'Login Type',       'system_login_type',     0, NULL, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false),
    (8,  'Login Result',     'system_login_result',   0, NULL, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false),
    (9,  'Boolean',          'infra_boolean_string',  0, 'Yes/No boolean display', 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false),
    (10, 'Operate Type',     'infra_operate_type',    0, 'API operation log types', 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false);

-- =====================================================================
-- Seed: Dict Data
-- =====================================================================

INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES
    -- common_status
    (1,  1, 'Enabled',  '0', 'common_status', 0, 'success', '', NULL, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false),
    (2,  2, 'Disabled', '1', 'common_status', 0, 'danger',  '', NULL, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false),

    -- system_user_sex
    (3,  1, 'Male',    '1', 'system_user_sex', 0, 'primary', '', NULL, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false),
    (4,  2, 'Female',  '2', 'system_user_sex', 0, 'success', '', NULL, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false),
    (5,  3, 'Unknown', '0', 'system_user_sex', 0, 'default', '', NULL, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false),

    -- system_menu_type
    (6,  1, 'Directory', '1', 'system_menu_type', 0, 'primary', '', NULL, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false),
    (7,  2, 'Menu',      '2', 'system_menu_type', 0, 'success', '', NULL, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false),
    (8,  3, 'Button',    '3', 'system_menu_type', 0, 'info',    '', NULL, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false),

    -- system_role_type
    (9,  1, 'Built-in', '1', 'system_role_type', 0, 'danger',  '', NULL, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false),
    (10, 2, 'Custom',   '2', 'system_role_type', 0, 'primary', '', NULL, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false),

    -- system_data_scope
    (11, 1, 'All Data',                    '1', 'system_data_scope', 0, 'default', '', NULL, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false),
    (12, 2, 'Custom Department Data',      '2', 'system_data_scope', 0, 'default', '', NULL, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false),
    (13, 3, 'Own Department Data',         '3', 'system_data_scope', 0, 'default', '', NULL, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false),
    (14, 4, 'Department & Children Data',  '4', 'system_data_scope', 0, 'default', '', NULL, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false),
    (15, 5, 'Own Data Only',               '5', 'system_data_scope', 0, 'default', '', NULL, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false),

    -- user_type
    (16, 1, 'Member',        '1', 'user_type', 0, 'primary', '', NULL, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false),
    (17, 2, 'Administrator', '2', 'user_type', 0, 'success', '', NULL, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false),

    -- system_login_type
    (18, 1, 'Username Login', '100', 'system_login_type', 0, 'primary', '', NULL, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false),
    (19, 2, 'Social Login',   '101', 'system_login_type', 0, 'info',    '', NULL, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false),
    (20, 3, 'Mobile Login',   '103', 'system_login_type', 0, 'default', '', NULL, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false),
    (21, 4, 'SMS Login',      '104', 'system_login_type', 0, 'default', '', NULL, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false),
    (22, 5, 'Self Logout',    '200', 'system_login_type', 0, 'warning', '', NULL, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false),
    (23, 6, 'Forced Logout',  '202', 'system_login_type', 0, 'danger',  '', NULL, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false),

    -- system_login_result
    (24, 1, 'Success',            '0',  'system_login_result', 0, 'success', '', NULL, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false),
    (25, 2, 'Bad Credentials',    '10', 'system_login_result', 0, 'danger',  '', NULL, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false),
    (26, 3, 'User Disabled',      '20', 'system_login_result', 0, 'warning', '', NULL, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false),
    (27, 4, 'Captcha Not Found',  '30', 'system_login_result', 0, 'info',    '', NULL, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false),
    (28, 5, 'Captcha Code Error', '31', 'system_login_result', 0, 'info',    '', NULL, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false),

    -- infra_boolean_string
    (29, 1, 'Yes', 'true',  'infra_boolean_string', 0, 'success', '', NULL, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false),
    (30, 2, 'No',  'false', 'infra_boolean_string', 0, 'danger',  '', NULL, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false),

    -- infra_operate_type
    (31, 1, 'GET',    '1', 'infra_operate_type', 0, 'info',    '', NULL, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false),
    (32, 2, 'POST',   '2', 'infra_operate_type', 0, 'success', '', NULL, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false),
    (33, 3, 'PUT',    '3', 'infra_operate_type', 0, 'warning', '', NULL, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false),
    (34, 4, 'DELETE', '4', 'infra_operate_type', 0, 'danger',  '', NULL, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false),
    (35, 5, 'EXPORT', '5', 'infra_operate_type', 0, 'default', '', NULL, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false),
    (36, 6, 'IMPORT', '6', 'infra_operate_type', 0, 'default', '', NULL, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false);

-- =====================================================================
-- Reset sequences
-- =====================================================================
SELECT setval('system_dict_type_id_seq', (SELECT COALESCE(MAX(id), 1) FROM system_dict_type));
SELECT setval('system_dict_data_id_seq', (SELECT COALESCE(MAX(id), 1) FROM system_dict_data));
