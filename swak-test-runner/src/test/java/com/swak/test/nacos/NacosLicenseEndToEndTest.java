package com.swak.test.nacos;

import com.alibaba.cloud.nacos.NacosConfigManager;
import com.alibaba.cloud.nacos.NacosConfigProperties;
import com.swak.license.config.LicenseConfigServiceImpl;
import com.swak.license.nacos.NacosLicenseConfigAdapter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * License 读取链路接真实 Nacos 的端到端验证。
 * <p>
 * 与 SCA 的 {@code spring.config.import} 不同，{@link NacosLicenseConfigAdapter} 自己注册了
 * 监听器（{@code ConfigService.addListener}），所以 License 的热更新不依赖 SCA 那套刷新机制
 * —— 后者在 2025.1.0.0 上对 config-data 导入的属性源是失效的，见 {@link NacosConfigDataImportTest}。
 */
@EnabledIf("nacosAvailable")
class NacosLicenseEndToEndTest {

    private static final String LOCAL_DEFAULT = "local-default-license";

    private NacosConfigManager nacosConfigManager;

    private NacosLicenseConfigAdapter adapter;

    private LicenseConfigServiceImpl licenseConfigService;

    static boolean nacosAvailable() {
        return NacosSupport.nacosAvailable();
    }

    @BeforeEach
    void setUp() {
        NacosConfigProperties properties = new NacosConfigProperties();
        properties.setServerAddr(NacosSupport.SERVER_ADDR);
        nacosConfigManager = new NacosConfigManager(properties);

        adapter = new NacosLicenseConfigAdapter();
        adapter.setNacosConfigManager(nacosConfigManager);

        licenseConfigService = new LicenseConfigServiceImpl();
        ReflectionTestUtils.setField(licenseConfigService, "nacosLicenseConfigAdapter", adapter);
        ReflectionTestUtils.setField(licenseConfigService, "defaultLicenseSource", LOCAL_DEFAULT);
    }

    @AfterEach
    void tearDown() throws Exception {
        nacosConfigManager.getConfigService().shutDown();
    }

    @Test
    @DisplayName("License 内容能从真实 Nacos 读到，读不到时降级到本地默认值")
    void readsLicenseFromNacos() throws Exception {
        String dataId = "swak-license-" + UUID.randomUUID();
        String content = "license-payload-" + UUID.randomUUID();

        NacosSupport.publish(dataId, content);

        // 发布后要等 dump 刷新缓存，轮询到可读为止
        String actual = null;
        for (int i = 0; i < 20; i++) {
            actual = licenseConfigService.getConfig(dataId);
            if (content.equals(actual)) {
                break;
            }
            Thread.sleep(300L);
        }
        assertThat(actual).isEqualTo(content);

        // 不存在的 dataId 走本地默认值，不抛异常
        assertThat(licenseConfigService.getConfig("no-such-" + UUID.randomUUID())).isEqualTo(LOCAL_DEFAULT);
    }
}
