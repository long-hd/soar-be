-- Payment order: one receivable. Global (not tenant-scoped).
CREATE TABLE pay_order (
    id                 bigserial      PRIMARY KEY,
    app_id             int8           NOT NULL,
    channel_id         int8,
    channel_code       varchar(32),
    merchant_order_id  varchar(64)    NOT NULL,
    subject            varchar(255)   NOT NULL,
    body               varchar(512),
    notify_url         varchar(1024)  NOT NULL,
    price              numeric(20,4)  NOT NULL,
    currency           varchar(3)     NOT NULL,
    channel_fee_rate   float8,
    channel_fee_price  numeric(20,4),
    status             int4           NOT NULL,
    user_ip            varchar(64),
    expire_time        timestamptz,
    success_time       timestamptz,
    extension_id       int8,
    no                 varchar(64),
    refund_price       numeric(20,4)  NOT NULL DEFAULT 0,
    channel_user_id    varchar(255),
    channel_order_no   varchar(64),
    creator            int8,
    create_time        timestamptz    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updater            int8,
    update_time        timestamptz    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted            boolean        NOT NULL DEFAULT false
);
CREATE INDEX idx_pay_order_app_merchant ON pay_order (app_id, merchant_order_id);

-- Payment order extension: one attempt to pay an order through one channel. Global.
CREATE TABLE pay_order_extension (
    id                  bigserial      PRIMARY KEY,
    no                  varchar(64)    NOT NULL,
    order_id            int8           NOT NULL,
    channel_id          int8           NOT NULL,
    channel_code        varchar(32)    NOT NULL,
    user_ip             varchar(64),
    status              int4           NOT NULL,
    channel_extras      text,
    channel_error_code  varchar(128),
    channel_error_msg   varchar(512),
    channel_notify_data text,
    creator             int8,
    create_time         timestamptz    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updater             int8,
    update_time         timestamptz    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted             boolean        NOT NULL DEFAULT false
);
CREATE UNIQUE INDEX uk_pay_order_extension_no ON pay_order_extension (no);
CREATE INDEX idx_pay_order_extension_order ON pay_order_extension (order_id);