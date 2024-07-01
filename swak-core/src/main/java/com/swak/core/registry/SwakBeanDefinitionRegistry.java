
package com.swak.core.registry;

import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

import java.util.Map;

public abstract class SwakBeanDefinitionRegistry implements BeanFactoryAware,EnvironmentAware {

    protected DefaultListableBeanFactory beanFactory;
    
    protected Environment environment;

    protected BeanDefinition registerIfNotExists(final String beanName, final Class<?> beanClass,
        Map<String, Object> propertyMap) {
    	return registerIfNotExists(beanName, beanClass, propertyMap,false);
    }

    protected BeanDefinition registerIfNotExists(final String beanName, final Class<?> beanClass,
        Map<String, Object> propertyMap,boolean primary) {
        if (beanFactory.containsBeanDefinition(beanName)) {
            return beanFactory.getBeanDefinition(beanName);
        }
        BeanDefinitionBuilder beanDefinitionBuilder =
            BeanDefinitionBuilder.genericBeanDefinition(beanClass);
        if (MapUtils.isNotEmpty(propertyMap)) {
            propertyMap.forEach((k, v) -> {
                beanDefinitionBuilder.addPropertyValue(k, v);
            });
        }
        if(primary) {
            beanDefinitionBuilder.setPrimary(primary);
        }
        beanFactory.registerBeanDefinition(beanName, beanDefinitionBuilder.getBeanDefinition());
        return beanFactory.getBeanDefinition(beanName);
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = (DefaultListableBeanFactory) beanFactory;
    }

	@Override
	public void setEnvironment(Environment environment) {
		this.environment = environment;
	}
    
    
}
