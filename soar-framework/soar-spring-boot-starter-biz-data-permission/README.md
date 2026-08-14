# soar-spring-boot-starter-biz-data-permission

Row-level data filtering driven by the logged-in user's role data scope.

Ported from yudao's `yudao-spring-boot-starter-biz-data-permission`. The **rule layer** is a 1:1 port;
the **SQL rewriting layer** was rewritten because Soar has no MyBatis — see [Design notes](#design-notes).

---

## Overview

The starter injects a `WHERE` condition into SQL that Hibernate emits, so a caller only sees rows their
role permits.

```sql
-- before
select ... from system_users u1_0 where u1_0.deleted = false

-- after, for a user whose role scope is DEPT_ONLY (dept 10) and who may also see their own row
select ... from system_users u1_0
where (u1_0.deleted = false)
  and (u1_0.dept_id in (10) or u1_0.id = 5)
```

Two independent things must both be true for a table to be filtered:

1. **Data permission is enabled** for the current call — it is **on by default**; `@DataPermission(enable = false)` turns it off.
2. **The table is registered** with a column mapping via a `DeptDataPermissionRuleCustomizer`.

An unregistered table is never touched, regardless of scope.

---



## Request flow

```
Service / Repository call
        │
Hibernate renders SQL
        │
DataPermissionStatementInspector.inspect(sql)
        ├── not SELECT/WITH/UPDATE/DELETE ──────────► return sql unchanged
        ├── no LoginUser, or skipPermissionCheck() ─► return sql unchanged
        ├── DataPermissionRuleFactory.getDataPermissionRule()
        │        └── reads the active @DataPermission from DataPermissionContextHolder
        │            (none = all rules; enable=false = no rules; include/exclude = subset)
        ├── no rules ───────────────────────────────► return sql unchanged
        │
        ├── parse with JSqlParser
        ├── walk FROM / JOIN / subquery-in-FROM / CTE / UNION
        │        └── for each table in rule.getTableNames(): rule.getExpression(table, alias)
        └── AND the expressions into WHERE, re-render only if modified
```

---



## Scope resolution

`DeptDataPermissionRule` asks `PermissionCommonApi.getDeptDataPermission(userId)`, which aggregates
**all** of the user's roles into a `DeptDataPermissionRespDTO` (`all`, `deptIds`, `self`):


| `DataScopeEnum` (role) | Effect on the DTO                                        |
| ---------------------- | -------------------------------------------------------- |
| `ALL(1)`               | `all = true`                                             |
| `DEPT_CUSTOM(2)`       | `deptIds += role.dataScopeDeptIds` + the user's own dept |
| `DEPT_ONLY(3)`         | `deptIds += user's own dept`                             |
| `DEPT_AND_CHILD(4)`    | `deptIds += own dept + descendants`                      |
| `SELF(5)`              | `self = true`                                            |


There is no "dept and self" scope. The `OR` form appears when a user holds several roles whose scopes
aggregate into both `deptIds` and `self`.

The rule then builds:


| Resolved DTO   | Injected expression                             |
| -------------- | ----------------------------------------------- |
| `all = true`   | *(none — no filtering)*                         |
| `deptIds` only | `dept_col IN (…)`                               |
| `self` only    | `user_col = <loginUserId>`                      |
| both           | `(dept_col IN (…) OR user_col = <loginUserId>)` |
| neither        | `1 = 0` — guaranteed empty result               |


The resolved DTO is memoised on `LoginUser` for the request, so the lookup runs once per request.

Only `UserTypeEnum.ADMIN` users are filtered; other user types return no expression.

---



## Registering a table

Declare a `DeptDataPermissionRuleCustomizer` bean in the module that owns the table. Its presence is
what activates `SoarDeptDataPermissionAutoConfiguration`.

```java
@Configuration(proxyBeanMethods = false)
public class SoarDataPermissionConfiguration {

    @Bean
    public DeptDataPermissionRuleCustomizer systemDeptDataPermissionRuleCustomizer() {
        return rule -> {
            rule.addDeptColumn("system_users", "dept_id"); // → dept_id IN (...)
            rule.addUserColumn("system_users", "id");      // → id = loginUserId   (SELF)
        };
    }
}
```

- `addDeptColumn(table, column)` — the "which department owns this row" column. `addDeptColumn(table)` defaults the column to `dept_id`.
- `addUserColumn(table, column)` — the "whose row is this" column used by `SELF`. On `system_users` that is the PK `id`; on business tables it is usually `creator` or `user_id`.
- Either may be registered alone. Both add the table to `getTableNames()`.

**Table names are raw DB names, matched exactly and case-sensitively** after stripping `"` quotes. They
must match `@Table(name = ...)`. A typo fails silently — no startup error, no filtering.

Multiple modules may each contribute a customizer; all of them mutate the same rule instance.

**Currently registered**: `system_users` only.

---



## Opting out



### Whole method or class

```java
@DataPermission(enable = false) // login lookups must not be filtered by dept
public UserPO getUserByUsername(String username) { ... }
```

Applied by Spring AOP (`DataPermissionAnnotationAdvisor`), so it only takes effect through a proxy —
a self-call `this.foo()` inside the same bean does **not** trigger it.

Current uses: `PermissionServiceImpl.getDeptDataPermission` (prevents recursion), `AdminAuthServiceImpl`,
`AuthController`, `DeptServiceImpl` (prevents poisoning the dept cache).

### A block of code

```java
UserPO creator = DataPermissionUtils.executeIgnore(
        () -> userRepository.findById(order.getCreator()).orElse(null));
```

Works without AOP and survives self-calls. Pushes a synthetic `enable = false` annotation onto the
context stack and pops it in a `finally`.

### A subset of rules

```java
@DataPermission(includeRules = {DeptDataPermissionRule.class})  // only this rule
@DataPermission(excludeRules = {DeptDataPermissionRule.class})  // everything but this rule
```

`includeRules` outranks `excludeRules`. With only one rule class registered today, both are effectively
no-ops — they exist for when a second `DataPermissionRule` implementation lands.

---



## Package layout

```
core/
├── annotation/DataPermission.java            # enable / includeRules / excludeRules
├── aop/
│   ├── DataPermissionContextHolder.java      # TransmittableThreadLocal stack of active annotations
│   ├── DataPermissionAnnotationInterceptor.java
│   └── DataPermissionAnnotationAdvisor.java
├── rule/
│   ├── DataPermissionRule.java               # getTableNames() + getExpression(table, alias)
│   ├── DataPermissionRuleFactory.java
│   ├── DataPermissionRuleFactoryImpl.java    # applies the annotation semantics
│   └── dept/
│       ├── DeptDataPermissionRule.java
│       └── DeptDataPermissionRuleCustomizer.java
├── db/DataPermissionStatementInspector.java  # the SQL rewriter
└── util/DataPermissionUtils.java             # executeIgnore
config/
├── SoarDataPermissionAutoConfiguration.java      # factory + inspector + advisor
└── SoarDeptDataPermissionAutoConfiguration.java  # @ConditionalOnBean(DeptDataPermissionRuleCustomizer)
```

---



## Gotchas


| Symptom                                        | Cause                                                                                                                                |
| ---------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------ |
| Rows missing from a `LEFT JOIN` result         | The condition is appended to `WHERE`, not the join's `ON`, so an outer join collapses to an inner one. See `DP-TD2`.                 |
| Whole result set empty                         | The caller resolved to no dept and no self → `1 = 0` injected. Check the role's `data_scope`.                                        |
| Filtering silently absent                      | Table not registered, or table name typo/case mismatch, or `@DataPermission(enable=false)` in scope, or no `LoginUser` (jobs, boot). |
| `@DataPermission` ignored                      | Self-call inside the same bean bypasses the proxy — use `DataPermissionUtils.executeIgnore`.                                         |
| HTTP 500 "Data permission failed to parse SQL" | JSqlParser could not parse the statement. Fail-closed by design. Wrap the call in `executeIgnore` or annotate the method.            |
| Subquery not filtered                          | The walker does not descend into `WHERE` / SELECT-list subqueries. See `DP-TD1`, `DP-TD5`.                                           |


---



## Design notes

Soar hooks Hibernate's `StatementInspector`; yudao hooks MyBatis-Plus's `DataPermissionInterceptor`.
The rule layer is shared in spirit — both produce JSqlParser `Expression` objects — but the AST walker
is Soar's own and is **shallower** than MyBatis-Plus's.

Deliberate choices:

- **Literal values, not bind parameters.** Keeps Hibernate's positional `?` placeholders intact. Cost: plan-cache fragmentation per scope (`DP-TD8`).
- **Fail-closed on parse errors.** An unparseable statement throws rather than running unfiltered (`DP-TD10`).
- **Re-render only when modified.** Statements that touch no registered table are returned byte-for-byte, avoiding round-trip fidelity risk.
- `1 = 0` instead of yudao's `NULL = NULL` for the no-permission case.

Known divergences from yudao, with evidence and severity, are documented in
`[docs/decisions/tasks/dp-01-sql-walker-parity.md](../../docs/decisions/tasks/dp-01-sql-walker-parity.md)`
and tracked as `DP-TD1`…`DP-TD11` in `[TECH_DEBT.md](../../TECH_DEBT.md)`.

There are currently **no tests** in this starter (`DP-TD3`).

---



## Related

- `soar-spring-boot-starter-security` — supplies `LoginUser`, `SecurityFrameworkUtils`, `skipPermissionCheck()`
- `soar-spring-boot-starter-biz-tenant` — separate concern; tenant filtering is *not* affected by `@DataPermission`
- `soar-module-system` — `PermissionServiceImpl.getDeptDataPermission`, `DataScopeEnum`, table registration

External walkthrough (video): [https://drive.google.com/file/d/1Y0e4lUACXKHn5nWhFrRUp6U5Hm1mcAl_/view?usp=drive_link](https://drive.google.com/file/d/1Y0e4lUACXKHn5nWhFrRUp6U5Hm1mcAl_/view?usp=drive_link)