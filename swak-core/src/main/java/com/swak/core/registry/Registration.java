
package com.swak.core.registry;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.util.ClassUtils;

import java.beans.Introspector;

public class Registration {

	/**
	 *	 当Bean不存在的时候注册
	 * @param registry  注册中心
	 * @param beanClass 类型
	 * @return 成功标识
	 */
	public static BeanDefinition registerIfNotExists(BeanDefinitionRegistry registry, Class<?> beanClass) {
		return registerIfNotExists(registry, buildDefaultBeanName(beanClass), beanClass);
	}

	public static String buildDefaultBeanName(BeanDefinition definition) {
		String shortClassName = ClassUtils.getShortName(definition.getBeanClassName());
		return Introspector.decapitalize(shortClassName);
	}

	public static String buildDefaultBeanName(Class<?> beanClass) {
		String shortClassName = ClassUtils.getShortName(beanClass.getSimpleName());
		return Introspector.decapitalize(shortClassName);
	}

	/**
	 * 当Bean不存在的时候注册
	 *
	 * @param registry  注册中心
	 * @param beanName  名称
	 * @param beanClass 类型
	 * @return 成功标识
	 */
	public static BeanDefinition registerIfNotExists(final BeanDefinitionRegistry registry, final String beanName,
			final Class<?> beanClass) {
		if (registry.containsBeanDefinition(beanName)) {
			return registry.getBeanDefinition(beanName);
		}

		String[] candidates = registry.getBeanDefinitionNames();
		BeanDefinition definition;
		for (String candidate : candidates) {
			definition = registry.getBeanDefinition(candidate);
			if (beanClass.getName().equals(definition.getBeanClassName())) {
				return definition;
			}
		}

		BeanDefinition annotationProcessor = BeanDefinitionBuilder.genericBeanDefinition(beanClass).getBeanDefinition();
		registry.registerBeanDefinition(beanName, annotationProcessor);
		return registry.getBeanDefinition(beanName);
	}
}
