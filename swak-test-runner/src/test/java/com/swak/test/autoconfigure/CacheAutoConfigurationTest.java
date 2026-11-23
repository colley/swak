package com.swak.test.autoconfigure;

import com.swak.cache.manager.ExtCacheManager;
import com.swak.cache.spring.configuration.SwakCacheAutoConfiguration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.cache.CacheManager;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * swak.cache.enabled 开关的开关语义。
 * <p>
 * 这里同时验证 {@code @Import} 进来的 Redisson/Redis 两个配置在缺少属性时不会误激活
 * —— Boot 4 拆分了 autoconfigure 模块，条件一旦判错就会在启动期尝试连 Redis。
 */
class CacheAutoConfigurationTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(SwakCacheAutoConfiguration.class));

    @Test
    @DisplayName("未配置 swak.cache.enabled 时不装配任何缓存组件")
    void disabledByDefault() {
        runner.run(context -> {
            assertThat(context).doesNotHaveBean(CacheManager.class);
            assertThat(context).doesNotHaveBean(SwakCacheAutoConfiguration.class);
            assertThat(context).doesNotHaveBean("cacheManager");
        });
    }

    @Test
    @DisplayName("swak.cache.enabled=true 时提供缓存管理器（无 Redis 配置则回落 Caffeine）")
    void enabledRegistersCacheManager() {
        runner.withPropertyValues("swak.cache.enabled=true").run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context).hasSingleBean(CacheManager.class);
            assertThat(context.getBean("cacheManager", CacheManager.class))
                    .isInstanceOf(ExtCacheManager.class);
        });
    }

    @Test
    @DisplayName("swak.cache.enabled=false 显式关闭时不装配缓存组件")
    void explicitlyDisabled() {
        runner.withPropertyValues("swak.cache.enabled=false")
                .run(context -> assertThat(context).doesNotHaveBean(CacheManager.class));
    }
}
