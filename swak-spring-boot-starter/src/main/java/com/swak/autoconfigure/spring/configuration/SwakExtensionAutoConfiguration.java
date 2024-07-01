package com.swak.autoconfigure.spring.configuration;

import com.swak.core.extension.ExtensionFinder;
import com.swak.core.extension.executor.DefaultExtensionExecutor;
import com.swak.core.extension.executor.ExtensionExecutor;
import com.swak.core.extension.repository.AnnotationExtensionFinder;
import com.swak.core.extension.repository.DefaultExtensionRepository;
import com.swak.core.extension.repository.ExtensionRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * Extension Auto Configuration
 *
 * @author colley.ma
 * @since  2022/06/15
 */
@Configuration(proxyBeanMethods = false)
public class SwakExtensionAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(ExtensionRepository.class)
    public ExtensionRepository extensionRepository() {
        return new DefaultExtensionRepository();
    }

    @Bean
    @ConditionalOnMissingBean(ExtensionFinder.class)
    public ExtensionFinder extensionFinder() {
        return new AnnotationExtensionFinder();
    }

    @Bean
    @ConditionalOnMissingBean(ExtensionExecutor.class)
    public ExtensionExecutor extensionExecutor() {
        return new DefaultExtensionExecutor();
    }

}
