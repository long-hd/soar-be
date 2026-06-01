-- =====================================================================
-- V1_0_6: Create Operate Log Table
-- =====================================================================

CREATE TABLE system_operate_log (
    id              bigserial       PRIMARY KEY,
    trace_id        varchar(64),
    user_id         int8            NOT NULL,
    user_type       int4            NOT NULL DEFAULT 0,
    module          varchar(50)     NOT NULL,
    name            varchar(50)     NOT NULL,
    biz_id          int8            NOT NULL,
    content         varchar(2000),
    extra           varchar(2000),
    request_method  varchar(16),
    request_url     varchar(255),
    user_ip         varchar(50),
    user_agent      varchar(512),
    creator         int8,
    create_time     timestamptz     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updater         int8,
    update_time     timestamptz     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted         boolean         NOT NULL DEFAULT false,
    tenant_id       int8            NOT NULL DEFAULT 0
);

CREATE INDEX idx_system_operate_log_user_id ON system_operate_log (user_id);
CREATE INDEX idx_system_operate_log_create_time ON system_operate_log (create_time);
