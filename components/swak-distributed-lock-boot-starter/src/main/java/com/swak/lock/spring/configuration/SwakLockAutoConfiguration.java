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

/**
 * 分布式锁自动配置。
 * <p>
 * 注意：这里不能使用类级 {@code @ConditionalOnBean(DistributedLock.class)}。{@code DistributedLock}
 * 由本类 {@code @Import} 的 {@code LocalOnlyLockConfiguration}/{@code RedissonLockConfiguration}/
 * {@code ZookeeperLockConfiguration} 提供，而 Spring 先评估类级条件、后处理 {@code @Import}，
 * 类级条件永远不成立。因此条件下沉到各个 {@code @Bean} 方法上。
 *
 * @author colley.ma
 */
@Configuration
@Import({LocalOnlyLockConfiguration.class, RedissonLockConfiguration.class, ZookeeperLockConfiguration.class})
public class SwakLockAutoConfiguration {

    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    @ConditionalOnBean(DistributedLock.class)
    @ConditionalOnMissingBean(LockAnnotationParser.class)
    public LockAnnotationParser lockAnnotationParser() {
        return new SwakLockAnnotationParser();
    }

    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    @ConditionalOnBean(DistributedLock.class)
    @ConditionalOnMissingBean(LockOperationSource.class)
    public LockOperationSource lockOperationSource(LockAnnotationParser lockAnnotationParser) {
        return new DefaultLockOperationSource(lockAnnotationParser);
    }

    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    @ConditionalOnBean(DistributedLock.class)
    @ConditionalOnMissingBean(LockableInterceptor.class)
    public LockableInterceptor lockableInterceptor(DistributedLock distributedLock, LockOperationSource lockOperationSource) {
        LockableInterceptor interceptor = new LockableInterceptor();
        interceptor.setOperationSource(lockOperationSource);
        interceptor.setDistributedLock(distributedLock);
        return interceptor;
    }

    @Bean("internalLockableAdvisor")
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    @ConditionalOnBean(LockableInterceptor.class)
    @ConditionalOnMissingBean(LockableAnnotationBeanPostProcessor.class)
    public LockableAnnotationBeanPostProcessor internalLockableAdvisor(LockableInterceptor lockableInterceptor) {
        LockableAnnotationBeanPostProcessor dynamicAdvisor = new LockableAnnotationBeanPostProcessor(lockableInterceptor);
        dynamicAdvisor.setOrder(SwakConstants.ORDER_PRECEDENCE + 1001);
        return dynamicAdvisor;
    }
}
