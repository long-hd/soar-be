# Tech Debt Tracker — Soar Backend

> Single source of truth for known shortcuts, deferred work, and quality gaps in `soar-be`.
>
> Mirrors the tracker convention already used by `soar-fe/TECH_DEBT.md`.

**Created**: 2026-08-14 — seeded from the data-permission walker audit (`docs/decisions/tasks/dp-01-sql-walker-parity.md`), extended the same day with the `soar-module-pay` review.

**Last updated**: 2026-08-14

---

## Schema

| Field        | Values                                                                                                                                                  |
| ------------ | ------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **ID**       | `<Area>-TD<N>` — area prefix groups related items (e.g. `DP-TD1` for data permission)                                                                   |
| **Severity** | `critical` (blocks current work) / `high` (correctness or security risk once triggered) / `medium` (visible quality gap, fix when convenient) / `low` (defer-by-design, or "if observed") |
| **Status**   | `open` / `in-progress` / `resolved` / `wontfix`                                                                                                          |
| **Opened**   | Date, or phase/block once BE phase docs exist                                                                                                           |
| **Target**   | Condition or milestone for resolution. Empty = no target ("if observed")                                                                                |

Maintenance: append new debt when a block lands; move to § Resolved with a date when closed. Block
deliverables under `docs/decisions/tasks/` may restate items inline for reviewer context — this file
stays canonical.

---

## Open

### High severity

| ID       | Title                                                       | Opened     | Target                                        | Notes |
| -------- | ----------------------------------------------------------- | ---------- | --------------------------------------------- | ----- |
| `DP-TD1` | Data permission: subquery in `WHERE` is not filtered        | 2026-08-14 | Before registering any table beyond `system_users` | `DataPermissionStatementInspector.processPlainSelect` never traverses `getWhere()`, so `where x in (select … from <registered table>)` and `exists (select …)` run unfiltered — silent fail-open, no log. MyBatis-Plus (yudao) covers this via `processWhereSubSelect`. Latent today because only `system_users` is registered. See DP-01 §4/G1. |
| `DP-TD2` | Data permission: join condition injected into `WHERE`, not `ON` | 2026-08-14 | Before registering any table beyond `system_users` | Every table (FROM + all joins) is `AND`ed into `WHERE`, so `LEFT JOIN <registered table>` degenerates into an inner join and drops rows of the **left** table. With a scopeless user the injected `1 = 0` empties the whole result. MP classifies join type and writes into `join.setOnExpressions(...)`. See DP-01 §4/G2. |
| `PAY-TD1` | The amount a channel reports back cannot be checked — the DTO has no field for it | 2026-08-14 | Before any real rail goes live | `PayOrderChannelRespDTO` carries status, trade numbers, times and raw data but **no price**, so the paid amount never crosses the client boundary and the service has nothing to compare against `pay_order.price`. `VnpayPayClient#doParseOrderNotify` reads `vnp_TxnRef` / `vnp_TransactionNo` / `vnp_ResponseCode` / `vnp_TransactionStatus` / `vnp_PayDate` and drops `vnp_Amount`. A correctly signed callback for a smaller amount settles the order in full. Fix spans three layers: add the field, populate it in each client, assert it in `notifyOrderSuccess`. |
| `PAY-TD2` | `PayTestController` ships in the module with five `@PermitAll` endpoints | 2026-08-14 | Before the pay module is exposed in any deployed environment | `controller/admin/test/PayTestController` exposes `create-order`, `get-order`, `notify-sink`, `notify-sink/toggle` and `sync-order`, all `@PermitAll`. The class javadoc says "TEST ONLY — remove before production" but nothing enforces it: no profile guard, no conditional bean. Unauthenticated order creation in a deployed environment. |
| `PAY-TD3` | Mock rail has no environment guard | 2026-08-14 | Same as PAY-TD2 | `MockPayClient` reports SUCCESS for every order and every query. `PayChannelEnum.MOCK("mock", …)` makes it a first-class rail selected purely by `pay_channel.code`, so one row inserted in production turns real payments into free ones. There is no `@Profile` or `@ConditionalOnProperty` anywhere in `soar-module-pay` — unlike `soar.security.mock-enable`, nothing ties the mock rail to an environment. |

### Medium severity

| ID       | Title                                                              | Opened     | Target                                  | Notes |
| -------- | ------------------------------------------------------------------ | ---------- | --------------------------------------- | ----- |
| `DP-TD3` | Data permission starter has no tests                               | 2026-08-14 | With the DP-TD1 / DP-TD2 fix            | `soar-spring-boot-starter-biz-data-permission` has no `src/test` at all, unlike `soar-spring-boot-starter-biz-tenant`. A string-in/string-out suite over `inspect()` with a stub rule would have caught DP-TD1/2/5/6 at authoring time. Should land before or with the walker fixes. |
| `DP-TD4` | `UPDATE … FROM` / `DELETE … USING` filter the main table only      | 2026-08-14 | When a bulk update/delete joins a registered table | `processUpdate` / `processDelete` use `update.getTable()` / `delete.getTable()` and ignore joined tables. MP has the same limitation, but MP targets MySQL — on PostgreSQL these forms are idiomatic, so parity with MP is not sufficient here. No upstream reference to port; needs fresh design. |
| `DP-TD5` | Subquery in SELECT list / function arguments is not filtered       | 2026-08-14 | With the DP-TD1 fix                     | `plainSelect.getSelectItems()` is never read. MP covers via `processSelectItem` + `processFunction`. Lower frequency than DP-TD1 but same class of leak. |
| `PAY-TD4` | No menu / permission seed for `pay:*`                              | 2026-08-14 | When the pay admin UI is built           | Controllers declare `@PreAuthorize("@ss.hasPermission('pay:...')")`, but no migration seeds the matching MENU/BUTTON rows. Only `super_admin` (granted everything in code) can reach the pay admin API; no role can be given partial access. Violates the "new entity → menu/permission seed" checklist item. |
| `PAY-TD5` | Tenancy is asymmetric across pay tables                            | 2026-08-14 | Before granting any `pay:*` permission to a non-super-admin | `PayChannelPO` and `PayNotifyTaskPO` extend `TenantBasePO`; `PayOrderPO`, `PayOrderExtensionPO`, `PayAppPO` and `PayNotifyLogPO` extend `BasePO` (global). No pay table is registered with `DeptDataPermissionRuleCustomizer`, and `getOrderPage` does not restrict by the caller's apps. Latent only because PAY-TD4 keeps everyone but super-admin out; fixing PAY-TD4 without this one leaks orders across tenants. |
| `PAY-TD6` | `(app_id, merchant_order_id)` is indexed but not unique            | 2026-08-14 | Before any real rail goes live            | `V1_1_9__create_pay_order_tables.sql:30` creates `idx_pay_order_app_merchant` as a plain index. Duplicate detection is a SELECT-then-INSERT in the service, so two concurrent requests carrying the same merchant order number can both create an order. The DB should be the arbiter. |
| `PAY-TD7` | Order and extension status diverge on the expire / closed paths    | 2026-08-14 | With the next pay state-machine change    | `expireOrder0` re-checks each attempt against the channel, then calls `closeExpiredOrder`, which CAS-closes `pay_order` only — the `pay_order_extension` rows stay WAITING. In the other direction `notifyOrderClosed` CAS-closes the extension and never touches the parent order. Neither is wrong alone, but together the two tables end up disagreeing about the same payment attempt. |
| `PAY-TD8` | `PayClientFactory` does not react to a channel's `code` changing or being deleted | 2026-08-14 | If a channel is ever re-pointed at another rail | `createOrUpdatePayClient` only calls `createPayClient` when the id is absent from the map; otherwise it calls `client.refresh(config)` and ignores `channelCode` entirely, so switching a row's `code` (`mock` → `vnpay`) keeps the old client type. The interface has no removal method at all, and `deleteChannel` only deletes the row, so a deleted channel's client stays live in the map. Both need a process restart — dangerous in the `mock` → real direction. |
| `PAY-TD9` | `application-prod.yaml` does not override `soar.pay.order-notify-url` | 2026-08-14 | Before the first production deploy of pay | The base `application.yaml:151` sets it to `http://localhost:48080/...` and marks it dev-only in a comment; the prod profile overrides `admin-ui.url`, `mock-enable`, tenant and captcha, but not this. Harmless for VNPay (its IPN URL is configured in the merchant portal, not sent per request) and broken for any rail that does send a callback URL. |

### Low severity

| ID        | Title                                                                 | Opened     | Target                                    | Notes |
| --------- | --------------------------------------------------------------------- | ---------- | ----------------------------------------- | ----- |
| `DP-TD6`  | Sub-join `(a JOIN b)` in FROM is not walked                           | 2026-08-14 | With the walker rework                    | `collectFromItem` handles `Table` and `ParenthesedSelect` only; `ParenthesedFromItem` falls through. Hibernate rarely emits this shape — hand-written native SQL might. MP covers via `processSubJoin`. |
| `DP-TD7`  | No parse cache and no parse timeout                                   | 2026-08-14 | If SQL-heavy endpoints show parser cost   | `CCJSqlParserUtil.parse(sql)` runs on every statement prepare. MP exposes a `JsqlParseCache` hook (off by default) and parses via an executor with timeout (`JsqlParserGlobal`). Mitigated in practice by the `modified`-only re-render and the early guards (no login user → skip). |
| `DP-TD8`  | Injected values are literals → plan-cache fragmentation               | 2026-08-14 | —                                         | **By design**: literals avoid shifting the positional `?` placeholders Hibernate binds. Cost is that `dept_id in (10)` vs `in (11,12)` are distinct SQL strings, so prepared-statement and plan caches fragment per caller scope. Revisit only if profiling shows it. |
| `DP-TD9`  | Table-name match is exact and case-sensitive; CTE name collision risk | 2026-08-14 | If a native query or CTE trips it         | `rule.getTableNames().contains(tableName)` after stripping `"` quotes. A native query writing `SYSTEM_USERS` silently skips filtering; a CTE named after a registered table would get a `dept_id` predicate it may not have, producing a runtime SQL error. |
| `DP-TD10` | Unparseable SQL fails the request (HTTP 500)                          | 2026-08-14 | If a native query in a logged-in path trips it | **By design** (fail-closed — documented in the inspector). Triggers only when a login user exists *and* rules are non-empty. Today's native queries are safe: the `jsonb_exists` tenant lookup runs pre-auth, and the `DELETE … LIMIT` cleanups run in jobs. Mitigation for future cases: `@DataPermission(enable = false)` or `DataPermissionUtils.executeIgnore(...)`. |
| `DP-TD11` | `INSERT … SELECT` is not filtered                                     | 2026-08-14 | —                                         | `isFilterable` matches `select` / `with` / `update` / `delete` only. yudao/MP behave the same (no `processInsert` override, hooks fire on query + update/delete). Accepted as parity. |
| `API-TD1` | `pageSize = -1` escape hatch is unreachable through validation        | 2026-08-14 | When the first export endpoint needs it   | `PageParam.PAGE_SIZE_NONE = -1` is documented as "no pagination", but `pageSize` carries `@Min(1)`, so any DTO validated as a normal `PageParam` rejects it. No endpoint relies on it today. Decide between dropping the constant and adding a validation group. See `docs/api-contract.md § 13`. |
| `PAY-TD10` | Signature comparison uses `equalsIgnoreCase`; raw channel responses logged at INFO | 2026-08-14 | With the PAY-TD1 fix | `VnpayPayClient#doParseOrderNotify` compares the received and expected HMAC with `equalsIgnoreCase`, which is not constant-time — theoretical against an HMAC the attacker cannot iterate quickly, but free to fix with `MessageDigest.isEqual` on the decoded bytes. Separately `doGetOrder` logs the raw querydr response at INFO, putting channel parameters into ordinary application logs. |
| `JOB-TD1` | Job rows seeded with explicit ids leave `infra_job_id_seq` behind | 2026-08-14 | Next time an environment is seeded | The three pay jobs were originally inserted by hand with ids 1–3. An explicit id does not advance a `bigserial` sequence, so the next job created through `POST /infra/job/create` collides on the primary key. `db/seed/infra-job.sql` now inserts without ids and ends with a `setval` repair; existing databases need that one statement run once. |
| `PORT-TD1` | `GlobalExceptionHandler.handleTableNotExists` branches on yudao modules that do not exist | 2026-08-14 | Next time the handler is touched | Nine branches match table prefixes (`report_`, `bpm_`, `mp_`, `product_`, `erp_`, `crm_`, `pay_`, `ai_`, `iot_`) and return messages linking to `iocoder.cn`. Only `pay_` corresponds to a real Soar module. Dead port artifact — harmless (the whole method only fires on "doesn't exist", which is a MySQL message, not PostgreSQL) but misleading. |

---

## Resolved

Chronological, newest first.

| ID | Title | Opened | Resolved | Resolution |
| -- | ----- | ------ | -------- | ---------- |
| `PAY-TD11` | The pay module's scheduled jobs had no reproducible seed | 2026-08-14 | 2026-08-14 | `payNotifyJob`, `payOrderSyncJob` and `payOrderExpireJob` were seeded ad hoc, so a fresh database silently ran the pay module with no notify relay, no order sync and no expiry. Added `soar-server/src/main/resources/db/seed/infra-job.sql` (idempotent, no explicit ids) and `db/seed/README.md` explaining why job rows are not Flyway migrations and that `PUT /admin-api/infra/job/sync` must follow the insert. Not automated on purpose: which jobs run is a per-environment choice, and the rows are editable from the admin UI. |
| `PORT-TD2` | Non-English comments in ported code and config | 2026-08-14 | 2026-08-14 | Translated 35 comment lines across 10 files: `GlobalExceptionHandler` (3 Chinese comments + 1 Chinese assertion message), `PayTestController`, `RoleServiceImpl`, `FileConfigServiceImpl`, `application.yaml`, `application-prod.yaml`, `Dockerfile`, `pom.xml`, `soar-dependencies/pom.xml`, `soar-spring-boot-starter-websocket/pom.xml`, `V1_0_9__reseed_system_menu.sql`. Comment text only — no code or config values changed. One Chinese string remains **intentionally** in `docs/decisions/tasks/dp-01-sql-walker-parity.md` as a verbatim citation of MyBatis-Plus source, now with an English gloss. |
| `DOC-TD1` | `AGENTS.md § Deep Context` pointed at four non-existent documents | 2026-08-14 | 2026-08-14 | Replaced the bullet list with a question→file table covering the documents that actually exist, plus a documentation protocol and cross-repo coordination rules. Dead links in `soar-fe` (`../soar-be/docs/phase-plan.md`, `../soar-be/docs/architecture-decisions.md`) repointed at the same time. |

---

## Follow-ups outside this tracker

- **BE items currently tracked in `soar-fe/TECH_DEBT.md`** — that file carries a `#### BE` section
  (file-client domain fallback, presigned-URL production posture) plus several resolved `BE-TD-*`
  rows. They predate this tracker and should be migrated here when next touched, rather than copied
  blind. Not restated above to avoid duplicating stale status.
- **Refund and transfer are unimplemented in `soar-module-pay`, by decision.** `PayRefundPO`, the refund
  notify type and the transfer client methods exist as scaffolding. This is deferred scope, not a
  shortcut, so it is not tracked as debt — it becomes debt only if something starts depending on it.
- **No yudao reference map.** `AGENTS.md` used to promise `docs/RUOYI_REFERENCE_MAP.md`; it never
  existed, and agents now compare against the `yudao-boot-mini` checkout directly. Worth writing only if
  the ad-hoc comparison proves repeatedly costly.
