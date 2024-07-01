package com.swak.operatelog.spring.configuration;


import com.swak.core.SwakConstants;
import com.swak.operatelog.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;


@Configuration
@ConditionalOnBean(value = OperateLogService.class)
public class SwakOperatelogAutoConfiguration {

    @Autowired(required = false)
    private OperateLogService operateLogService;

    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    @ConditionalOnMissingBean(OperateLogAnnotationParser.class)
    public OperateLogAnnotationParser operateLogAnnotationParser() {
        return new OperateLogAnnotationParser();
    }

    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    @ConditionalOnMissingBean(OperateLogOperationSource.class)
    public OperateLogOperationSource operateLogOperationSource(OperateLogAnnotationParser operateLogAnnotationParser) {
        OperateLogOperationSource operationSource = new DefaultLogOperationSource(operateLogAnnotationParser);
        return operationSource;
    }

    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    @ConditionalOnMissingBean(OperateLogInterceptor.class)
    public OperateLogInterceptor operateLogInterceptor(OperateLogOperationSource operateLogOperationSource) {
        OperateLogInterceptor interceptor = new OperateLogInterceptor(operateLogService);
        interceptor.setOperationSource(operateLogOperationSource);
        return interceptor;
    }

    @Bean("internalOperateLogAdvisor")
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    @ConditionalOnMissingBean(OperateLogAnnotationBeanPostProcessor.class)
    public OperateLogAnnotationBeanPostProcessor internalOperateLogAdvisor(OperateLogInterceptor operateLogInterceptor) {
        OperateLogAnnotationBeanPostProcessor dynamicAdvisor = new OperateLogAnnotationBeanPostProcessor(
                operateLogInterceptor);
        dynamicAdvisor.setOrder(SwakConstants.ORDER_PRECEDENCE + 1000);
        return dynamicAdvisor;
    }
}
