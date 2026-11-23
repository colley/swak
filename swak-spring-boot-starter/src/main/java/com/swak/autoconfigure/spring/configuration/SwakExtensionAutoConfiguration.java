package com.swak.autoconfigure.spring.configuration;

import com.swak.core.extension.ExtensionFinder;
import com.swak.core.extension.executor.DefaultExtensionExecutor;
import com.swak.core.extension.executor.ExtensionExecutor;
import com.swak.core.extension.repository.AnnotationExtensionFinder;
import com.swak.core.extension.repository.DefaultExtensionRepository;
import com.swak.core.extension.repository.ExtensionRepository;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;


/**
 * Extension Auto Configuration
 * <p>
 * 这些 bean 会被 {@code SwakAnnotationPostProcessor}（BeanPostProcessor）在容器启动极早期经
 * {@code ExtensionPostProcessor} 间接拉起来，因此：工厂方法声明为 {@code static}（避免提前实例化配置类），
 * 并标记为 {@link BeanDefinition#ROLE_INFRASTRUCTURE}（它们无需被代理，声明为基础设施可避免
 * BeanPostProcessorChecker 告警）。
 *
 * @author colley.ma
 * @since  2022/06/15
 */
@Configuration(proxyBeanMethods = false)
public class SwakExtensionAutoConfiguration {

    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    @ConditionalOnMissingBean(ExtensionRepository.class)
    public static ExtensionRepository extensionRepository() {
        return new DefaultExtensionRepository();
    }

    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    @ConditionalOnMissingBean(ExtensionFinder.class)
    public static ExtensionFinder extensionFinder() {
        return new AnnotationExtensionFinder();
    }

    @Bean
    @ConditionalOnMissingBean(ExtensionExecutor.class)
    public static ExtensionExecutor extensionExecutor() {
        return new DefaultExtensionExecutor();
    }

}
