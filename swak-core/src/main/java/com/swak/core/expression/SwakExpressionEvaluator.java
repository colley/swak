package com.swak.core.expression;

import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author colley.ma
 * @since 3.0.0
 **/
public class SwakExpressionEvaluator {

    public static final Object NO_RESULT = new Object();


    private final SpelExpressionParser parser = new SpelExpressionParser();

    // shared param discoverer since it caches data internally
    private final ParameterNameDiscoverer paramNameDiscoverer = new DefaultParameterNameDiscoverer();

    private final Map<String, Expression> keyCache = new ConcurrentHashMap<String, Expression>(64);

    private final Map<String, Expression> conditionCache = new ConcurrentHashMap<String, Expression>(64);

    private final Map<String, Expression> unlessCache = new ConcurrentHashMap<String, Expression>(64);

    private final Map<String, Method> targetMethodCache = new ConcurrentHashMap<String, Method>(64);


    /**
     * Create an {@link EvaluationContext} without a return value.
     *
     * @see #createEvaluationContext(Method, Object[], Object, Class, Object)
     */
    public EvaluationContext createEvaluationContext(Method method, Object[] args, Object target, Class<?> targetClass) {
        return createEvaluationContext(method, args, target, targetClass,
                NO_RESULT);
    }

    /**
     * Create an {@link EvaluationContext}.
     *
     * @param method      the method
     * @param args        the method arguments
     * @param target      the target object
     * @param targetClass the target class
     * @param result      the return value (can be {@code null}) or
     *                    {@link #NO_RESULT} if there is no return at this time
     * @return the evaluation context
     */
    public EvaluationContext createEvaluationContext(Method method, Object[] args, Object target, Class<?> targetClass,
                                                     final Object result) {
        SwakExpressionRootObject rootObject = new SwakExpressionRootObject(method, args, target, targetClass);
        SwakLazyParamAwareEvaluationContext evaluationContext = new SwakLazyParamAwareEvaluationContext(rootObject,
                this.paramNameDiscoverer, method, args, targetClass, this.targetMethodCache);
        if (result != NO_RESULT) {
            evaluationContext.setVariable("result", result);
        }
        return evaluationContext;
    }

    public Object key(String keyExpression, Method method, EvaluationContext evalContext) {
        return getExpression(this.keyCache, keyExpression, method).getValue(evalContext);
    }

    public boolean condition(String conditionExpression, Method method, EvaluationContext evalContext) {
        return getExpression(this.conditionCache, conditionExpression, method).getValue(
                evalContext, boolean.class);
    }

    public boolean unless(String unlessExpression, Method method, EvaluationContext evalContext) {
        return getExpression(this.unlessCache, unlessExpression, method).getValue(
                evalContext, boolean.class);
    }

    private Expression getExpression(Map<String, Expression> cache, String expression, Method method) {
        String key = toString(method, expression);
        Expression rtn = cache.get(key);
        if (rtn == null) {
            rtn = this.parser.parseExpression(expression);
            cache.put(key, rtn);
        }
        return rtn;
    }

    private String toString(Method method, String expression) {
        StringBuilder sb = new StringBuilder();
        sb.append(method.getDeclaringClass().getName());
        sb.append("#");
        sb.append(method);
        sb.append("#");
        sb.append(expression);
        return sb.toString();
    }
}
