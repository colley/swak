package com.swak.core.interceptor.annotation;

import org.springframework.aop.support.AopUtils;
import org.springframework.aop.support.StaticMethodMatcher;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;

public class AnnotationSetsMethodMatcher extends StaticMethodMatcher {
    private final Collection<Class<? extends Annotation>> annotationTypes;

    private final boolean checkInherited;

    public AnnotationSetsMethodMatcher(Class<? extends Annotation> annotationType) {
        this(annotationType, false);
    }

    public AnnotationSetsMethodMatcher(Class<? extends Annotation> annotationType,
        boolean checkInherited) {
        this(Collections.singleton(annotationType), checkInherited);
    }

    public AnnotationSetsMethodMatcher(Collection<Class<? extends Annotation>> annotationTypes,
        boolean checkInherited) {
        Assert.notNull(annotationTypes, "Annotation type must not be null");
        this.annotationTypes = new LinkedHashSet<>(annotationTypes);
        this.checkInherited = checkInherited;
    }

    public AnnotationSetsMethodMatcher(Collection<Class<? extends Annotation>> annotationTypes) {
        this(annotationTypes, false);
    }

    @Override
    public boolean matches(Method method, Class<?> targetClass) {
        if (matchesMethod(method)) {
            return true;
        }
        // Proxy classes never have annotations on their redeclared methods.
        if (Proxy.isProxyClass(targetClass)) {
            return false;
        }
        // The method may be on an interface, so let's check on the target class as well.
        Method specificMethod = AopUtils.getMostSpecificMethod(method, targetClass);
        return (specificMethod != method && matchesMethod(specificMethod));
    }

    private boolean matchesMethod(Method method) {
        for (Class<? extends Annotation> annotationType : annotationTypes) {
            if (this.checkInherited ? AnnotatedElementUtils.hasAnnotation(method, annotationType)
                : method.isAnnotationPresent(annotationType)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean equals(@Nullable Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof AnnotationSetsMethodMatcher)) {
            return false;
        }
        AnnotationSetsMethodMatcher otherMm = (AnnotationSetsMethodMatcher) other;
        return (this.annotationTypes.equals(otherMm.annotationTypes)
            && this.checkInherited == otherMm.checkInherited);
    }

    @Override
    public int hashCode() {
        return this.annotationTypes.hashCode();
    }

    @Override
    public String toString() {
        return getClass().getName() + ": " + this.annotationTypes;
    }

}
