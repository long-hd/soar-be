package com.hdl.soar.framework.datapermission.core.rule;

import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.Expression;

import java.util.Set;

/**
 * Data permission rule contract.
 *
 * <p>Each rule declares which tables it applies to and, for a matched table,
 * produces the JSqlParser {@link Expression} that will be injected into the
 * SQL {@code WHERE} clause (e.g. {@code dept_id IN (...) OR creator = ?}).
 *
 * <p>Implemented by the dept/self rule in a later block; the
 * {@code DataPermissionStatementInspector} invokes {@link #getExpression} for
 * every table in a parsed statement that matches {@link #getTableNames()}.
 */
public interface DataPermissionRule {
    /**
     * Table names this rule applies to.
     *
     * <p>The data-permission mechanism rewrites SQL, appending a {@code WHERE}
     * condition so that only authorized rows are returned. A rule only acts on
     * the tables returned here; all other tables are left untouched.
     *
     * @return set of table names (raw DB table names, e.g. {@code system_users})
     */
    Set<String> getTableNames();

    /**
     * Build the filter expression for a given matched table.
     *
     * @param tableName  the matched table name
     * @param tableAlias the table alias in the SQL, may be {@code null}
     * @return the JSqlParser filter {@link Expression}, or {@code null} to add no condition
     */
    Expression getExpression(String tableName, Alias tableAlias);

}
