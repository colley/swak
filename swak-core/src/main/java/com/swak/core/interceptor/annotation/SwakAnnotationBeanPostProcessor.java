package com.swak.core.interceptor.annotation;

import org.aopalliance.aop.Advice;
import org.springframework.aop.framework.autoproxy.AbstractBeanFactoryAwareAdvisingPostProcessor;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.lang.annotation.Annotation;
import java.util.Collection;

@SuppressWarnings("serial")
public abstract class SwakAnnotationBeanPostProcessor
        extends AbstractBeanFactoryAwareAdvisingPostProcessor {

    @Nullable
    private Collection<Class<? extends Annotation>> annotationTypes;

    public SwakAnnotationBeanPostProcessor() {
        setBeforeExistingAdvisors(true);
    }

    public SwakAnnotationBeanPostProcessor(
            Collection<Class<? extends Annotation>> annotationTypes) {
        this();
        setAnnotationTypes(annotationTypes);
    }

    public void setAnnotationTypes(Collection<Class<? extends Annotation>> annotationTypes) {
        Assert.notNull(annotationTypes, "'annotationTypes' must not be null");
        this.annotationTypes = annotationTypes;
    }

    public abstract Advice getAnnotationAdvice();

    @Override
    public void setBeanFactory(BeanFactory beanFactory) {
        super.setBeanFactory(beanFactory);
        SwakAnnotationAdvisor advisor = new SwakAnnotationAdvisor() {
            @Override
            protected Collection<Class<? extends Annotation>> getAnnotationTypes() {
                return annotationTypes;
            }

            @Override
            protected Advice buildAdvice() {
                return getAnnotationAdvice();
            }
        };
        advisor.setBeanFactory(beanFactory);
        advisor.setOrder(getOrder());
        this.advisor = advisor;
    }
}
