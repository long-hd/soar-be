# db/seed

Reference SQL that is **run by hand**, not by Flyway.

Flyway is configured to scan `db/migration` only, so nothing in this directory is
picked up at startup. These files exist so that environment setup that currently
lives in someone's terminal history is reviewable, re-runnable, and diffable.

## Why these rows are not migrations

A Flyway migration is the right place for data the application cannot start
without: dictionaries, menus, permission codes, the initial tenant. Scheduled
jobs are not that — which jobs run is an operational choice per environment. A
dev box that never touches payments does not need the pay jobs scheduled, and
forcing them in through a migration would make that undecidable.

They are also not created at boot from code, because a job row is editable
through the admin UI (cron, status, retry). Code that re-creates or re-asserts
rows on every start would fight the operator.

So: the rows are seeded deliberately, and this directory is the record of what a
"normal" seed looks like.

## Files

| File | What it seeds | Required by |
| --- | --- | --- |
| `infra-job.sql` | The three `infra_job` rows for the pay module | `soar-module-pay` — without them, notify relay / order sync / order expiry never run |

## How to run

```bash
psql -h localhost -U postgres -d soar -f soar-server/src/main/resources/db/seed/infra-job.sql
```

Then tell the scheduler about the new rows:

```
PUT /admin-api/infra/job/sync        # needs infra:job:update
```

**The sync call is not optional.** Inserting a row into `infra_job` does not
register anything with Quartz. `JobServiceImpl#syncJob` walks every job row,
deletes and re-adds its Quartz job and trigger, then pauses the ones marked
`STOP`. Skip it and the admin UI will show three healthy-looking jobs that never
fire — which is exactly the failure mode this directory is meant to prevent.

The same rows can be created through `POST /admin-api/infra/job/create`, which
registers with Quartz directly and needs no sync. SQL is only more convenient
when bootstrapping a fresh database.

## Conventions for new seed files

- **Idempotent.** Guard every `INSERT` with `WHERE NOT EXISTS` on a natural key,
  so re-running is a no-op rather than a duplicate.
- **No explicit ids.** Let `bigserial` assign them. An explicit id does not
  advance the sequence, and the next row created through the API then collides
  on the primary key. If a file must pin ids (menu parent references), it ends
  with `SELECT setval(...)` — same rule as the migrations.
- **No `DEFAULT ''` substitutes.** Optional columns get `NULL`, never `''` or
  `'[]'`, matching the migration rules in `CONVENTIONS.md`.
- **Say what has to happen next.** If the rows need an API call, a cache evict,
  or a restart to take effect, put it in a comment at the top of the file and in
  the table above.
- **English comments only.**
