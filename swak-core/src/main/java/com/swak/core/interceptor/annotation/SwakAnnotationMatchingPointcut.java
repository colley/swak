package com.swak.core.interceptor.annotation;

import com.swak.core.interceptor.BasicOperationSource;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.aop.ClassFilter;
import org.springframework.aop.MethodMatcher;
import org.springframework.aop.Pointcut;
import org.springframework.aop.support.annotation.AnnotationMatchingPointcut;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collection;

public abstract class SwakAnnotationMatchingPointcut implements Pointcut {

    private ClassFilter classFilter;

    private final MethodMatcher methodMatcher;

    public SwakAnnotationMatchingPointcut() {
        this(false);
    }

    public SwakAnnotationMatchingPointcut(boolean checkInherited) {
        Collection<Class<? extends Annotation>> methodAnnotationTypes = getMethodAnnotationTypes();
        Assert.isTrue(methodAnnotationTypes != null,
            "Method annotation type needs to be specified");
        if (methodAnnotationTypes != null) {
            this.methodMatcher =
                new ExtAnnotationSetsMethodMatcher(methodAnnotationTypes, checkInherited);
        } else {
            this.methodMatcher = MethodMatcher.TRUE;
        }

        setClassFilter(getClassMatchingFilter());
    }

    @Override
    public MethodMatcher getMethodMatcher() {
        return this.methodMatcher;
    }

    @Override
    public boolean equals(@Nullable Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof AnnotationMatchingPointcut)) {
            return false;
        }
        SwakAnnotationMatchingPointcut otherPointcut = (SwakAnnotationMatchingPointcut) other;
        return (this.classFilter.equals(otherPointcut.classFilter)
            && this.methodMatcher.equals(otherPointcut.methodMatcher));
    }

    @Override
    public int hashCode() {
        return this.classFilter.hashCode() * 37 + this.methodMatcher.hashCode();
    }

    @Override
    public String toString() {
        return "SwakAnnotationMatchingPointcut: " + this.classFilter + ", " + this.methodMatcher;
    }

    public void setClassFilter(ClassFilter classFilter) {
        this.classFilter = classFilter;
    }

    @Override
    public ClassFilter getClassFilter() {
        return this.classFilter;
    }


    @Nullable
    protected abstract BasicOperationSource getOperationSource();

    @Nullable
    protected abstract Collection<Class<? extends Annotation>> getMethodAnnotationTypes();

    @Nullable
    protected abstract ClassFilter getClassMatchingFilter();

    protected class ExtAnnotationSetsMethodMatcher extends AnnotationSetsMethodMatcher {

        public ExtAnnotationSetsMethodMatcher(
            Collection<Class<? extends Annotation>> annotationTypes, boolean checkInherited) {
            super(annotationTypes);
        }

        public ExtAnnotationSetsMethodMatcher(Class<? extends Annotation> annotationType,
            boolean checkInherited) {
            super(annotationType, checkInherited);
        }

        public ExtAnnotationSetsMethodMatcher(Class<? extends Annotation> annotationType) {
            super(annotationType);
        }

        public ExtAnnotationSetsMethodMatcher(
            Collection<Class<? extends Annotation>> annotationTypes) {
            super(annotationTypes);
        }

        @Override
        public boolean matches(Method method, Class<?> targetClass) {
            boolean isMatches = super.matches(method, targetClass);
            if (isMatches) {
                BasicOperationSource cas = getOperationSource();
                if (cas != null
                    && CollectionUtils.isNotEmpty(cas.getBasicOperations(method, targetClass))) {
                    return true;
                }
            }
            return false;
        }
    }

}
