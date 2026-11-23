package com.swak.test.nacos;

import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * nacos-client 3.1.1 ↔ 真实 Nacos 服务端的联通性验证。
 * <p>
 * 只验证客户端协议（HTTP 读写 + 配置变更推送）在 JDK 17 下可用；
 * Spring 侧的 {@code spring.config.import} 集成见 {@link NacosConfigDataImportTest}。
 * <p>
 * 没有可用的 Nacos 时整类跳过，不阻塞构建。
 */
@EnabledIf("nacosAvailable")
class NacosClientWireTest {

    static boolean nacosAvailable() {
        return NacosSupport.nacosAvailable();
    }

    @Test
    @DisplayName("客户端可以发布并读回配置（HTTP 通道）")
    void publishAndRead() throws Exception {
        String dataId = NacosSupport.newDataId("swak-wire-");
        ConfigService configService = NacosSupport.newConfigService();
        try {
            NacosSupport.publishAndAwait(configService, dataId, "swak.wire=v1");

            assertThat(configService.getConfig(dataId, NacosSupport.GROUP, 5000L)).isEqualTo("swak.wire=v1");
        } finally {
            configService.shutDown();
        }
    }

    @Test
    @DisplayName("配置变更能推送到监听器")
    void listenerReceivesChange() throws Exception {
        String dataId = NacosSupport.newDataId("swak-wire-");
        ConfigService configService = NacosSupport.newConfigService();
        CountDownLatch latch = new CountDownLatch(1);
        try {
            NacosSupport.publishAndAwait(configService, dataId, "swak.wire=v1");

            configService.addListener(dataId, NacosSupport.GROUP, new Listener() {
                @Override
                public Executor getExecutor() {
                    return null;
                }

                @Override
                public void receiveConfigInfo(String configInfo) {
                    if (configInfo != null && configInfo.contains("v2")) {
                        latch.countDown();
                    }
                }
            });

            // listen 注册是异步的；且服务端对「内容相同」的发布不会再发通知（md5 未变），
            // 所以每次重试都换一个内容，保证每轮都能触发一次真实的变更事件。
            for (int i = 0; i < 10 && latch.getCount() > 0; i++) {
                NacosSupport.publish(dataId, "swak.wire=v2-" + i);
                latch.await(2, TimeUnit.SECONDS);
            }

            assertThat(latch.getCount()).as("应收到 Nacos 的配置变更推送").isZero();
        } finally {
            configService.shutDown();
        }
    }
}
