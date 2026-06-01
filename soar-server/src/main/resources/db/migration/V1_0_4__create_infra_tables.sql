-- =====================================================================
-- V1_0_4: Create Infrastructure Module Tables
-- =====================================================================

-- ---------------------------------------------------------------------
-- 1. API Access Log
-- ---------------------------------------------------------------------
CREATE TABLE infra_api_access_log (
    id                  bigserial       PRIMARY KEY,
    trace_id            varchar(64),
    user_id             int8,
    user_type           int4,
    application_name    varchar(50)     NOT NULL,
    request_method      varchar(16)     NOT NULL,
    request_url         varchar(255)    NOT NULL,
    request_params      text,
    response_body       text,
    user_ip             varchar(50)     NOT NULL,
    user_agent          varchar(512)    NOT NULL,
    operate_module      varchar(50),
    operate_name        varchar(50),
    operate_type        int4,
    begin_time          timestamptz     NOT NULL,
    end_time            timestamptz     NOT NULL,
    duration            int4            NOT NULL,
    result_code         int4            NOT NULL DEFAULT 0,
    result_msg          varchar(512),
    creator             int8,
    create_time         timestamptz     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updater             int8,
    update_time         timestamptz     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted             boolean         NOT NULL DEFAULT false,
    tenant_id           int8            NOT NULL DEFAULT 0
);

CREATE INDEX idx_infra_api_access_log_create_time ON infra_api_access_log (create_time);

-- ---------------------------------------------------------------------
-- 2. API Error Log
-- ---------------------------------------------------------------------
CREATE TABLE infra_api_error_log (
    id                          bigserial       PRIMARY KEY,
    trace_id                    varchar(64)     NOT NULL,
    user_id                     int8,
    user_type                   int4,
    application_name            varchar(50)     NOT NULL,
    request_method              varchar(16)     NOT NULL,
    request_url                 varchar(255)    NOT NULL,
    request_params              text            NOT NULL,
    user_ip                     varchar(50)     NOT NULL,
    user_agent                  varchar(512)    NOT NULL,
    exception_time              timestamptz     NOT NULL,
    exception_name              varchar(128)    NOT NULL,
    exception_message           text            NOT NULL,
    exception_root_cause_message text           NOT NULL,
    exception_stack_trace        text           NOT NULL,
    exception_class_name        varchar(512)    NOT NULL,
    exception_file_name         varchar(512)    NOT NULL,
    exception_method_name       varchar(512)    NOT NULL,
    exception_line_number       int4            NOT NULL,
    process_status              int4            NOT NULL DEFAULT 0,
    process_time                timestamptz,
    process_user_id             int8,
    creator                     int8,
    create_time                 timestamptz     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updater                     int8,
    update_time                 timestamptz     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted                     boolean         NOT NULL DEFAULT false,
    tenant_id                   int8            NOT NULL DEFAULT 0
);

CREATE INDEX idx_infra_api_error_log_create_time ON infra_api_error_log (create_time);

-- ---------------------------------------------------------------------
-- 3. System Config (key-value configuration)
-- ---------------------------------------------------------------------
CREATE TABLE infra_config (
    id              bigserial       PRIMARY KEY,
    category        varchar(50)     NOT NULL,
    type            int4            NOT NULL,
    name            varchar(100)    NOT NULL,
    config_key      varchar(100)    NOT NULL,
    value           varchar(500),
    visible         boolean         NOT NULL DEFAULT true,
    remark          varchar(500),
    creator         int8,
    create_time     timestamptz     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updater         int8,
    update_time     timestamptz     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted         boolean         NOT NULL DEFAULT false
);

CREATE UNIQUE INDEX uk_infra_config_key ON infra_config (config_key) WHERE deleted = false;

-- Seed: initial password config
INSERT INTO infra_config (id, category, type, name, config_key, value, visible, remark, creator, updater)
VALUES (1, 'biz', 1, 'User initial password', 'system.user.init-password', '123456', false,
        'Default password for new user accounts', 1, 1);

-- ---------------------------------------------------------------------
-- 4. File metadata
-- ---------------------------------------------------------------------
CREATE TABLE infra_file (
    id              bigserial       PRIMARY KEY,
    config_id       int8,
    name            varchar(256),
    path            varchar(512)    NOT NULL,
    url             varchar(1024)   NOT NULL,
    type            varchar(128),
    size            int4            NOT NULL,
    creator         int8,
    create_time     timestamptz     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updater         int8,
    update_time     timestamptz     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted         boolean         NOT NULL DEFAULT false
);

-- ---------------------------------------------------------------------
-- 5. File storage configuration
-- ---------------------------------------------------------------------
CREATE TABLE infra_file_config (
    id              bigserial       PRIMARY KEY,
    name            varchar(63)     NOT NULL,
    storage         int4            NOT NULL,
    remark          varchar(255),
    master          boolean         NOT NULL DEFAULT false,
    config          varchar(4096)   NOT NULL,
    creator         int8,
    create_time     timestamptz     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updater         int8,
    update_time     timestamptz     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted         boolean         NOT NULL DEFAULT false
);

-- ---------------------------------------------------------------------
-- 6. File content (DB-based file storage)
-- ---------------------------------------------------------------------
CREATE TABLE infra_file_content (
    id              bigserial       PRIMARY KEY,
    config_id       int8            NOT NULL,
    path            varchar(512)    NOT NULL,
    content         bytea           NOT NULL,
    creator         int8,
    create_time     timestamptz     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updater         int8,
    update_time     timestamptz     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted         boolean         NOT NULL DEFAULT false
);

CREATE INDEX idx_infra_file_content_config_path ON infra_file_content (config_id, path);
