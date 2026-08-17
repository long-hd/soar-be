-- Refund: one paid order may be refunded many times (partial + repeated). Global (no tenant_id) —
-- same scoping as pay_order. Money is numeric(20,4). No column DEFAULTs (Hibernate always writes
-- every column), no DEFAULT '' — see the module migration convention.
CREATE TABLE pay_refund (
    id                    bigserial      PRIMARY KEY,
    no                    varchar(64)    NOT NULL,
    app_id                int8           NOT NULL,
    channel_id            int8           NOT NULL,
    channel_code          varchar(32),
    order_id              int8           NOT NULL,
    order_no              varchar(64),
    merchant_order_id     varchar(64)    NOT NULL,
    merchant_refund_id    varchar(64)    NOT NULL,
    notify_url            varchar(1024),
    status                int4           NOT NULL,
    pay_price             numeric(20,4)  NOT NULL,
    refund_price          numeric(20,4)  NOT NULL,
    reason                varchar(256),
    user_ip               varchar(64),
    channel_order_no      varchar(64),
    channel_refund_no     varchar(64),
    success_time          timestamptz,
    channel_error_code    varchar(64),
    channel_error_msg     varchar(256),
    channel_notify_data   text,
    creator               int8,
    create_time           timestamptz    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updater               int8,
    update_time           timestamptz    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted               boolean        NOT NULL DEFAULT false
);
-- Idempotency key for refund creation.
CREATE UNIQUE INDEX uk_pay_refund_app_merchant_refund ON pay_refund (app_id, merchant_refund_id);
-- External refund no is globally unique (channel-facing id backstop).
CREATE UNIQUE INDEX uk_pay_refund_no ON pay_refund (no);
-- Sync driver: WAITING refunds in the reconcile window.
CREATE INDEX idx_pay_refund_sync ON pay_refund (status, create_time);
-- In-flight guard + order drill-down.
CREATE INDEX idx_pay_refund_order ON pay_refund (order_id);

-- Notify task carries the merchant refund id for REFUND-type tasks (restores a column dropped in
-- slice 3). Nullable: ORDER-type tasks leave it null.
ALTER TABLE pay_notify_task ADD COLUMN merchant_refund_id varchar(64);
-- A refund app may not configure a refund callback, so notify_url can be null for REFUND-type
-- tasks. The relay no-ops a task with no url. Order tasks always have one (order_notify_url is
-- NOT NULL), so this never affects the order path.
ALTER TABLE pay_notify_task ALTER COLUMN notify_url DROP NOT NULL;