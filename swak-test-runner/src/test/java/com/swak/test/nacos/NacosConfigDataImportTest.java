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

import java.util.concurrent.atomic.AtomicInteger;

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
     * 只装配 SCA 的 Nacos 自动配置：{@code NacosContextRefresher}（负责给导入的 dataId 注册监听、
     * 发布变更事件）由它提供。
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

    /**
     * 固化 SCA 2025.1.0.0 在 config-data 导入场景下的<b>已知缺陷</b>：配置变更能推送到客户端、
     * 也会发布 {@code NacosConfigRefreshEvent}，但 Environment 里的值不会被替换。
     * <p>
     * 根因（已核实）：{@code NacosConfigDataLoader} 把配置源放进 Environment 时，名字是
     * {@code group@dataId}（本例 {@code DEFAULT_GROUP@xxx.properties}），且被 Boot 包装成
     * {@code OriginTrackedMapPropertySource}；而负责刷新替换的
     * {@code NacosPropertySourceRefreshListener} 是按 {@code dataId + "," + group} 去
     * {@code MutablePropertySources#get}，并要求取到 {@code NacosPropertySource} 实例。
     * 键和类型都对不上，于是直接返回，配置不会被替换。
     * <p>
     * 一旦 SCA 修掉这个问题，这条用例会失败，届时把断言改成正向的即可。
     */
    @Test
    @DisplayName("已知缺陷：变更事件能到达，但导入的属性源不会被替换（Environment 保持旧值）")
    void refreshEventArrivesButPropertySourceIsNotReplaced() throws Exception {
        String dataId = NacosSupport.newDataId("swak-import-");
        ConfigService probe = NacosSupport.newConfigService();
        try {
            NacosSupport.publishAndAwait(probe, dataId, "swak.nacos.dynamic=v1");

            try (ConfigurableApplicationContext context = bootWith(dataId)) {
                assertThat(context.getEnvironment().getProperty("swak.nacos.dynamic")).isEqualTo("v1");

                AtomicInteger refreshEvents = new AtomicInteger();
                context.addApplicationListener(event -> {
                    if (event.getClass().getSimpleName().equals("NacosConfigRefreshEvent")) {
                        refreshEvents.incrementAndGet();
                    }
                });

                // 每轮换一个内容：服务端对相同内容的发布不会再发通知（md5 未变）
                for (int i = 0; i < 5 && refreshEvents.get() == 0; i++) {
                    NacosSupport.publish(dataId, "swak.nacos.dynamic=v2-" + i);
                    Thread.sleep(1000L);
                }

                assertThat(refreshEvents.get())
                        .as("变更通知应到达客户端并发布 NacosConfigRefreshEvent")
                        .isPositive();
                assertThat(context.getEnvironment().getProperty("swak.nacos.dynamic"))
                        .as("SCA 2025.1.0.0 不会替换已导入的属性源，Environment 仍是旧值")
                        .isEqualTo("v1");
            }
        } finally {
            probe.shutDown();
        }
    }
}
