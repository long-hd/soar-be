-- Payment notify task: the outbox row. One per order that reaches SUCCESS. Tenant-scoped
-- (holds no secret, but lives under the same tenant as the channel that drove the success).
CREATE TABLE pay_notify_task (
    id                 bigserial      PRIMARY KEY,
    app_id             int8           NOT NULL,
    type               int4           NOT NULL,
    data_id            int8           NOT NULL,
    merchant_order_id  varchar(64)    NOT NULL,
    notify_url         varchar(1024)  NOT NULL,
    status             int4           NOT NULL,
    next_notify_time   timestamptz    NOT NULL,
    last_execute_time  timestamptz,
    notify_times       int4           NOT NULL DEFAULT 0,
    max_notify_times   int4           NOT NULL DEFAULT 9,
    tenant_id          int8           NOT NULL DEFAULT 0,
    creator            int8,
    create_time        timestamptz    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updater            int8,
    update_time        timestamptz    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted            boolean        NOT NULL DEFAULT false
);
-- Poll query driver: WAITING tasks that are due.
CREATE INDEX idx_pay_notify_task_poll ON pay_notify_task (status, next_notify_time);
CREATE INDEX idx_pay_notify_task_data ON pay_notify_task (type, data_id);

-- Payment notify log: one row per delivery attempt. Global (audit trail, not tenant-filtered).
CREATE TABLE pay_notify_log (
    id            bigserial      PRIMARY KEY,
    task_id       int8           NOT NULL,
    notify_times  int4           NOT NULL,
    status        int4           NOT NULL,
    response      varchar(1024),
    creator       int8,
    create_time   timestamptz    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updater       int8,
    update_time   timestamptz    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted       boolean        NOT NULL DEFAULT false
);
CREATE INDEX idx_pay_notify_log_task ON pay_notify_log (task_id);