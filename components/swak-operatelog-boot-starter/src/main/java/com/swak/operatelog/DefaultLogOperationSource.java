package com.swak.operatelog;

import com.swak.core.interceptor.AbstractBasicOperationSource;
import com.swak.operatelog.annotation.OperateLogOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;

/**
 * @author colley
 */
@Slf4j
public class DefaultLogOperationSource extends AbstractBasicOperationSource<OperateLogOperation> implements OperateLogOperationSource {

    private static final Collection<OperateLogOperation> NULL_ATTRIBUTE = Collections.emptyList();

    private OperateLogAnnotationParser annotationParser;

    public DefaultLogOperationSource(OperateLogAnnotationParser annotationParser) {
        this.annotationParser = annotationParser;
    }

    @Override
    public Collection<OperateLogOperation> getBasicOperations(Method method, Class<?> targetClass) {
        if (method.getDeclaringClass() == Object.class) {
            return null;
        }
        Object cacheKey = getCacheKey(method, targetClass);
        Collection<OperateLogOperation> cached = this.attributeCache.get(cacheKey);
        if (cached != null) {
            return (cached != NULL_ATTRIBUTE ? cached : null);
        } else {
            Collection<OperateLogOperation> operations = computeOperations(method, targetClass);
            if (CollectionUtils.isNotEmpty(operations)) {
                this.attributeCache.put(cacheKey, operations);
            } else {
                this.attributeCache.put(cacheKey, NULL_ATTRIBUTE);
            }
            return operations;
        }
    }

    @Override
    protected Collection<OperateLogOperation> findAnnotationOperations(Class<?> clazz) {
        return annotationParser.parseAnnotations(clazz);
    }

    @Override
    protected Collection<OperateLogOperation> findAnnotationOperations(Method method, Class<?> targetClass) {
        return annotationParser.parseAnnotations(method, targetClass);
    }

    @Override
    public void afterSingletonsInstantiated() {
        //igroe
    }

    public void setAnnotationParser(OperateLogAnnotationParser annotationParser) {
        this.annotationParser = annotationParser;
    }
}
