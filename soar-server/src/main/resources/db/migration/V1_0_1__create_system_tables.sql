-- =====================================================================
-- Soar V1.0.1 — System module tables
-- Database: PostgreSQL
--
-- Differences from yudao reference:
--   creator/updater  : int8     (user ID, not varchar username)
--   create_time/...  : timestamptz  (Instant, not timestamp)
--   deleted          : boolean  (not int4)
--   id               : bigserial (GenerationType.IDENTITY)
-- =====================================================================

-- =====================================================================
-- Tenant
-- =====================================================================

CREATE TABLE system_tenant_package (
    id              bigserial       PRIMARY KEY,
    name            varchar(30)     NOT NULL,
    status          int4            NOT NULL DEFAULT 0,
    remark          varchar(256)    DEFAULT '',
    menu_ids        varchar(4096)   NOT NULL DEFAULT '[]',
    creator         int8,
    create_time     timestamptz     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updater         int8,
    update_time     timestamptz     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted         boolean         NOT NULL DEFAULT false
);

CREATE TABLE system_tenant (
    id              bigserial       PRIMARY KEY,
    name            varchar(30)     NOT NULL,
    contact_user_id int8,
    contact_name    varchar(30),
    contact_mobile  varchar(500),
    status          int4            NOT NULL DEFAULT 0,
    websites        varchar(1024)   DEFAULT '[]',
    package_id      int8            NOT NULL,
    expire_time     timestamptz,
    account_count   int4,
    creator         int8,
    create_time     timestamptz     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updater         int8,
    update_time     timestamptz     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted         boolean         NOT NULL DEFAULT false
);

-- =====================================================================
-- Organization: Department, Post
-- =====================================================================

CREATE TABLE system_dept (
    id              bigserial       PRIMARY KEY,
    name            varchar(30)     NOT NULL,
    parent_id       int8            NOT NULL DEFAULT 0,
    sort            int4            NOT NULL DEFAULT 0,
    leader_user_id  int8,
    phone           varchar(11),
    email           varchar(50),
    status          int4            NOT NULL,
    creator         int8,
    create_time     timestamptz     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updater         int8,
    update_time     timestamptz     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted         boolean         NOT NULL DEFAULT false,
    tenant_id       int8            NOT NULL DEFAULT 0
);

CREATE TABLE system_post (
    id              bigserial       PRIMARY KEY,
    name            varchar(50)     NOT NULL,
    code            varchar(64)     NOT NULL,
    sort            int4            NOT NULL,
    status          int4            NOT NULL,
    remark          varchar(500),
    creator         int8,
    create_time     timestamptz     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updater         int8,
    update_time     timestamptz     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted         boolean         NOT NULL DEFAULT false,
    tenant_id       int8            NOT NULL DEFAULT 0
);

-- =====================================================================
-- User
-- =====================================================================

CREATE TABLE system_users (
    id              bigserial       PRIMARY KEY,
    username        varchar(30)     NOT NULL,
    password        varchar(100)    NOT NULL DEFAULT '',
    nickname        varchar(30)     NOT NULL,
    remark          varchar(500),
    dept_id         int8,
    post_ids        varchar(255),
    email           varchar(50),
    mobile          varchar(11),
    sex             int4            DEFAULT 0,
    avatar          varchar(512),
    status          int4            NOT NULL DEFAULT 0,
    login_ip        varchar(50),
    login_date      timestamptz,
    creator         int8,
    create_time     timestamptz     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updater         int8,
    update_time     timestamptz     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted         boolean         NOT NULL DEFAULT false,
    tenant_id       int8            NOT NULL DEFAULT 0
);

CREATE INDEX idx_system_users_username ON system_users (username);
CREATE INDEX idx_system_users_mobile ON system_users (mobile);
CREATE INDEX idx_system_users_email ON system_users (email);
CREATE INDEX idx_system_users_dept_id ON system_users (dept_id);

CREATE TABLE system_user_post (
    id              bigserial       PRIMARY KEY,
    user_id         int8,
    post_id         int8,
    creator         int8,
    create_time     timestamptz     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updater         int8,
    update_time     timestamptz     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted         boolean         NOT NULL DEFAULT false,
    tenant_id       int8            NOT NULL DEFAULT 0
);

-- =====================================================================
-- RBAC: Role, Menu, bindings
-- =====================================================================

CREATE TABLE system_role (
    id              bigserial       PRIMARY KEY,
    name            varchar(30)     NOT NULL,
    code            varchar(100)    NOT NULL,
    sort            int4            NOT NULL,
    status          int4            NOT NULL,
    type            int4            NOT NULL DEFAULT 1,
    remark          varchar(500),
    data_scope      int4            DEFAULT 1,
    data_scope_dept_ids varchar(500) DEFAULT '[]',
    creator         int8,
    create_time     timestamptz     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updater         int8,
    update_time     timestamptz     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted         boolean         NOT NULL DEFAULT false,
    tenant_id       int8            NOT NULL DEFAULT 0
);

CREATE TABLE system_menu (
    id              bigserial       PRIMARY KEY,
    name            varchar(50)     NOT NULL,
    permission      varchar(100)    NOT NULL DEFAULT '',
    type            int4            NOT NULL,
    sort            int4            NOT NULL DEFAULT 0,
    parent_id       int8            NOT NULL DEFAULT 0,
    path            varchar(200)    DEFAULT '',
    icon            varchar(100),
    component       varchar(255),
    component_name  varchar(255),
    status          int4            NOT NULL DEFAULT 0,
    visible         boolean         NOT NULL DEFAULT true,
    keep_alive      boolean         NOT NULL DEFAULT true,
    always_show     boolean         NOT NULL DEFAULT true,
    creator         int8,
    create_time     timestamptz     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updater         int8,
    update_time     timestamptz     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted         boolean         NOT NULL DEFAULT false
);

CREATE TABLE system_role_menu (
    id              bigserial       PRIMARY KEY,
    role_id         int8,
    menu_id         int8,
    creator         int8,
    create_time     timestamptz     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updater         int8,
    update_time     timestamptz     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted         boolean         NOT NULL DEFAULT false,
    tenant_id       int8            NOT NULL DEFAULT 0
);

CREATE INDEX idx_system_role_menu_role_id ON system_role_menu (role_id);

CREATE TABLE system_user_role (
    id              bigserial       PRIMARY KEY,
    user_id         int8,
    role_id         int8,
    creator         int8,
    create_time     timestamptz     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updater         int8,
    update_time     timestamptz     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted         boolean         NOT NULL DEFAULT false,
    tenant_id       int8            NOT NULL DEFAULT 0
);

CREATE INDEX idx_system_user_role_user_id ON system_user_role (user_id);

-- =====================================================================
-- OAuth2
-- =====================================================================

CREATE TABLE system_oauth2_client (
    id                              bigserial       PRIMARY KEY,
    client_id                       varchar(255)    NOT NULL,
    secret                          varchar(255)    NOT NULL,
    name                            varchar(255)    NOT NULL,
    logo                            varchar(255)    NOT NULL,
    description                     varchar(255),
    status                          int4            NOT NULL,
    access_token_validity_seconds   int4            NOT NULL,
    refresh_token_validity_seconds  int4            NOT NULL,
    redirect_uris                   varchar(255)    NOT NULL,
    authorized_grant_types          varchar(255)    NOT NULL,
    scopes                          varchar(255),
    auto_approve_scopes             varchar(255),
    authorities                     varchar(255),
    resource_ids                    varchar(255),
    additional_information          varchar(4096),
    creator                         int8,
    create_time                     timestamptz     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updater                         int8,
    update_time                     timestamptz     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted                         boolean         NOT NULL DEFAULT false
);

CREATE INDEX idx_system_oauth2_client_client_id ON system_oauth2_client (client_id);

CREATE TABLE system_oauth2_access_token (
    id              bigserial       PRIMARY KEY,
    access_token    varchar(255)    NOT NULL,
    refresh_token   varchar(255)    NOT NULL,
    user_id         int8            NOT NULL,
    user_type       int4            NOT NULL,
    user_info       varchar(512),
    client_id       varchar(255)    NOT NULL,
    scopes          varchar(255),
    expires_time    timestamptz     NOT NULL,
    creator         int8,
    create_time     timestamptz     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updater         int8,
    update_time     timestamptz     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted         boolean         NOT NULL DEFAULT false,
    tenant_id       int8            NOT NULL DEFAULT 0
);

CREATE INDEX idx_system_oauth2_access_token ON system_oauth2_access_token (access_token);
CREATE INDEX idx_system_oauth2_access_token_refresh ON system_oauth2_access_token (refresh_token);

CREATE TABLE system_oauth2_refresh_token (
    id              bigserial       PRIMARY KEY,
    refresh_token   varchar(255)    NOT NULL,
    user_id         int8            NOT NULL,
    user_type       int4            NOT NULL,
    client_id       varchar(255)    NOT NULL,
    scopes          varchar(255),
    expires_time    timestamptz     NOT NULL,
    creator         int8,
    create_time     timestamptz     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updater         int8,
    update_time     timestamptz     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted         boolean         NOT NULL DEFAULT false,
    tenant_id       int8            NOT NULL DEFAULT 0
);

CREATE INDEX idx_system_oauth2_refresh_token ON system_oauth2_refresh_token (refresh_token);

-- =====================================================================
-- Logging
-- =====================================================================

CREATE TABLE system_login_log (
    id              bigserial       PRIMARY KEY,
    log_type        int4            NOT NULL,
    trace_id        varchar(64),
    user_id         int8,
    user_type       int4,
    username        varchar(50),
    result          int4,
    user_ip         varchar(50),
    user_agent      varchar(512),
    creator         int8,
    create_time     timestamptz     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updater         int8,
    update_time     timestamptz     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted         boolean         NOT NULL DEFAULT false,
    tenant_id       int8            NOT NULL DEFAULT 0
);

CREATE INDEX idx_system_login_log_username ON system_login_log (username);
CREATE INDEX idx_system_login_log_create_time ON system_login_log (create_time);