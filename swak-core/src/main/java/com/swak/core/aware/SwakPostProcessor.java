package com.swak.core.aware;

public interface SwakPostProcessor {
    
    default public void postBeanBeforeInitialization(Object bean, String beanName) {}


    default public void postBeanAfterInitialization(Object bean, String beanName) {}
}
