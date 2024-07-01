
package com.swak.lock.parser;

import com.swak.core.interceptor.SwakAnnotationUtils;
import com.swak.lock.annotation.LockAnnotationParser;
import com.swak.lock.annotation.LockOperation;
import com.swak.lock.annotation.Lockable;
import org.springframework.util.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.*;


/**
 * Cacheable 扩展 ExtCacheable解析
 */
public class SwakLockAnnotationParser implements LockAnnotationParser {

    private static final Set<Class<? extends Annotation>> CACHE_OPERATION_ANNOTATIONS = new LinkedHashSet<>(1);
    static {
        CACHE_OPERATION_ANNOTATIONS.add(Lockable.class);
    }

    /**
     * {@link Lockable} not support class,@Target is method
     */
    @Override
    public Collection<LockOperation> parseAnnotations(Class<?> type) {
        return Collections.emptyList();
    }

    @Override
    public Collection<LockOperation> parseAnnotations(Method method, Class<?> targetClass) {
        Collection<? extends Annotation> ans =
            SwakAnnotationUtils.computeOperations(method, targetClass, CACHE_OPERATION_ANNOTATIONS);
        if (ans == null) {
            return null;
        }
        final Collection<LockOperation> ops = new ArrayList<>(1);
        ans.stream().filter(ann -> ann instanceof Lockable)
            .forEach(ann -> ops.add(parseLockAnnotation(method, (Lockable) ann)));
        return ops;
    }

    private LockOperation parseLockAnnotation(AnnotatedElement ae, Lockable lockable) {
        LockOperation lockOperation = new LockOperation()
                .setKey(lockable.key()).setLeaseTime(lockable.leaseTime())
                        .setTimeToTry(lockable.timeToTry()).setTimeUnit(lockable.timeUnit());
        validateLockOperation(ae, lockOperation);
        return lockOperation;
    }

    private void validateLockOperation(AnnotatedElement ae, LockOperation operation) {
        if (StringUtils.hasText(operation.getKey())) {
            throw new IllegalStateException("Invalid Lockable annotation configuration on '" + ae.toString()
                + "'.'key' attributes have been set. These attributes are mutually exclusive: either set the SpEL expression used to"
                + "compute the key at runtime to use.");
        }
    }
}
