package com.swak.core.interceptor.annotation;

import org.aopalliance.aop.Advice;
import org.springframework.aop.Pointcut;
import org.springframework.aop.support.AbstractPointcutAdvisor;
import org.springframework.aop.support.ComposablePointcut;
import org.springframework.aop.support.annotation.AnnotationMatchingPointcut;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.util.Assert;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("serial")
public abstract class SwakAnnotationAdvisor extends AbstractPointcutAdvisor
    implements BeanFactoryAware {

    private Advice advice;

    private Pointcut pointcut;

    /**
     * Create a new {@code SwakAnnotationAdvisor} for bean-style configuration.
     */
    public SwakAnnotationAdvisor() {
        this.advice = buildAdvice();
        Collection<Class<? extends Annotation>> annotationTypes = getAnnotationTypes();
        Assert.notNull(annotationTypes, "'annotationTypes' must not be null");
        this.pointcut = buildPointcut(annotationTypes);
    }

    public void setAnnotationType(Class<? extends Annotation> annotationType) {
        Assert.notNull(annotationType, "'annotationType' must not be null");
        Set<Class<? extends Annotation>> annotationTypes = new HashSet<>();
        annotationTypes.add(annotationType);
        this.pointcut = buildPointcut(annotationTypes);
    }

    /**
     * Set the {@code BeanFactory} to be used when looking up executors by qualifier.
     */
    @Override
    public void setBeanFactory(BeanFactory beanFactory) {
        if (this.advice instanceof BeanFactoryAware) {
            ((BeanFactoryAware) this.advice).setBeanFactory(beanFactory);
        }
    }

    @Override
    public Advice getAdvice() {
        return this.advice;
    }

    @Override
    public Pointcut getPointcut() {
        return this.pointcut;
    }

    protected abstract Collection<Class<? extends Annotation>> getAnnotationTypes();

    protected abstract Advice buildAdvice();

    protected Pointcut buildPointcut(Collection<Class<? extends Annotation>> annotationTypes) {
        ComposablePointcut result = null;
        for (Class<? extends Annotation> annotationType : annotationTypes) {
            Pointcut cpc = new AnnotationMatchingPointcut(annotationType, true);
            Pointcut mpc = new AnnotationMatchingPointcut(null, annotationType, true);
            if (result == null) {
                result = new ComposablePointcut(cpc);
            } else {
                result.union(cpc);
            }
            result = result.union(mpc);
        }
        return (result != null ? result : Pointcut.TRUE);
    }
}
