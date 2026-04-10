/**
 * Copyright (C), 2018 store
 * Encoding: UTF-8
 * Date: 20-5-21 下午4:04
 * History:
 */
package com.swak.core.aware;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;


/**
 * the SwakAnnotationPostProcessor 处理所有swak自定的注解or
 * 
 * @author colley.ma
 * @since 2.4.0
 **/
@Configuration(proxyBeanMethods = false)
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
public class SwakAnnotationPostProcessor implements BeanPostProcessor {

    private final List<SwakPostProcessor> delegates = new ArrayList<>();

    @Autowired(required = false)
    public void setConfigurers(List<SwakPostProcessor> configurers) {
        if (!CollectionUtils.isEmpty(configurers)) {
            delegates.addAll(configurers);
        }
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (SwakPostProcessor.class.isAssignableFrom(bean.getClass())) {
            return bean;
        }
        for (SwakPostProcessor processor : delegates) {
            processor.postBeanBeforeInitialization(bean, beanName);
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (SwakPostProcessor.class.isAssignableFrom(bean.getClass())) {
            return bean;
        }
        for (SwakPostProcessor processor : delegates) {
            processor.postBeanAfterInitialization(bean, beanName);
        }
        return bean;
    }
}
