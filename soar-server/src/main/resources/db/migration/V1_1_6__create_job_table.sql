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

-- ---------------------------------------------------------------------
-- Job execution log
-- ---------------------------------------------------------------------
-- One row per execution attempt (a retry produces another row, execute_index > 1).
-- Global table: no tenant_id. handler_name/handler_param are denormalized snapshots,
-- kept so a log stays readable even after its job is edited or deleted.

CREATE TABLE infra_job_log (
    id                  bigserial       PRIMARY KEY,
    job_id              int8            NOT NULL,
    handler_name        varchar(64)     NOT NULL,
    handler_param       varchar(255),
    execute_index       int4            NOT NULL DEFAULT 1,
    begin_time          timestamptz     NOT NULL,
    end_time            timestamptz,
    duration            int4,
    status              int4            NOT NULL,
    result              varchar(4000),
    creator             int8,
    create_time         timestamptz     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updater             int8,
    update_time         timestamptz     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted             boolean         NOT NULL DEFAULT false
);

-- The clean-up job deletes by age → index create_time for the range scan.
CREATE INDEX idx_infra_job_log_create_time ON infra_job_log (create_time);
CREATE INDEX idx_infra_job_log_job_id ON infra_job_log (job_id);