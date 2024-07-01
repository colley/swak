package com.swak.lock.spring.configuration;

import com.swak.core.SwakConstants;
import com.swak.core.sync.DistributedLock;
import com.swak.lock.annotation.LockAnnotationParser;
import com.swak.lock.parser.*;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Role;

@Configuration
@Import({LocalOnlyLockConfiguration.class, RedissonLockConfiguration.class, ZookeeperLockConfiguration.class})
@ConditionalOnBean(DistributedLock.class)
public class SwakLockAutoConfiguration{

    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    @ConditionalOnMissingBean(LockAnnotationParser.class)
    public LockAnnotationParser lockAnnotationParser() {
        return new SwakLockAnnotationParser();
    }

    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    @ConditionalOnMissingBean(LockOperationSource.class)
    public LockOperationSource lockOperationSource(LockAnnotationParser lockAnnotationParser) {
        return new DefaultLockOperationSource(lockAnnotationParser);
    }

    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    @ConditionalOnMissingBean(LockableInterceptor.class)
    public LockableInterceptor lockableInterceptor(DistributedLock distributedLock,LockOperationSource lockOperationSource) {
        LockableInterceptor interceptor = new LockableInterceptor();
        interceptor.setOperationSource(lockOperationSource);
        interceptor.setDistributedLock(distributedLock);
        return interceptor;
    }

    @Bean("internalLockableAdvisor")
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    @ConditionalOnMissingBean(LockableAnnotationBeanPostProcessor.class)
    public LockableAnnotationBeanPostProcessor internalLockableAdvisor(LockableInterceptor lockableInterceptor) {
        LockableAnnotationBeanPostProcessor dynamicAdvisor = new LockableAnnotationBeanPostProcessor(lockableInterceptor);
        dynamicAdvisor.setOrder(SwakConstants.ORDER_PRECEDENCE + 1001);
        return dynamicAdvisor;
    }
}
