package com.swak.test.nacos;

import com.alibaba.cloud.nacos.NacosConfigAutoConfiguration;
import com.alibaba.cloud.nacos.NacosConfigSpringCloudAutoConfiguration;
import com.alibaba.nacos.api.config.ConfigService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 验证 SCA 2025.1.0.0 在 <b>完整装配</b> 下是否本来就能热刷新。
 * <p>
 * 关键点：{@code NacosConfigRefreshEvent} 的消费者不在实现包
 * {@code spring-alibaba-nacos-config}，而在 starter 包
 * {@code spring-cloud-starter-alibaba-nacos-config} 里的
 * {@code NacosConfigRefreshEventListener}（由
 * {@code NacosConfigSpringCloudAutoConfiguration} 注册）。它把 Nacos 自有事件翻译成
 * {@code RefreshEvent}，再交给 Spring Cloud 的 {@code ContextRefresher} 重跑 config data。
 * <p>
 * 早先的用例只导入了 {@code NacosConfigAutoConfiguration}，消费者没进容器，
 * 得出的「上游链路断了」结论需要重新确认。
 */
@EnabledIf("nacosAvailable")
class NacosNativeRefreshTest {

    static boolean nacosAvailable() {
        return NacosSupport.nacosAvailable();
    }

    /**
     * 模拟真实应用的装配：nacos 核心自动配置 + starter 的 Spring Cloud 集成层 + 刷新基础设施。
     */
    @Configuration
    @ImportAutoConfiguration({ NacosConfigAutoConfiguration.class,
            NacosConfigSpringCloudAutoConfiguration.class, RefreshAutoConfiguration.class })
    static class FullyWiredApp {
    }

    @Test
    @DisplayName("完整装配下，Nacos 自有事件被翻译成 RefreshEvent，Environment 能拿到新值")
    void nativeRefreshWorksWithFullWiring() throws Exception {
        String dataId = NacosSupport.newDataId("swak-native-");
        ConfigService probe = NacosSupport.newConfigService();
        try {
            NacosSupport.publishAndAwait(probe, dataId, "swak.nacos.native=v1");

            try (ConfigurableApplicationContext context = boot(FullyWiredApp.class, dataId)) {
                // SCA 给这个 @Bean 显式命名，方法名与 bean 名不同
                assertThat(context.containsBean("nacosConfigSpringCloudRefreshEventListener"))
                        .as("starter 里的 NacosConfigRefreshEventListener 应在容器中")
                        .isTrue();
                assertThat(context.getEnvironment().getProperty("swak.nacos.native")).isEqualTo("v1");

                String current = null;
                for (int i = 0; i < 8; i++) {
                    NacosSupport.publish(dataId, "swak.nacos.native=v2-" + i);
                    Thread.sleep(1000L);
                    current = context.getEnvironment().getProperty("swak.nacos.native");
                    if (current != null && current.startsWith("v2-")) {
                        break;
                    }
                }

                assertThat(current)
                        .as("完整装配下 ContextRefresher 重跑 config data，应读到新值")
                        .startsWith("v2-");
            }
        } finally {
            probe.shutDown();
        }
    }

    @Test
    @DisplayName("对照：只装配 NacosConfigAutoConfiguration 时事件无人消费，Environment 保持旧值")
    void withoutSpringCloudIntegrationEventIsNotConsumed() throws Exception {
        String dataId = NacosSupport.newDataId("swak-native-partial-");
        ConfigService probe = NacosSupport.newConfigService();
        try {
            NacosSupport.publishAndAwait(probe, dataId, "swak.nacos.partial=v1");

            try (ConfigurableApplicationContext context = boot(PartialApp.class, dataId)) {
                assertThat(context.containsBean("nacosConfigSpringCloudRefreshEventListener")).isFalse();
                assertThat(context.getEnvironment().getProperty("swak.nacos.partial")).isEqualTo("v1");

                for (int i = 0; i < 5; i++) {
                    NacosSupport.publish(dataId, "swak.nacos.partial=v2-" + i);
                    Thread.sleep(1000L);
                }

                assertThat(context.getEnvironment().getProperty("swak.nacos.partial"))
                        .as("缺消费者时不会刷新，这就是早先用例的装配")
                        .isEqualTo("v1");
            }
        } finally {
            probe.shutDown();
        }
    }

    @Configuration
    @ImportAutoConfiguration(NacosConfigAutoConfiguration.class)
    static class PartialApp {
    }

    private ConfigurableApplicationContext boot(Class<?> app, String dataId) {
        SpringApplication application = new SpringApplication(app);
        application.setWebApplicationType(WebApplicationType.NONE);
        application.setBannerMode(Banner.Mode.OFF);
        return application.run(
                "--spring.cloud.nacos.config.server-addr=" + NacosSupport.SERVER_ADDR,
                "--spring.cloud.nacos.config.import-check.enabled=false",
                "--spring.config.import=nacos:" + dataId + "?group=" + NacosSupport.GROUP + "&refreshEnabled=true");
    }
}
