package com.swak.core.interceptor;

import com.swak.common.dto.base.BaseOperation;

import java.lang.reflect.Method;
import java.util.Collection;

public interface SwakAnnotationParser<T extends BaseOperation> {

    default boolean isCandidateClass(Class<?> targetClass) {
        return true;
    }

    Collection<T> parseAnnotations(Class<?> type);

    Collection<T> parseAnnotations(Method method, Class<?> targetClass);
}
