package com.hdl.soar.framework.common.util.spring;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Utility class for Spring Expression Language (SpEL)
 */
public class SpringExpressionUtils {

    /**
     * Spring Expression Language (SpEL) expression parser
     */
    private static final ExpressionParser EXPRESSION_PARSER = new SpelExpressionParser();

    /**
     * Parameter name discoverer
     */
    private static final ParameterNameDiscoverer PARAMETER_NAME_DISCOVERER =
            new DefaultParameterNameDiscoverer();

    private SpringExpressionUtils() {}

    /**
     * Parse a single SpEL expression result from a join point
     *
     * @param joinPoint        the AOP join point
     * @param expressionString the SpEL expression
     * @return the evaluated result
     */
    public static Object parseExpression(JoinPoint joinPoint, String expressionString) {
        Map<String, Object> result =
                parseExpressions(joinPoint, Collections.singletonList(expressionString));
        return result.get(expressionString);
    }

    /**
     * Parse multiple SpEL expressions from a join point.
     *
     * @param joinPoint         the AOP join point
     * @param expressionStrings list of SpEL expressions
     * @return result map where key is the expression and value is the evaluated result
     */
    public static Map<String, Object> parseExpressions(JoinPoint joinPoint, List<String> expressionStrings) {
        // Return empty map if no expressions provided
        if (CollUtil.isEmpty(expressionStrings)) {
            return MapUtil.newHashMap();
        }

        // Step 1: build evaluation context
        // Get method from join point
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method method = methodSignature.getMethod();

        // Use Spring's ParameterNameDiscoverer to get parameter names
        String[] paramNames = PARAMETER_NAME_DISCOVERER.getParameterNames(method);

        // Create SpEL evaluation context
        EvaluationContext context = new StandardEvaluationContext();

        // Bind method parameters to context variables
        if (ArrayUtil.isNotEmpty(paramNames)) {
            Object[] args = joinPoint.getArgs();
            for (int i = 0; i < paramNames.length; i++) {
                context.setVariable(paramNames[i], args[i]);
            }
        }

        // Step 2: evaluate each expression
        Map<String, Object> result = MapUtil.newHashMap(expressionStrings.size(), true);
        expressionStrings.forEach(key -> {
            Object value = EXPRESSION_PARSER.parseExpression(key).getValue(context);
            result.put(key, value);
        });

        return result;
    }

    /**
     * Resolve a SpEL expression from the Spring Bean factory context
     *
     * @param expressionString the SpEL expression
     * @return the evaluated result
     */
    public static Object parseExpression(String expressionString) {
        return parseExpression(expressionString, null);
    }

    /**
     * Resolve a SpEL expression from the Spring Bean Factory context
     *
     * @param expressionString SpEL expression
     * @param variables        variables map
     * @return evaluated result
     */
    public static Object parseExpression(String expressionString, Map<String, Object> variables) {
        if (StrUtil.isBlank(expressionString)) {
            return null;
        }

        Expression expression = EXPRESSION_PARSER.parseExpression(expressionString);

        StandardEvaluationContext context = new StandardEvaluationContext();
        context.setBeanResolver(new BeanFactoryResolver(SpringUtil.getApplicationContext()));

        if (MapUtil.isNotEmpty(variables)) {
            context.setVariables(variables);
        }

        return expression.getValue(context);
    }

}
