package com.swak.operatelog;

import com.google.common.collect.Sets;
import com.swak.core.interceptor.annotation.SwakAnnotationBeanPostProcessor;
import com.swak.operatelog.annotation.OperateLog;
import org.aopalliance.aop.Advice;

import java.util.Arrays;

@SuppressWarnings("serial")
public class OperateLogAnnotationBeanPostProcessor extends SwakAnnotationBeanPostProcessor {

    private OperateLogInterceptor operateLogInterceptor;

    public OperateLogAnnotationBeanPostProcessor() {
        super(Sets.newHashSet(Arrays.asList(OperateLog.class)));
    }

    public OperateLogAnnotationBeanPostProcessor(OperateLogInterceptor operateLogInterceptor) {
        super(Sets.newHashSet(Arrays.asList(OperateLog.class)));
        this.operateLogInterceptor = operateLogInterceptor;
    }


    @Override
    public Advice getAnnotationAdvice() {
        return this.operateLogInterceptor;
    }
}
