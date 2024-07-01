package com.swak.lock.parser;

import com.google.common.collect.Sets;
import com.swak.core.interceptor.annotation.SwakAnnotationBeanPostProcessor;
import com.swak.lock.annotation.Lockable;
import org.aopalliance.aop.Advice;

import java.util.Arrays;

@SuppressWarnings("serial")
public class LockableAnnotationBeanPostProcessor extends SwakAnnotationBeanPostProcessor {

    private LockableInterceptor lockableInterceptor;

    public LockableAnnotationBeanPostProcessor() {
        super(Sets.newHashSet(Arrays.asList(Lockable.class)));
    }

    public LockableAnnotationBeanPostProcessor(LockableInterceptor lockableInterceptor) {
        super(Sets.newHashSet(Arrays.asList(Lockable.class)));
        this.lockableInterceptor = lockableInterceptor;
    }


    @Override
    public Advice getAnnotationAdvice() {
        return this.lockableInterceptor;
    }
}
