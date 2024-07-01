package com.swak.core.aware;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class InjectBeanSelfProcessor implements SwakPostProcessor {

    @Override
    public void postBeanAfterInitialization(Object bean, String beanName) {
        // 因为spring的aop事物管理存在嵌套调用问题，故需要如此处理
        if (BeanSelfAware.class.isAssignableFrom(bean.getClass())) {
            log.warn("injectBeanSelfProcessor.setSelf. beanName:{}", bean.getClass().getName(),
                beanName);
            @SuppressWarnings({"rawtypes", "unchecked"})
            BeanSelfAware<Object> bs = ((BeanSelfAware) bean);
            bs.setServiceBeanSelf(bean);
        }
    }
}
