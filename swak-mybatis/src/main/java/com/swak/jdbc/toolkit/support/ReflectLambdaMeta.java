
package com.swak.jdbc.toolkit.support;

import com.swak.common.util.ClassUtils;
import com.swak.common.util.StringPool;

import java.lang.invoke.SerializedLambda;

/**
 * ReflectLambdaMeta
 * @author colley.ma
 * @version v1.0
 * @since 2024/3/6 15:27
 **/
public class ReflectLambdaMeta implements LambdaMeta {
    private final SerializedLambda lambda;

    private final ClassLoader classLoader;

    public ReflectLambdaMeta(SerializedLambda lambda, ClassLoader classLoader) {
        this.lambda = lambda;
        this.classLoader = classLoader;
    }

    @Override
    public String getImplMethodName() {
        return lambda.getImplMethodName();
    }

    @Override
    public Class<?> getInstantiatedClass() {
        String instantiatedMethodType = lambda.getInstantiatedMethodType();
        String instantiatedType = instantiatedMethodType.substring(2, instantiatedMethodType.indexOf(StringPool.SEMICOLON)).replace(StringPool.FORWARD_SLASH, StringPool.DOT);
        return ClassUtils.toClassConfident(instantiatedType, this.classLoader);
    }

}
