package com.swak.lock.parser;

import com.swak.core.interceptor.AbstractBasicOperationSource;
import com.swak.lock.annotation.LockAnnotationParser;
import com.swak.lock.annotation.LockOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;


@Slf4j
public class DefaultLockOperationSource extends AbstractBasicOperationSource<LockOperation> implements LockOperationSource {

    private static final Collection<LockOperation> NULL_ATTRIBUTE = Collections.emptyList();

    private LockAnnotationParser annotationParser;

    public DefaultLockOperationSource(LockAnnotationParser annotationParser) {
        this.annotationParser = annotationParser;
    }

    @Override
    public Collection<LockOperation> getBasicOperations(Method method, Class<?> targetClass) {
        if (method.getDeclaringClass() == Object.class) {
            return null;
        }
        Object cacheKey = getCacheKey(method, targetClass);
        Collection<LockOperation> cached = this.attributeCache.get(cacheKey);
        if (cached != null) {
            return (cached != NULL_ATTRIBUTE ? cached : null);
        } else {
            Collection<LockOperation> operations = computeOperations(method, targetClass);
            if (CollectionUtils.isNotEmpty(operations)) {
                this.attributeCache.put(cacheKey, operations);
            } else {
                this.attributeCache.put(cacheKey, NULL_ATTRIBUTE);
            }
            return operations;
        }
    }

    @Override
    protected Collection<LockOperation> findAnnotationOperations(Class<?> clazz) {
        return annotationParser.parseAnnotations(clazz);
    }

    @Override
    protected Collection<LockOperation> findAnnotationOperations(Method method, Class<?> targetClass) {
        return annotationParser.parseAnnotations(method, targetClass);
    }

    @Override
    public void afterSingletonsInstantiated() {
        //igroe
    }

    public void setAnnotationParser(SwakLockAnnotationParser annotationParser) {
        this.annotationParser = annotationParser;
    }
}
