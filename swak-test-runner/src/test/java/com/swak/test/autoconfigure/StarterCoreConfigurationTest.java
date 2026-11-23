package com.swak.test.autoconfigure;

import com.swak.autoconfigure.config.AsyncConfigProperties;
import com.swak.autoconfigure.config.SwakAsyncConfigurer;
import com.swak.autoconfigure.resolver.ContextClosedThreadPoolHandler;
import com.swak.autoconfigure.spring.configuration.AsyncThreadPoolConfiguration;
import com.swak.autoconfigure.spring.configuration.SwakExtensionAutoConfiguration;
import com.swak.core.extension.ExtensionFinder;
import com.swak.core.extension.executor.ExtensionExecutor;
import com.swak.core.extension.repository.ExtensionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * swak-spring-boot-starter 里两个轻量自动配置：
 * 异步线程池（以业务方实现 AsyncConfigProperties 为前提）与扩展点容器（默认即可用）。
 */
class StarterCoreConfigurationTest {

    private final ApplicationContextRunner asyncRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(AsyncThreadPoolConfiguration.class));

    private final ApplicationContextRunner extensionRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(SwakExtensionAutoConfiguration.class));

    @Test
    @DisplayName("未实现 AsyncConfigProperties 时不装配异步线程池")
    void noAsyncProperties() {
        asyncRunner.run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context).doesNotHaveBean("swakAsyncTaskExecutor");
            assertThat(context).doesNotHaveBean(SwakAsyncConfigurer.class);
        });
    }

    @Test
    @DisplayName("实现 AsyncConfigProperties 后装配线程池与关闭钩子")
    void withAsyncProperties() {
        asyncRunner.withBean(AsyncConfigProperties.class, () -> new AsyncConfigProperties() {
            @Override
            public Integer getCorePoolSize() {
                return 2;
            }

            @Override
            public Integer getMaxPoolSize() {
                return 4;
            }

            @Override
            public Integer getQueueCapacity() {
                return 16;
            }
        }).run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context).hasSingleBean(ThreadPoolTaskExecutor.class);
            assertThat(context.getBean("swakAsyncTaskExecutor", ThreadPoolTaskExecutor.class)
                    .getCorePoolSize()).isEqualTo(2);
            assertThat(context).hasSingleBean(SwakAsyncConfigurer.class);
            assertThat(context).hasSingleBean(ContextClosedThreadPoolHandler.class);
        });
    }

    @Test
    @DisplayName("扩展点容器默认装配 repository / finder / executor 三个 Bean")
    void extensionContainerIsAlwaysAvailable() {
        extensionRunner.run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context).hasSingleBean(ExtensionRepository.class);
            assertThat(context).hasSingleBean(ExtensionFinder.class);
            assertThat(context).hasSingleBean(ExtensionExecutor.class);
        });
    }
}
