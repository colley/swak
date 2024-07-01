package com.swak.core.interceptor;

import org.aopalliance.intercept.MethodInterceptor;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.BeanFactoryAnnotationUtils;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

import java.lang.reflect.Method;

public abstract class SwakAdviceSupport
        implements MethodInterceptor, BeanFactoryAware, InitializingBean, SmartInitializingSingleton {

    protected BasicOperationSource operationSource;


    protected BeanFactory beanFactory;

    protected boolean initialized = false;

    /**
     * Return the BasicOperationSource for aspect.
     */

    public BasicOperationSource getOperationSource() {
        return this.operationSource;
    }

    public void setOperationSource(BasicOperationSource operationSource) {
        this.operationSource = operationSource;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    @Override
    public void afterPropertiesSet() {
        Assert.state(getOperationSource() != null,
                "The 'OperationSource' property is required: "
                        + "If there are no  operationSource methods, then don't use a operationSource aspect.");
    }

    @Override
    public void afterSingletonsInstantiated() {

    }

    /**
     * Convenience method to return a String representation of this Method for use in logging. Can
     * be overridden in
     * subclasses to provide a different identifier for the given method.
     *
     * @param method      the method we're interested in
     * @param targetClass class the method is on
     * @return log message identifying this method
     * @see org.springframework.util.ClassUtils#getQualifiedMethodName
     */
    protected String methodIdentification(Method method, Class<?> targetClass) {
        Method specificMethod = ClassUtils.getMostSpecificMethod(method, targetClass);
        return ClassUtils.getQualifiedMethodName(specificMethod);
    }

    /**
     * Return a bean with the specified name and type
     *
     * @param beanName     the name of the bean, as defined by the operation
     * @param expectedType type for the bean
     * @return the bean matching that name
     * @throws org.springframework.beans.factory.NoSuchBeanDefinitionException if such bean does not
     *                                                                         exist
     */
    protected <T> T getBean(String beanName, Class<T> expectedType) {
        if (this.beanFactory == null) {
            throw new IllegalStateException("BeanFactory must be set on cache aspect for "
                    + expectedType.getSimpleName() + " retrieval");
        }
        return BeanFactoryAnnotationUtils.qualifiedBeanOfType(this.beanFactory, expectedType,
                beanName);
    }

    /**
     * Execute the underlying operation (typically in case of cache miss) and return the result of
     * the invocation. If an
     * exception occurs it will be wrapped in a {@link com.swak.common.exception.ThrowableWrapper}: the
     * exception can be
     * handled or modified but it <em>must</em> be wrapped in a
     *
     * @param invoker the invoker handling the operation being cached
     * @return the result of the invocation
     * @see SwakOperationInvoker#invoke()
     */
    protected Object invokeOperation(SwakOperationInvoker invoker) {
        return invoker.invoke();
    }

    protected Class<?> getTargetClass(Object target) {
        return AopProxyUtils.ultimateTargetClass(target);
    }

}
