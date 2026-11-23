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
    public static LockAnnotationParser lockAnnotationParser() {
        return new SwakLockAnnotationParser();
    }

    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    @ConditionalOnBean(DistributedLock.class)
    @ConditionalOnMissingBean(LockOperationSource.class)
    public static LockOperationSource lockOperationSource(LockAnnotationParser lockAnnotationParser) {
        return new DefaultLockOperationSource(lockAnnotationParser);
    }

    /**
     * 注意：这里刻意不注入 {@link DistributedLock}。本 Bean 会被
     * {@link LockableAnnotationBeanPostProcessor}（BeanPostProcessor）提前触发实例化，
     * 若在此注入锁实现，会连带把 {@code DistributedLock} 提前实例化，导致它无法被所有
     * BeanPostProcessor 加工并打出 BeanPostProcessorChecker 警告。
     * {@link LockableInterceptor#afterSingletonsInstantiated()} 会在单例全部就绪后从
     * BeanFactory 中惰性获取锁实现。
     */
    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    @ConditionalOnBean(DistributedLock.class)
    @ConditionalOnMissingBean(LockableInterceptor.class)
    public static LockableInterceptor lockableInterceptor(LockOperationSource lockOperationSource) {
        LockableInterceptor interceptor = new LockableInterceptor();
        interceptor.setOperationSource(lockOperationSource);
        return interceptor;
    }

    /**
     * 声明为 {@code static}：BeanPostProcessor 在容器启动极早期创建，非 static 工厂方法会强制提前
     * 实例化本配置类，从而错过部分 BeanPostProcessor 的处理（BeanPostProcessorChecker 会告警）。
     */
    @Bean("internalLockableAdvisor")
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    @ConditionalOnBean(LockableInterceptor.class)
    @ConditionalOnMissingBean(LockableAnnotationBeanPostProcessor.class)
    public static LockableAnnotationBeanPostProcessor internalLockableAdvisor(LockableInterceptor lockableInterceptor) {
        LockableAnnotationBeanPostProcessor dynamicAdvisor = new LockableAnnotationBeanPostProcessor(lockableInterceptor);
        dynamicAdvisor.setOrder(SwakConstants.ORDER_PRECEDENCE + 1001);
        return dynamicAdvisor;
    }
}
