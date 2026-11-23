package com.swak.test.nacos;

import com.alibaba.cloud.nacos.NacosConfigManager;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import com.swak.core.support.SpringBeanFactory;
import com.swak.license.api.io.Source;
import com.swak.license.nacos.NacosLicenseConfigAdapter;
import com.swak.license.spi.config.LicenseVerifyService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.support.GenericApplicationContext;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link NacosLicenseConfigAdapter} 的接线测试（不依赖真实 Nacos）。
 * <p>
 * 覆盖点：读取缓存、监听器只注册一次、配置变更触发热重装、Nacos 异常时优雅降级。
 * 真实客户端与服务端的联通性由 {@link NacosConfigIntegrationTest} 负责。
 */
class NacosLicenseConfigAdapterTest {

    private static final String DATA_ID = "swak-license";

    private static final String GROUP = "DEFAULT_GROUP";

    private ConfigService configService;

    private NacosLicenseConfigAdapter adapter;

    private GenericApplicationContext context;

    private LicenseVerifyService verifyService;

    private static String b64(String raw) {
        return Base64.getEncoder().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }

    @BeforeEach
    void setUp() {
        configService = mock(ConfigService.class);
        NacosConfigManager manager = mock(NacosConfigManager.class);
        when(manager.getConfigService()).thenReturn(configService);
        adapter = new NacosLicenseConfigAdapter();
        adapter.setNacosConfigManager(manager);

        verifyService = mock(LicenseVerifyService.class);
        context = new GenericApplicationContext();
        context.registerBean(LicenseVerifyService.class, () -> verifyService);
        context.refresh();
        new SpringBeanFactory().setApplicationContext(context);
    }

    @AfterEach
    void tearDown() {
        context.close();
    }

    @Test
    @DisplayName("从 Nacos 读到内容后走本地缓存，重复调用不再打远端")
    void cachesConfig() throws NacosException {
        when(configService.getConfig(DATA_ID, GROUP, 5000L)).thenReturn(b64("lic-v1"));

        assertThat(adapter.getConfig(DATA_ID, GROUP)).isEqualTo(b64("lic-v1"));
        assertThat(adapter.getConfig(DATA_ID, GROUP)).isEqualTo(b64("lic-v1"));

        verify(configService, times(1)).getConfig(DATA_ID, GROUP, 5000L);
    }

    @Test
    @DisplayName("监听器只注册一次，重复 getConfig 不会重复注册")
    void registersListenerOnce() throws NacosException {
        when(configService.getConfig(DATA_ID, GROUP, 5000L)).thenReturn(b64("lic-v1"));

        adapter.getConfig(DATA_ID, GROUP);
        adapter.getConfig(DATA_ID, GROUP);
        adapter.getConfig(DATA_ID, GROUP);

        verify(configService, times(1)).addListener(eq(DATA_ID), eq(GROUP), any(Listener.class));
    }

    @Test
    @DisplayName("Nacos 配置变更时把 base64 内容解码后热重装 License")
    void configChangeTriggersReinstall() throws Exception {
        when(configService.getConfig(DATA_ID, GROUP, 5000L)).thenReturn(b64("lic-v1"));
        adapter.getConfig(DATA_ID, GROUP);

        ArgumentCaptor<Listener> captor = ArgumentCaptor.forClass(Listener.class);
        verify(configService).addListener(eq(DATA_ID), eq(GROUP), captor.capture());

        captor.getValue().receiveConfigInfo(b64("lic-v2"));

        ArgumentCaptor<Source> sourceCaptor = ArgumentCaptor.forClass(Source.class);
        verify(verifyService, times(1)).reinstall(sourceCaptor.capture());
        String reinstalled = sourceCaptor.getValue()
                .applyReader(in -> new String(in.readAllBytes(), StandardCharsets.UTF_8));
        assertThat(reinstalled).isEqualTo("lic-v2");
        // 缓存同步更新
        assertThat(adapter.getConfig(DATA_ID, GROUP)).isEqualTo(b64("lic-v2"));
    }

    @Test
    @DisplayName("配置内容未变化时不触发热重装")
    void unchangedConfigDoesNotReinstall() throws Exception {
        when(configService.getConfig(DATA_ID, GROUP, 5000L)).thenReturn(b64("lic-v1"));
        adapter.getConfig(DATA_ID, GROUP);

        ArgumentCaptor<Listener> captor = ArgumentCaptor.forClass(Listener.class);
        verify(configService).addListener(eq(DATA_ID), eq(GROUP), captor.capture());

        captor.getValue().receiveConfigInfo(b64("lic-v1"));

        verify(verifyService, never()).reinstall(any());
    }

    @Test
    @DisplayName("Nacos 抛异常时返回 null，由调用方降级到本地 License 源")
    void nacosFailureDegradesToNull() throws NacosException {
        when(configService.getConfig(DATA_ID, GROUP, 5000L)).thenThrow(new NacosException(500, "boom"));

        assertThat(adapter.getConfig(DATA_ID, GROUP)).isNull();
    }

    @Test
    @DisplayName("监听器注册失败不影响本次配置返回")
    void listenerRegistrationFailureDoesNotBreakRead() throws NacosException {
        when(configService.getConfig(DATA_ID, GROUP, 5000L)).thenReturn(b64("lic-v1"));
        org.mockito.Mockito.doThrow(new NacosException(500, "boom"))
                .when(configService).addListener(eq(DATA_ID), eq(GROUP), any(Listener.class));

        assertThat(adapter.getConfig(DATA_ID, GROUP)).isEqualTo(b64("lic-v1"));
        verify(configService, times(1)).getConfig(any(), any(), anyLong());
    }
}
