-- =====================================================================
-- V1_1_6: Create Scheduled Job Table
-- =====================================================================
-- Job CONFIGURATION (what should run, and when) — the admin-facing source of truth.
-- Quartz's own qrtz_* tables (V1_1_5) hold RUNTIME state derived from this.
--
-- Global table: no tenant_id. A job's config is system-wide; running it per-tenant
-- is a runtime concern handled by the per-tenant job aspect.
-- =====================================================================

CREATE TABLE infra_job (
    id                  bigserial       PRIMARY KEY,
    name                varchar(32)     NOT NULL,
    status              int4            NOT NULL,
    handler_name        varchar(64)     NOT NULL,
    handler_param       varchar(255),
    cron_expression     varchar(32)     NOT NULL,
    retry_count         int4            NOT NULL,
    retry_interval      int4            NOT NULL,
    monitor_timeout     int4,
    creator             int8,
    create_time         timestamptz     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updater             int8,
    update_time         timestamptz     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted             boolean         NOT NULL DEFAULT false
);

-- handler_name identifies the job everywhere: it is the Spring bean name, the Quartz
-- JobKey and the TriggerKey. Uniqueness is enforced in the service (soft-deleted rows
-- would otherwise block reuse of a name), so this index is for lookup speed only.
CREATE INDEX idx_infra_job_handler_name ON infra_job (handler_name);