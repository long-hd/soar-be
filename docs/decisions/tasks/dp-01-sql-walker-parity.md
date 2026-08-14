# DP-01 — Data Permission SQL Walker: Soar vs MyBatis-Plus (yudao)

> Audit deliverable. Compares Soar's `DataPermissionStatementInspector` against the engine behind
> yudao's data permission: MyBatis-Plus `DataPermissionInterceptor` + `BaseMultiTableInnerInterceptor`.
>
> **Verified against**: MyBatis-Plus **3.5.16** sources jar from Maven Central — the version pinned by
> `yudao-boot-mini/yudao-dependencies/pom.xml` (`<mybatis-plus.version>3.5.16</mybatis-plus.version>`).
> Not written from memory.
>
> **Date**: 2026-08-14

---

## 1. Context

Soar ports yudao's data permission feature, but the two halves were ported differently:

| Layer | Port strategy | Result |
| ----- | ------------- | ------ |
| **Rule layer** (`DataPermission` annotation, context holder, rule factory, `DeptDataPermissionRule`, scope aggregation) | 1:1 port | Behaviourally equivalent |
| **SQL rewriting layer** | Rewritten from scratch (MyBatis → Hibernate) | **Diverges — this doc** |

The rewrite was necessary: yudao rewrites SQL through a MyBatis `InnerInterceptor`; Soar has no MyBatis,
so it hooks Hibernate's `StatementInspector`. The rule layer could be reused as-is because both sides
speak JSqlParser `Expression`, but the AST walker had to be written anew — and it is materially
shallower than MP's.

This document records exactly where, with evidence, so the divergence is a recorded decision rather
than an accident.

**Current blast radius**: only one table is registered today —
`system_users` (`dept_id` + `id`) in `SoarDataPermissionConfiguration`. Most gaps below are latent
and become real as business tables get registered.

---

## 2. Architectural difference (not a gap)

| | yudao (MP 3.5.16) | Soar |
| --- | --- | --- |
| Hook point | MyBatis `InnerInterceptor` (`beforeQuery` / `beforePrepare`) | Hibernate `StatementInspector.inspect(String)` |
| SQL seen | Only MyBatis mapped statements | **Every** SQL Hibernate emits (incl. lazy loads, collection fetches, pagination count queries) |
| Per-statement opt-out | `@InterceptorIgnore` + `mappedStatementId` | Not applicable — opt-out is `@DataPermission(enable=false)` / `DataPermissionUtils.executeIgnore` |
| Rule selection input | `mappedStatementId` passed to factory | No equivalent; factory reads the annotation context only |

Soar's coverage is **broader** (nothing escapes the inspector), which is a security positive but also
means Soar encounters more exotic SQL shapes than MP does — raising the cost of the walker gaps below.

---

## 3. Layer-by-layer comparison

| Capability | yudao (MP) | Soar | Verdict |
| ---------- | ---------- | ---- | ------- |
| `FROM tbl` | Yes | Yes | parity |
| `JOIN tbl` — table collected | Yes | Yes | parity |
| **JOIN condition placed in `ON`** (preserves outer-join semantics) | **Yes**, per join type | **No** — always WHERE | **Soar gap** |
| Subquery in `FROM` | Yes | Yes | parity |
| **Sub-join `(a JOIN b)` in FROM** (`ParenthesedFromItem`) | **Yes** (`processSubJoin`) | **No** | **Soar gap** |
| CTE / `WITH` | Yes | Yes | parity |
| `UNION` / set operations | Yes | Yes | parity |
| **Subquery in `WHERE`** (`IN`, `EXISTS`, `NOT EXISTS`, comparisons) | **Yes** (`processWhereSubSelect`) | **No** | **Soar gap** |
| **Subquery in SELECT list / function args** | **Yes** (`processSelectItem`, `processFunction`) | **No** | **Soar gap** |
| `UPDATE` — main table only | Yes | Yes | shared limitation |
| `DELETE` — main table only | Yes | Yes | shared limitation |
| `INSERT … SELECT` | Not filtered | Not filtered | shared limitation |
| Parse failure behaviour | Fail-closed (throws) | Fail-closed (throws) | parity |
| **Re-render SQL only when modified** | **No** — always `statement.toString()` | **Yes** | **Soar safer** |
| Parse-result cache hook | Yes (`JsqlParseCache`, off by default) | No | Soar gap (perf only) |
| Parse via executor w/ timeout | Yes (`JsqlParserGlobal` thread pool) | No (`CCJSqlParserUtil.parse(sql)`) | Soar gap (perf/DoS only) |

---

## 4. Soar gaps (detail + evidence)

### G1 — Subquery in `WHERE` is not walked

MP walks it, recursing through binary expressions so both `AND` and `OR` branches are covered:

```java
// BaseMultiTableInnerInterceptor#processPlainSelect (MP 3.5.16, lines 106-108)
// 处理 where 中的子查询
Expression where = plainSelect.getWhere();
processWhereSubSelect(where, whereSegment);
```

`processWhereSubSelect` (lines 171-206) handles `InExpression`, `ExistsExpression`, `NotExpression`,
`BinaryExpression` (comparisons, `AND`, `OR`) and `ParenthesedExpressionList`.

Soar's `processPlainSelect` never reads `getWhere()` for traversal — it only uses it as the left
operand when appending:

```java
// DataPermissionStatementInspector#processPlainSelect (lines 120-138)
List<Expression> conditions = new ArrayList<>();
boolean modified = collectFromItem(plainSelect.getFromItem(), rules, conditions);
if (plainSelect.getJoins() != null) { ... }
if (!conditions.isEmpty()) {
    Expression injected = and(conditions);
    Expression where = plainSelect.getWhere();
    plainSelect.setWhere(where == null ? injected : new AndExpression(parenthesize(where), injected));
}
```

**Consequence** — silent fail-open. No log, no error, just unfiltered rows:

```sql
-- filtered in yudao, NOT filtered in Soar
select * from biz_order o where o.creator in (select id from system_users)
select * from biz_order o where exists (select 1 from system_users u where u.id = o.creator)
```

### G2 — Join conditions always land in `WHERE`, never in `ON`

This is the highest-impact divergence.

MP classifies each join and pushes the condition into the join's `ON` expression, keeping only
`FROM`-level tables in `WHERE`:

```java
// BaseMultiTableInnerInterceptor#processJoins (MP 3.5.16, lines 350-356)
Collection<Expression> originOnExpressions = join.getOnExpressions();
if (originOnExpressions.size() == 1 && onTables != null) {
    List<Expression> onExpressions = new LinkedList<>();
    onExpressions.add(builderExpression(originOnExpressions.iterator().next(), onTables, whereSegment));
    join.setOnExpressions(onExpressions);
    leftTable = mainTable == null ? joinTable : mainTable;
    continue;
}
```

It distinguishes `join.isRight()`, `join.isInner()`, `join.isSimple()` (implicit comma join) and the
default (left/outer) case, choosing which tables belong to `ON` vs `WHERE` accordingly.

Soar collects every table — `FROM` and all joins — into one list and `AND`s the whole thing into
`WHERE` (see the `processPlainSelect` excerpt above; `collectFromItem` is called for
`join.getFromItem()` with no join-type inspection at lines 125-129).

**Consequence** — an outer join degenerates into an inner join:

```sql
select o.*, u.nickname
from biz_order o
left join system_users u on u.id = o.creator
-- Soar appends:  where ... and (u.dept_id in (10) or u.id = 5)
```

Orders whose `creator` is `NULL`, deleted, or outside the caller's scope **disappear from the result
set** — even though `biz_order` itself is not a registered table. The user perceives "missing orders",
not "hidden creator name".

Worse with a scopeless user: the rule returns `1 = 0`
(`DeptDataPermissionRule#alwaysFalse`, lines 163-166), which in `WHERE` empties the entire result,
left table included.

### G3 — Subquery in SELECT list / function arguments is not walked

MP handles both:

```java
// BaseMultiTableInnerInterceptor#processPlainSelect (lines 101-104)
List<SelectItem<?>> selectItems = plainSelect.getSelectItems();
if (CollectionUtils.isNotEmpty(selectItems)) {
    selectItems.forEach(selectItem -> processSelectItem(selectItem, whereSegment));
}
```

`processSelectItem` (lines 208-218) descends into scalar sub-selects and `EXISTS`; `processFunction`
(lines 227-245) recurses into nested function arguments.

Soar never calls `plainSelect.getSelectItems()`.

### G4 — Sub-join `(a JOIN b)` in FROM is not walked

MP unwraps `ParenthesedFromItem` and processes the nested join list:

```java
// BaseMultiTableInnerInterceptor#processFromItem (lines 138-141)
} else if (fromItem instanceof ParenthesedFromItem) {
    List<Table> tables = processSubJoin((ParenthesedFromItem) fromItem, whereSegment);
    mainTables.addAll(tables);
}
```

Soar's `collectFromItem` (lines 163-175) recognises only `Table` and `ParenthesedSelect`; a
`ParenthesedFromItem` falls through and returns `false`, so nothing inside is filtered.

---

## 5. Where Soar is safer than MP

### S1 — SQL is re-rendered only when actually modified

MP always round-trips through the AST, even when the handler contributed nothing:

```java
// JsqlParserSupport#processParser (MP 3.5.16, lines 96-100)
sql = statement.toString();
```

Soar returns the original string untouched unless something was injected:

```java
// DataPermissionStatementInspector#inspect (line 85)
return modified ? statement.toString() : sql;
```

Given that Soar inspects **every** Hibernate statement (§2), this matters more here than it would in
MP: any JSqlParser round-trip fidelity loss is confined to statements that genuinely touch a
registered table.

### S2 — Empty-permission predicate is `1 = 0` rather than `NULL = NULL`

yudao returns `new EqualsTo(null, null)` → renders as `WHERE NULL = NULL`
(`yudao .../DeptDataPermissionRule.java` lines 124 and 136). Soar uses an explicit `1 = 0`. Same
result set (empty), but the Soar form is unambiguous and survives AST printing without relying on
null operands.

### S3 — Null-safety fixes carried over the port

- yudao builds its NPE message with `tableAlias.getName()` (line 110) — itself an NPE when the table
  has no alias. Soar's message uses `tableName` only.
- yudao calls `deptDataPermission.getAll()` directly (unboxing, line 117); Soar uses
  `Boolean.TRUE.equals(...)`.

---

## 6. Shared limitations (both sides)

### C1 — `UPDATE` / `DELETE` filter the main table only

MP:

```java
// DataPermissionInterceptor (lines 136-152)
protected void processUpdate(Update update, int index, String sql, Object obj) {
    final Expression sqlSegment = getUpdateOrDeleteExpression(update.getTable(), update.getWhere(), (String) obj);
    ...
}
```

Soar's `processUpdate` / `processDelete` (lines 140-158) do the same with `update.getTable()` /
`delete.getTable()`.

**Soar-specific aggravation**: yudao targets MySQL, where multi-table `UPDATE`/`DELETE` is less
idiomatic. Soar targets PostgreSQL, where `UPDATE … FROM other` and `DELETE … USING other` are common.
Parity with MP here is *not* sufficient for Soar.

### C2 — `INSERT … SELECT` is not filtered

MP never reaches the parser for inserts: `DataPermissionInterceptor` does not override
`processInsert`, and its hooks fire only for queries (`beforeQuery`) and `UPDATE`/`DELETE`
(`beforePrepare`, lines 76-88). Soar rejects them earlier, in `isFilterable` (lines 199-209), which
matches only `select` / `with` / `update` / `delete`.

Acceptable on both sides: an insert reads its source rows through a SELECT that is itself inspected in
Soar's case only if issued separately — an `INSERT … SELECT` single statement is not.

---

## 7. Impact assessment at current state

| Gap | Latent today? | Trigger |
| --- | ------------- | ------- |
| G1 WHERE subquery | Mostly | A query correlating on `system_users` via `in (select …)` / `exists` |
| G2 JOIN → `ON` | Mostly | Any `LEFT JOIN system_users` in a report/list query |
| G3 SELECT-list subquery | Yes | Scalar sub-select over a registered table |
| G4 sub-join | Yes | Hibernate rarely emits `(a join b)`; hand-written native SQL might |
| C1 `UPDATE … FROM` | Yes | Bulk update touching a registered table via join |

Because only `system_users` is registered, none of these currently leak in the shipped feature set.
They become live the moment a business table (`pay_order`, future logistics tables) is added to
`SoarDataPermissionConfiguration`.

**Recommended gate**: do not register additional tables until G1 and G2 are closed, or accept them
knowingly per-table after reviewing that table's query shapes.

---

## 8. Follow-ups

Tracked in `TECH_DEBT.md` as `DP-TD1` … `DP-TD11`.

Ordered by value:

1. **G2 → `DP-TD2` (`ON` placement)** — port MP's `processJoins` join-type classification. Highest
   correctness impact, self-contained change inside `DataPermissionStatementInspector`.
2. **G1 → `DP-TD1` (WHERE subquery)** — port `processWhereSubSelect`. Straightforward recursive walk.
3. **Tests → `DP-TD3`** — the starter has no `src/test` at all, unlike
   `soar-spring-boot-starter-biz-tenant`. A string-in/string-out test over `inspect()` with a stub
   rule covers every case in §3 cheaply and would have caught G1–G4 at authoring time. Should land
   before or with the fixes.
4. **G3 / G4 → `DP-TD5`, `DP-TD6`** — lower frequency; port alongside 1–2 while the walker is open.
5. **C1 → `DP-TD4` (`UPDATE … FROM` / `DELETE … USING`)** — Postgres-specific, no MP reference to
   port; design fresh.

---

## 9. References

**Soar**

- `soar-framework/soar-spring-boot-starter-biz-data-permission/src/main/java/com/hdl/soar/framework/datapermission/core/db/DataPermissionStatementInspector.java`
- `soar-framework/soar-spring-boot-starter-biz-data-permission/src/main/java/com/hdl/soar/framework/datapermission/core/rule/dept/DeptDataPermissionRule.java`
- `soar-module-system/src/main/java/com/hdl/soar/module/system/framework/datapermission/config/SoarDataPermissionConfiguration.java`
- `soar-module-system/src/main/java/com/hdl/soar/module/system/service/permission/PermissionServiceImpl.java` (`getDeptDataPermission`)

**yudao**

- `yudao-framework/yudao-spring-boot-starter-biz-data-permission/.../core/db/DataPermissionRuleHandler.java`
- `yudao-framework/yudao-spring-boot-starter-biz-data-permission/.../core/rule/dept/DeptDataPermissionRule.java`
- `yudao-dependencies/pom.xml` — MP version pin

**MyBatis-Plus 3.5.16** (sources jar, Maven Central: `com.baomidou:mybatis-plus-jsqlparser:3.5.16:sources`)

- `com/baomidou/mybatisplus/extension/plugins/inner/DataPermissionInterceptor.java`
- `com/baomidou/mybatisplus/extension/plugins/inner/BaseMultiTableInnerInterceptor.java`
- `com/baomidou/mybatisplus/extension/parser/JsqlParserSupport.java`
- `com/baomidou/mybatisplus/extension/parser/JsqlParserGlobal.java`
