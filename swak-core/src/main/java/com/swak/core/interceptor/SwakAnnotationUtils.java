package com.swak.core.interceptor;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.aop.support.AopUtils;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

public class SwakAnnotationUtils {

    public static Collection<? extends Annotation> parseAnnotations(AnnotatedElement ae,
        Set<Class<? extends Annotation>> annotationTypes) {
        Collection<? extends Annotation> ops = parseAnnotations(ae, false, annotationTypes);
        if (ops != null && ops.size() > 1) {
            Collection<? extends Annotation> localOps = parseAnnotations(ae, true, annotationTypes);
            if (localOps != null) {
                return localOps;
            }
        }
        return ops;
    }

    public static Collection<? extends Annotation> parseAnnotations(AnnotatedElement ae,
        boolean localOnly, Set<Class<? extends Annotation>> annotationTypes) {
        Collection<? extends Annotation> anns =
            (localOnly ? AnnotatedElementUtils.getAllMergedAnnotations(ae, annotationTypes)
                : AnnotatedElementUtils.findAllMergedAnnotations(ae, annotationTypes));
        return anns;
    }

    public static Collection<? extends Annotation> computeOperations(Method method,
        @Nullable Class<?> targetClass, Set<Class<? extends Annotation>> annotationTypes) {
        // Don't allow no-public methods as required.
        if (!Modifier.isPublic(method.getModifiers())) {
            return null;
        }
        // The method may be on an interface, but we need attributes from the target
        // class.
        // If the target class is null, the method will be unchanged.
        Method specificMethod = AopUtils.getMostSpecificMethod(method, targetClass);

        // First try is the method in the target class.
        Collection<? extends Annotation> opDef = findAnnotationOperations(specificMethod, annotationTypes);
        if (CollectionUtils.isNotEmpty(opDef)) {
            return opDef;
        }
        // Second try is the caching operation on the target class.
        opDef = findAnnotationOperations(specificMethod.getDeclaringClass(), annotationTypes);
        if (CollectionUtils.isNotEmpty(opDef) && ClassUtils.isUserLevelMethod(method)) {
            return opDef;
        }
        if (specificMethod != method) {
            // Fallback is to look at the original method.
            opDef = findAnnotationOperations(method, annotationTypes);
            if (CollectionUtils.isNotEmpty(opDef)) {
                return opDef;
            }
            // Last fallback is the class of the original method.
            opDef = findAnnotationOperations(method.getDeclaringClass(), annotationTypes);
            if (opDef != null && ClassUtils.isUserLevelMethod(method)) {
                return opDef;
            }
        }
        return null;
    }

    public static Collection<? extends Annotation> findAnnotationOperations(Method method,
        Set<Class<? extends Annotation>> annotationTypes) {
        return parseAnnotations(method, annotationTypes);
    }

    public static boolean isAnnotationPresent(Method method,
        Class<? extends Annotation> annotationTypes) {
        Set<Class<? extends Annotation>> annsSets = new LinkedHashSet<>(1);
        annsSets.add(annotationTypes);
        return CollectionUtils.isNotEmpty(findAnnotationOperations(method, annsSets));
    }

    public static Collection<? extends Annotation> findAnnotationOperations(Class<?> clazz,
        Set<Class<? extends Annotation>> annotationTypes) {
        return parseAnnotations(clazz, annotationTypes);
    }
}
