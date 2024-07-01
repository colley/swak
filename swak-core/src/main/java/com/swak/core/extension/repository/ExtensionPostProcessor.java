package com.swak.core.extension.repository;

import com.swak.core.aware.SwakPostProcessor;
import com.swak.core.extension.ExtensionFinder;
import com.swak.core.extension.ExtensionPoint;
import com.swak.core.extension.annotation.ExtensionOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import java.util.Collection;

@Configuration(proxyBeanMethods = false)
@Slf4j
public class ExtensionPostProcessor implements SwakPostProcessor {


	@Autowired
	private ExtensionFinder extensionFinder;

	@Autowired
	private ExtensionRepository extensionRepository;

	@Override
	public void postBeanAfterInitialization(Object bean, String beanName) {
		// if spring bean is ExtensionPoint interfaces
		if (ExtensionPoint.class.isAssignableFrom(bean.getClass())) {
			// 获取非代理类
			Class<?> extensionClz = AopProxyUtils.ultimateTargetClass(bean);
			Collection<ExtensionOperation> extensionOperations = extensionFinder.find(extensionClz);
			if (CollectionUtils.isEmpty(extensionOperations)) {
				log.warn("Found {} extensions for extension,but not mark @Extension", extensionClz.getName());
				return;
			}
			ExtensionOperation extensionoperation = extensionOperations.iterator().next();
			if (extensionoperation == null) {
				log.warn("Found {} extensions for extension,but not mark @Extension", extensionClz.getName());
				return;
			}
			extensionoperation.setExtension((ExtensionPoint)bean);
			extensionRepository.doRegistration(extensionoperation);
		}
	}
}
