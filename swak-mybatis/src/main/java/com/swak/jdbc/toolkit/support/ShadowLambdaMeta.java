
package com.swak.jdbc.toolkit.support;


import com.swak.common.util.ClassUtils;
import com.swak.common.util.StringPool;

/**
 * 创建的元信息
 * @author colley.ma
 * @version v1.0
 * @since 2024/3/6 15:28
 **/
public class ShadowLambdaMeta implements LambdaMeta {
    private final SerializedLambda lambda;

    public ShadowLambdaMeta(SerializedLambda lambda) {
        this.lambda = lambda;
    }

    @Override
    public String getImplMethodName() {
        return lambda.getImplMethodName();
    }

    @Override
    public Class<?> getInstantiatedClass() {
        String instantiatedMethodType = lambda.getInstantiatedMethodType();
        String instantiatedType = instantiatedMethodType.substring(2, instantiatedMethodType.indexOf(StringPool.SEMICOLON)).replace(StringPool.FORWARD_SLASH, StringPool.DOT);
        return ClassUtils.toClassConfident(instantiatedType, lambda.getCapturingClass().getClassLoader());
    }

}
