package com.swak.operatelog.spring.configuration;


import com.swak.core.SwakConstants;
import com.swak.operatelog.*;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;


/**
 * 操作日志自动配置。
 * <p>
 * 所用 {@code @Bean} 方法一律声明为 {@code static}，且 {@link OperateLogInterceptor} 不注入业务方的
 * {@link OperateLogService}：{@code internalOperateLogAdvisor} 是 BeanPostProcessor，容器启动极早期
 * 就要创建，非 static 工厂方法会连带提前实例化本配置类，注入 service 还会把业务 bean 也拖进早期阶段
 * （既触发 BeanPostProcessorChecker 告警，也让业务 bean 失去被代理的机会）。
 * {@link OperateLogService} 由 {@link OperateLogInterceptor#afterSingletonsInstantiated()} 惰性获取。
 */
@Configuration
@ConditionalOnBean(value = OperateLogService.class)
public class SwakOperatelogAutoConfiguration {

    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    @ConditionalOnMissingBean(OperateLogAnnotationParser.class)
    public static OperateLogAnnotationParser operateLogAnnotationParser() {
        return new OperateLogAnnotationParser();
    }

    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    @ConditionalOnMissingBean(OperateLogOperationSource.class)
    public static OperateLogOperationSource operateLogOperationSource(OperateLogAnnotationParser operateLogAnnotationParser) {
        return new DefaultLogOperationSource(operateLogAnnotationParser);
    }

    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    @ConditionalOnMissingBean(OperateLogInterceptor.class)
    public static OperateLogInterceptor operateLogInterceptor(OperateLogOperationSource operateLogOperationSource) {
        OperateLogInterceptor interceptor = new OperateLogInterceptor();
        interceptor.setOperationSource(operateLogOperationSource);
        return interceptor;
    }

    @Bean("internalOperateLogAdvisor")
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    @ConditionalOnMissingBean(OperateLogAnnotationBeanPostProcessor.class)
    public static OperateLogAnnotationBeanPostProcessor internalOperateLogAdvisor(OperateLogInterceptor operateLogInterceptor) {
        OperateLogAnnotationBeanPostProcessor dynamicAdvisor = new OperateLogAnnotationBeanPostProcessor(
                operateLogInterceptor);
        dynamicAdvisor.setOrder(SwakConstants.ORDER_PRECEDENCE + 1000);
        return dynamicAdvisor;
    }
}
