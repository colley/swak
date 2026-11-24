package com.swak.test.nacos;

import com.alibaba.cloud.nacos.NacosConfigAutoConfiguration;
import com.alibaba.nacos.api.config.ConfigService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Spring Cloud Alibaba 2025.1.0.0 的 Nacos 配置导入在 Spring Boot 4.1.1 上的真实联通验证。
 * <p>
 * SCA 2025.1.0.0 官方对齐的是 Boot 4.0.0，这里是 4.1.1，风险点是
 * {@code spring.config.import=nacos:...} 这条 ConfigData 链路（由 spring.factories 注册的
 * {@code ConfigDataLocationResolver} 提供）。编译期完全看不出来，只能起真实上下文。
 */
@EnabledIf("nacosAvailable")
class NacosConfigDataImportTest {

    static boolean nacosAvailable() {
        return NacosSupport.nacosAvailable();
    }

    /**
     * 这里只装配 SCA 的 Nacos 核心自动配置：{@code NacosContextRefresher}（负责给导入的 dataId
     * 注册监听、发布变更事件）由它提供。
     * <p>
     * 注意它<b>不包含</b> starter 里的 {@code NacosConfigSpringCloudAutoConfiguration}
     * （负责把 Nacos 自有事件翻译成 Spring Cloud 的 {@code RefreshEvent}），所以本类只验证
     * 「导入是否生效」。热刷新的正反对照见 {@link NacosNativeRefreshTest}。
     * <p>
     * 这里不用 {@code @EnableAutoConfiguration}，否则会把 swak 的 WebMvc 组件也拉进来，
     * 非 Web 上下文里会因缺少 ServletContext 启动失败。
     */
    @Configuration
    @ImportAutoConfiguration(NacosConfigAutoConfiguration.class)
    static class App {
    }

    private ConfigurableApplicationContext bootWith(String dataId) {
        SpringApplication application = new SpringApplication(App.class);
        application.setWebApplicationType(WebApplicationType.NONE);
        application.setBannerMode(Banner.Mode.OFF);
        return application.run(
                "--spring.cloud.nacos.config.server-addr=" + NacosSupport.SERVER_ADDR,
                "--spring.cloud.nacos.config.import-check.enabled=false",
                "--spring.config.import=nacos:" + dataId + "?group=" + NacosSupport.GROUP + "&refreshEnabled=true");
    }

    @Test
    @DisplayName("spring.config.import=nacos: 能在 Boot 4.1.1 上把远端配置注入 Environment")
    void importsRemoteConfig() throws Exception {
        String dataId = NacosSupport.newDataId("swak-import-");
        ConfigService probe = NacosSupport.newConfigService();
        try {
            NacosSupport.publishAndAwait(probe, dataId, "swak.nacos.imported=from-nacos");

            try (ConfigurableApplicationContext context = bootWith(dataId)) {
                assertThat(context.getEnvironment().getProperty("swak.nacos.imported")).isEqualTo("from-nacos");
            }
        } finally {
            probe.shutDown();
        }
    }
}
