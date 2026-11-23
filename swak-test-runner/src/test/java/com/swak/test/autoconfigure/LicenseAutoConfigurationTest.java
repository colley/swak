package com.swak.test.autoconfigure;

import com.swak.license.config.LicenseConfigService;
import com.swak.license.config.LicenseConfigurer;
import com.swak.license.spi.config.LicenseConfig;
import com.swak.license.spring.configuration.SwakLicenseAutoConfiguration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * License 相关配置以使用方实现 LicenseConfigurer 为前提。
 */
class LicenseAutoConfigurationTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(SwakLicenseAutoConfiguration.class));

    @Test
    @DisplayName("没有 LicenseConfigurer 时不装配任何 License 组件")
    void withoutConfigurer() {
        runner.run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context).doesNotHaveBean(LicenseConfig.class);
            assertThat(context).doesNotHaveBean(LicenseConfigService.class);
        });
    }

    @Test
    @DisplayName("存在 LicenseConfigurer 时装配 LicenseConfigService 与 LicenseConfig")
    void withConfigurer() {
        runner.withBean(LicenseConfigurer.class, () -> mock(LicenseConfigurer.class)).run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context).hasSingleBean(LicenseConfigService.class);
            assertThat(context).hasSingleBean(LicenseConfig.class);
        });
    }
}
