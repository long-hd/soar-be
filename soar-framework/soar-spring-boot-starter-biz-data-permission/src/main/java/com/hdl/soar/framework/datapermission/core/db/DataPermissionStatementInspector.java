package com.hdl.soar.framework.datapermission.core.db;

import com.hdl.soar.framework.datapermission.core.rule.DataPermissionRule;
import com.hdl.soar.framework.datapermission.core.rule.DataPermissionRuleFactory;
import com.hdl.soar.framework.security.core.util.SecurityFrameworkUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.ParenthesedExpressionList;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.select.*;
import net.sf.jsqlparser.statement.update.Update;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.resource.jdbc.spi.StatementInspector;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Hibernate {@link StatementInspector} that injects data-permission filters into SELECT / UPDATE / DELETE SQL.
 *
 * <p>This is Soar's equivalent of MyBatis-Plus's {@code DataPermissionInterceptor}: it parses
 * each emitted SQL with JSqlParser, finds tables registered by a {@link DataPermissionRule},
 * and appends a {@code WHERE} condition (e.g. {@code dept_id IN (...) OR creator = ?}).
 *
 * <p>The injected condition uses literal values (not bind parameters), so it never shifts the
 * positional {@code ?} placeholders that Hibernate binds.
 *
 * <p>Self-registers via {@link HibernatePropertiesCustomizer}. The rule factory is resolved
 * lazily through {@link ObjectProvider} to avoid a startup dependency cycle with the
 * EntityManagerFactory.
 *
 * @author hdl
 */
@Slf4j
@RequiredArgsConstructor
public class DataPermissionStatementInspector implements StatementInspector, HibernatePropertiesCustomizer {

    private final ObjectProvider<DataPermissionRuleFactory> ruleFactoryProvider;

    @Override
    public String inspect(String sql) {
        // 1. only rewrite filterable statements (SELECT / UPDATE / DELETE)
        if (sql == null || !isFilterable(sql)) {
            return sql;
        }
        // 2. fast guards — also keeps bootstrap/Flyway SQL (no login user) untouched,
        //    and avoids resolving the rule factory (and thus the permission API) too early
        if (SecurityFrameworkUtils.getLoginUser() == null || SecurityFrameworkUtils.skipPermissionCheck()) {
            return sql;
        }
        // 3. resolve applicable rules (honors the active @DataPermission via the context holder)
        DataPermissionRuleFactory ruleFactory = ruleFactoryProvider.getIfAvailable();
        if (ruleFactory == null) {
            return sql;
        }
        List<DataPermissionRule> rules = ruleFactory.getDataPermissionRule();
        if (rules == null || rules.isEmpty()) {
            return sql;
        }
        // 4. parse + rewrite (fail-closed: a parse failure must NOT silently bypass the filter)
        try {
            Statement statement = CCJSqlParserUtil.parse(sql);
            boolean modified;
            switch (statement) {
                case Select select -> modified = processSelect(select, rules);
                case Update update -> modified = processUpdate(update, rules);
                case Delete delete -> modified = processDelete(delete, rules);
                case null, default -> {
                    return sql;
                }
            }
            return modified ? statement.toString() : sql;
        } catch (JSQLParserException ex) {
            // fail-closed: surface loudly rather than running the query without the filter
            log.error("[inspect] failed to parse SQL for data permission (fail-closed). sql={}", sql, ex);
            throw new IllegalStateException("Data permission failed to parse SQL: " + sql, ex);
        }
    }

    @Override
    public void customize(Map<String, Object> hibernateProperties) {
        hibernateProperties.put(AvailableSettings.STATEMENT_INSPECTOR, this);
    }

    // ==================== SQL walking ====================

    private boolean processSelect(Select select, List<DataPermissionRule> rules) {
        boolean modified = false;
        // CTE / WITH items
        if (select.getWithItemsList() != null) {
            for (WithItem<?> withItem : select.getWithItemsList()) {
                modified |= processSelect(withItem.getSelect(), rules);
            }
        }
        if (select instanceof PlainSelect plainSelect) {
            modified |= processPlainSelect(plainSelect, rules);
        } else if (select instanceof SetOperationList setOperationList) {
            for (Select child : setOperationList.getSelects()) {
                modified |= processSelect(child, rules);
            }
        } else if (select instanceof ParenthesedSelect parenthesedSelect) {
            modified |= processSelect(parenthesedSelect.getSelect(), rules);
        }
        return modified;
    }

    private boolean processPlainSelect(PlainSelect plainSelect, List<DataPermissionRule> rules) {
        List<Expression> conditions = new ArrayList<>();
        // FROM
        boolean modified = collectFromItem(plainSelect.getFromItem(), rules, conditions);
        // JOINs
        if (plainSelect.getJoins() != null) {
            for (Join join : plainSelect.getJoins()) {
                modified |= collectFromItem(join.getFromItem(), rules, conditions);
            }
        }
        // append collected conditions into WHERE
        if (!conditions.isEmpty()) {
            Expression injected = and(conditions);
            Expression where = plainSelect.getWhere();
            plainSelect.setWhere(where == null ? injected : new AndExpression(parenthesize(where), injected));
            modified = true;
        }
        return modified;
    }

    private boolean processUpdate(Update update, List<DataPermissionRule> rules) {
        Expression expression = buildTableExpression(update.getTable(), rules);
        if (expression == null) {
            return false;
        }
        Expression where = update.getWhere();
        update.setWhere(where == null ? expression : new AndExpression(parenthesize(where), expression));
        return true;
    }

    private boolean processDelete(Delete delete, List<DataPermissionRule> rules) {
        Expression expression = buildTableExpression(delete.getTable(), rules);
        if (expression == null) {
            return false;
        }
        Expression where = delete.getWhere();
        delete.setWhere(where == null ? expression : new AndExpression(parenthesize(where), expression));
        return true;
    }

    /**
     * If the item is a table, collect its rule expression; if it is a subquery, recurse into it.
     */
    private boolean collectFromItem(FromItem fromItem, List<DataPermissionRule> rules, List<Expression> conditions) {
        if (fromItem instanceof Table table) {
            Expression expression = buildTableExpression(table, rules);
            if (expression != null) {
                conditions.add(expression);
            }
            return false; // collecting into the current level's WHERE is handled by caller
        }
        if (fromItem instanceof ParenthesedSelect parenthesedSelect) {
            return processSelect(parenthesedSelect.getSelect(), rules);
        }
        return false;
    }

    /**
     * Combine all matching rules' expressions for a single table (ported from yudao's getSqlSegment).
     */
    private Expression buildTableExpression(Table table, List<DataPermissionRule> rules) {
        String tableName = getTableName(table);
        Alias alias = table.getAlias();
        Expression all = null;
        for (DataPermissionRule rule : rules) {
            if (!rule.getTableNames().contains(tableName)) {
                continue;
            }
            Expression one = rule.getExpression(tableName, alias);
            if (one == null) {
                continue;
            }
            all = (all == null) ? one : new AndExpression(all, one);
        }
        return all;
    }

    // ==================== helpers ====================

    private static boolean isFilterable(String sql) {
        // skip leading whitespace / opening parenthesis, then check the leading keyword
        int i = 0;
        while (i < sql.length() && (Character.isWhitespace(sql.charAt(i)) || sql.charAt(i) == '(')) {
            i++;
        }
        return sql.regionMatches(true, i, "select", 0, 6)
                || sql.regionMatches(true, i, "with", 0, 4)
                || sql.regionMatches(true, i, "update", 0, 6)
                || sql.regionMatches(true, i, "delete", 0, 6);
    }

    private static String getTableName(Table table) {
        String name = table.getName();
        if (name == null) {
            return "";
        }
        // strip quoting; schema is exposed separately by JSqlParser via getSchemaName()
        return name.replace("\"", "");
    }

    private static Expression and(List<Expression> expressions) {
        Expression result = expressions.get(0);
        for (int i = 1; i < expressions.size(); i++) {
            result = new AndExpression(result, expressions.get(i));
        }
        return result;
    }

    /** Wrap an expression in parentheses so AND precedence is preserved when the original WHERE is an OR. */
    private static Expression parenthesize(Expression expression) {
        return new ParenthesedExpressionList<>(expression);
    }

}
