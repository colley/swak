package com.swak.core.interceptor;

import com.swak.common.dto.base.BaseOperation;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.core.MethodClassKey;
import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractBasicOperationSource<T extends BaseOperation>
        implements BasicOperationSource<T> {

    /**
     * Cache of SwakOperations, keyed by method on a specific target class.
     * <p>
     * As this base class is not marked Serializable, the cache will be recreated after
     * serialization - provided that
     * the concrete subclass is Serializable.
     */
    protected final Map<Object, Collection<T>> attributeCache = new ConcurrentHashMap<>(1024);

    protected BeanFactory beanFactory;

    protected Object getCacheKey(Method method, @Nullable Class<?> targetClass) {
        return new MethodClassKey(method, targetClass);
    }

    @Nullable
    protected Collection<T> computeOperations(Method method, @Nullable Class<?> targetClass) {
        // Don't allow non-public methods, as configured.
        if (allowPublicMethodsOnly() && !Modifier.isPublic(method.getModifiers())) {
            return null;
        }

        // The method may be on an interface, but we need attributes from the target class.
        // If the target class is null, the method will be unchanged.
        Method specificMethod = AopUtils.getMostSpecificMethod(method, targetClass);

        // First try is the method in the target class.
        Collection<T> opDef = findAnnotationOperations(specificMethod, targetClass);
        if (opDef != null) {
            return opDef;
        }

        // Second try is the swak custom annotation operation on the target class.
        opDef = findAnnotationOperations(specificMethod.getDeclaringClass());
        if (opDef != null && ClassUtils.isUserLevelMethod(method)) {
            return opDef;
        }

        if (specificMethod != method) {
            // Fallback is to look at the original method.
            opDef = findAnnotationOperations(method, targetClass);
            if (opDef != null) {
                return opDef;
            }
            // Last fallback is the class of the original method.
            opDef = findAnnotationOperations(method.getDeclaringClass());
            if (opDef != null && ClassUtils.isUserLevelMethod(method)) {
                return opDef;
            }
        }

        return null;
    }

    /**
     * Subclasses need to implement this to return the swak custom annotation attribute for the
     * given class, if any.
     *
     * @param clazz the class to retrieve the attribute for
     * @return all swak custom annotation attribute associated with this class, or {@code null} if
     * none
     */
    @Nullable
    protected abstract Collection<T> findAnnotationOperations(Class<?> clazz);

    /**
     * Subclasses need to implement this to return the swak custom annotation attribute for the
     * given method, if any.
     *
     * @param method the method to retrieve the attribute for
     * @return all swak custom annotation attribute associated with this method, or {@code null} if
     * none
     */
    @Nullable
    protected abstract Collection<T> findAnnotationOperations(Method method, Class<?> targetClass);

    /**
     * Should only public methods be allowed to have swak custom annotation semantics?
     * <p>
     * The default implementation returns {@code false}.
     */
    protected boolean allowPublicMethodsOnly() {
        return false;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }
}
