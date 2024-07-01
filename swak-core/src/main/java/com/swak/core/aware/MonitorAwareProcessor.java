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
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
@Order(100)
@Slf4j
public class MonitorAwareProcessor implements SwakAwareProcessor {
    private final List<MonitorAware> delegates = Lists.newArrayList();

    @Autowired(required = false)
    public void setConfigurers(List<MonitorAware> configurers) {
        if (!CollectionUtils.isEmpty(configurers)) {
            delegates.addAll(configurers);
        }
    }

    @Override
    public void processor() {
        if (CollectionUtils.isEmpty(delegates)) {
            log.error("[swak-monitorAware] - MonitorAware init error,configurers is Empty!");
            return;
        }
        for (MonitorAware delegate : delegates) {
        	log.warn("[swak-monitorAware] - [enabled:{}] {}.startup", delegate.enabled(),
                delegate.getClass().getName());
            if (delegate.enabled()) {
                delegate.startup();
            }
        }
    }
}
