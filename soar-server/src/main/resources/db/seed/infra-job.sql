-- Reference seed for infra_job — the three jobs the pay module needs.
--
-- NOT a Flyway migration. Flyway only scans db/migration, so this file is inert
-- until someone runs it by hand. See db/seed/README.md for why jobs are seeded
-- this way and for the required follow-up call.
--
-- Safe to re-run: each INSERT is keyed on handler_name, which is the identity of
-- a job everywhere (Spring bean name, Quartz JobKey, TriggerKey).
--
-- After running this, call PUT /admin-api/infra/job/sync so the scheduler picks
-- the rows up. Without that call the rows exist but nothing is scheduled.

-- status 1 = NORMAL (registered and running). 0 = INIT, 2 = STOP.
-- retry_count 0 = no retry. monitor_timeout 0 = no timeout alert.
-- creator/updater 1 = the seeded admin user.

INSERT INTO infra_job (name, status, handler_name, handler_param, cron_expression,
                       retry_count, retry_interval, monitor_timeout, creator, updater)
SELECT 'Pay notify relay', 1, 'payNotifyJob', NULL, '0/30 * * * * ?', 0, 0, 0, 1, 1
WHERE NOT EXISTS (SELECT 1 FROM infra_job WHERE handler_name = 'payNotifyJob');

INSERT INTO infra_job (name, status, handler_name, handler_param, cron_expression,
                       retry_count, retry_interval, monitor_timeout, creator, updater)
SELECT 'Pay order sync', 1, 'payOrderSyncJob', NULL, '0 * * * * ?', 0, 0, 0, 1, 1
WHERE NOT EXISTS (SELECT 1 FROM infra_job WHERE handler_name = 'payOrderSyncJob');

INSERT INTO infra_job (name, status, handler_name, handler_param, cron_expression,
                       retry_count, retry_interval, monitor_timeout, creator, updater)
SELECT 'Pay order expire', 1, 'payOrderExpireJob', NULL, '0 0/5 * * * ?', 0, 0, 0, 1, 1
WHERE NOT EXISTS (SELECT 1 FROM infra_job WHERE handler_name = 'payOrderExpireJob');

-- Repair step for databases seeded with explicit ids (1, 2, 3) before this file
-- existed: an explicit id does not advance the bigserial sequence, so the next
-- job created through the API would collide on the primary key. Harmless to run
-- when the sequence is already ahead.
SELECT setval('infra_job_id_seq', COALESCE((SELECT MAX(id) FROM infra_job), 1));
