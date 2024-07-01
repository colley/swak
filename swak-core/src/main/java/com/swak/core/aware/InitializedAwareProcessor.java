package com.swak.core.aware;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;
import org.springframework.core.annotation.Order;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Configuration(proxyBeanMethods = false)
@Order(3)
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
@Slf4j
public class InitializedAwareProcessor implements SwakAwareProcessor {

    private final List<InitializedAware> delegates = Lists.newArrayList();

    @Autowired(required = false)
    public void setConfigurers(List<InitializedAware> configurers) {
        if (!CollectionUtils.isEmpty(configurers)) {
            delegates.addAll(configurers);
        }
    }


    @Override
    public void processor() {
        if (CollectionUtils.isEmpty(delegates)) {
            log.error(
                "[swak-initializedAware] - InitializedAware init error,configurers is Empty!");
            return;
        }
        for (InitializedAware delegate : delegates) {
            log.warn("[swak-initializedAware] - {}.afterInstantiated",
                delegate.getClass().getName());
            delegate.afterInstantiated();
        }
    }
}
