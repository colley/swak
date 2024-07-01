package com.swak.core.interceptor;

import com.swak.common.dto.base.BaseOperation;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.lang.Nullable;

import java.lang.reflect.Method;
import java.util.Collection;

/**
 * basic Operation Source
 *
 * @author colley.ma
 * @since  2022/01/26
 */
public interface BasicOperationSource<T extends BaseOperation> extends SmartInitializingSingleton, BeanFactoryAware {
    default boolean isCandidateClass(Class<?> targetClass) {
        return true;
    }

    Collection<T> getBasicOperations(Method method, @Nullable Class<?> targetClass);
}
