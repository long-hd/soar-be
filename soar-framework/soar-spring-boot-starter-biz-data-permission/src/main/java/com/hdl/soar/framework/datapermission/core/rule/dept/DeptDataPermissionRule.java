package com.hdl.soar.framework.datapermission.core.rule.dept;

import com.hdl.soar.framework.common.biz.system.permission.PermissionCommonApi;
import com.hdl.soar.framework.common.biz.system.permission.dto.DeptDataPermissionRespDTO;
import com.hdl.soar.framework.common.enums.UserTypeEnum;
import com.hdl.soar.framework.datapermission.core.rule.DataPermissionRule;
import com.hdl.soar.framework.security.core.LoginUser;
import com.hdl.soar.framework.security.core.util.SecurityFrameworkUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.expression.operators.relational.ParenthesedExpressionList;
import net.sf.jsqlparser.schema.Column;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Department-based {@link DataPermissionRule}.
 *
 * <p>Requires the target tables to carry a department column (commonly {@code dept_id})
 * and/or a self/user column (e.g. {@code creator}, or {@code id} for {@code system_users}).
 * Configured per table via {@link #addDeptColumn} / {@link #addUserColumn}, typically from a
 * {@link DeptDataPermissionRuleCustomizer}.
 *
 * <p>Final condition shape: {@code (dept_id IN (...) OR <self_column> = <loginUserId>)}.
 */
@Slf4j
@RequiredArgsConstructor
public class DeptDataPermissionRule implements DataPermissionRule {

    /**
     * LoginUser context cache key — memoizes the per-request resolved permission.
     */
    protected static final String CONTEXT_KEY = DeptDataPermissionRule.class.getSimpleName();

    private static final String DEPT_COLUMN_NAME = "dept_id";

    private final PermissionCommonApi permissionApi;

    /** table name -> department column name */
    private final Map<String, String> deptColumns = new HashMap<>();
    /** table name -> self/user column name */
    private final Map<String, String> userColumns = new HashMap<>();
    /** union of all configured table names */
    private final Set<String> tableNames = new HashSet<>();

    @Override
    public Set<String> getTableNames() {
        return tableNames;
    }

    @Override
    public Expression getExpression(String tableName, Alias tableAlias) {
        // only process when there is a logged-in user
        LoginUser loginUser = SecurityFrameworkUtils.getLoginUser();
        if (loginUser == null) {
            return null;
        }
        // only process ADMIN-type users
        if (!UserTypeEnum.ADMIN.getValue().equals(loginUser.getUserType())) {
            return null;
        }

        // resolve the permission, memoizing on the LoginUser context
        DeptDataPermissionRespDTO permission = loginUser.getContext(CONTEXT_KEY, DeptDataPermissionRespDTO.class);
        if (permission == null) {
            permission = permissionApi.getDeptDataPermission(loginUser.getId());
            if (permission == null) {
                log.error("[getExpression] LoginUser({}) returned null data permission", loginUser.getId());
                throw new NullPointerException(String.format(
                        "LoginUser(%d) Table(%s) returned no data permission", loginUser.getId(), tableName));
            }
            loginUser.setContext(CONTEXT_KEY, permission);
        }

        // case 1: ALL → no condition
        if (Boolean.TRUE.equals(permission.getAll())) {
            return null;
        }
        // case 2: neither dept nor self → 100% no permission → empty result
        if (isEmpty(permission.getDeptIds()) && Boolean.FALSE.equals(permission.getSelf())) {
            return alwaysFalse();
        }
        // case 3: combine dept + self
        Expression deptExpression = buildDeptExpression(tableName, tableAlias, permission.getDeptIds());
        Expression userExpression = buildUserExpression(tableName, tableAlias, permission.getSelf(), loginUser.getId());
        if (deptExpression == null && userExpression == null) {
            // table registered but no usable column for this user → return nothing rather than everything
            log.warn("[getExpression] LoginUser({}) Table({}) built an empty condition", loginUser.getId(), tableName);
            return alwaysFalse();
        }
        if (deptExpression == null) {
            return userExpression;
        }
        if (userExpression == null) {
            return deptExpression;
        }
        // WHERE (dept_id IN (...) OR self_col = ?)
        return new ParenthesedExpressionList<>(new OrExpression(deptExpression, userExpression));
    }

    private Expression buildDeptExpression(String tableName, Alias tableAlias, Set<Long> deptIds) {
        String columnName = deptColumns.get(tableName);
        if (isEmpty(columnName) || isEmpty(deptIds)) {
            return null;
        }
        // dept_col IN (id1, id2, ...)
        ParenthesedExpressionList<LongValue> inValues = new ParenthesedExpressionList<>();
        for (Long deptId : deptIds) {
            inValues.add(new LongValue(deptId));
        }
        return new InExpression(buildColumn(tableName, tableAlias, columnName), inValues);
    }

    private Expression buildUserExpression(String tableName, Alias tableAlias, Boolean self, Long userId) {
        if (Boolean.FALSE.equals(self)) {
            return null;
        }
        String columnName = userColumns.get(tableName);
        if (isEmpty(columnName)) {
            return null;
        }
        // self_col = userId
        return new EqualsTo(buildColumn(tableName, tableAlias, columnName), new LongValue(userId));
    }

    // ==================== configuration ====================

    /** Register the dept column for a table, defaulting the column to {@code dept_id}. */
    public void addDeptColumn(String tableName) {
        addDeptColumn(tableName, DEPT_COLUMN_NAME);
    }

    public void addDeptColumn(String tableName, String columnName) {
        deptColumns.put(tableName, columnName);
        tableNames.add(tableName);
    }

    public void addUserColumn(String tableName, String columnName) {
        userColumns.put(tableName, columnName);
        tableNames.add(tableName);
    }

    // ==================== helpers ====================

    /**
     * Build a JSqlParser column qualified by the table alias when present, else the table name.
     * Replaces old {@code MyBatisUtils.buildColumn}.
     */
    private static Column buildColumn(String tableName, Alias tableAlias, String columnName) {
        String prefix = tableAlias != null ? tableAlias.getName() : tableName;
        return new Column(prefix + "." + columnName);
    }

    /** {@code 1 = 0} — an always-false predicate guaranteeing an empty result set. */
    private static Expression alwaysFalse() {
        return new EqualsTo(new LongValue(1L), new LongValue(0L));
    }

    private static boolean isEmpty(String s) {
        return s == null || s.isEmpty();
    }

    private static boolean isEmpty(Set<?> set) {
        return set == null || set.isEmpty();
    }

}
