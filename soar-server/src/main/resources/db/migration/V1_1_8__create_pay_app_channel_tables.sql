-- Payment app: a money-collecting identity in the gateway. Global (not tenant-scoped).
CREATE TABLE pay_app (
    id                  bigserial       PRIMARY KEY,
    app_key             varchar(64)     NOT NULL,
    name                varchar(64)     NOT NULL,
    status              int4            NOT NULL,
    remark              varchar(500),
    order_notify_url    varchar(1024)   NOT NULL,
    refund_notify_url   varchar(1024),
    transfer_notify_url varchar(1024),
    creator             int8,
    create_time         timestamptz     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updater             int8,
    update_time         timestamptz     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted             boolean         NOT NULL DEFAULT false
);

-- Payment channel: a real rail (VNPay, MoMo, ...) under an app. Tenant-scoped: config holds
-- per-tenant secret credentials, so a channel row belongs to a tenant.
CREATE TABLE pay_channel (
    id           bigserial       PRIMARY KEY,
    app_id       int8            NOT NULL,
    code         varchar(32)     NOT NULL,
    status       int4            NOT NULL,
    fee_rate     float8          NOT NULL,
    remark       varchar(500),
    config       text            NOT NULL,
    creator      int8,
    create_time  timestamptz     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updater      int8,
    update_time  timestamptz     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted      boolean         NOT NULL DEFAULT false,
    tenant_id    int8            NOT NULL DEFAULT 0
);