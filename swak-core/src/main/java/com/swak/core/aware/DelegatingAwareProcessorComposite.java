package com.swak.core.aware;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Configuration(proxyBeanMethods = false)
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
public class DelegatingAwareProcessorComposite {

    private final AwareProcessorComposite composites = new AwareProcessorComposite();

    @Autowired(required = false)
    public void setAwareProcessor(List<SwakAwareProcessor> awareProcessors) {
        if (!CollectionUtils.isEmpty(awareProcessors)) {
            this.composites.addAwareProcessors(awareProcessors);
        }
    }

    public void afterInstantiated() {
        composites.processor();
    }
}
