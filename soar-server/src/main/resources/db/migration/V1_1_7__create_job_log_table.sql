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