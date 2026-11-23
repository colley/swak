package com.swak.test.autoconfigure;

import com.swak.autoconfigure.spring.configuration.SwakAutoConfiguration;
import com.swak.core.SwakConstants;
import com.swak.core.support.SpringBeanFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.servlet.LocaleResolver;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 主 starter 的自动配置。
 * <p>
 * 它带 {@code @ComponentScan("com.swak")} + {@code @EnableWebMvc}，是整个框架的装配入口，
 * 用 web 上下文跑一遍能覆盖「Boot 4 下 @EnableWebMvc 与组件扫描是否还能正常启动」。
 */
class SwakAutoConfigurationTest {

    private final WebApplicationContextRunner runner = new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(SwakAutoConfiguration.class))
            // 组件扫描会加载 ValidatorInitializedAware，它 @Resource 注入 jakarta.validation.Validator
            .withBean(jakarta.validation.Validator.class,
                    () -> jakarta.validation.Validation.buildDefaultValidatorFactory().getValidator());

    @Test
    @DisplayName("web 上下文可启动，并暴露核心 Bean")
    void contextLoadsWithCoreBeans() {
        runner.run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context).hasSingleBean(SpringBeanFactory.class);
            assertThat(context).hasBean("swakLocaleResolver");
            assertThat(context)
                    .getBean("swakLocaleResolver")
                    .isInstanceOf(LocaleResolver.class);
            assertThat(context).hasBean("asyncThreadPoolMonitor");
        });
    }

    @Test
    @DisplayName("组件扫描范围仍是 com.swak 全包")
    void componentScanBasePackage() {
        ComponentScan scan = SwakAutoConfiguration.class.getAnnotation(ComponentScan.class);
        assertThat(scan).isNotNull();
        assertThat(Arrays.asList(scan.basePackages())).containsExactly(SwakConstants.BASE_PACKAGE);
    }
}
