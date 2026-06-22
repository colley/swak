package com.swak.license.nacos;

import com.alibaba.cloud.nacos.NacosConfigManager;
import com.alibaba.nacos.api.config.listener.Listener;
import com.google.common.base.Charsets;
import com.swak.core.support.SpringBeanFactory;
import com.swak.license.api.LicenseManagementException;
import com.swak.license.api.io.Source;
import com.swak.license.api.io.bios.BIOS;
import com.swak.license.spi.config.LicenseVerifyService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.ByteArrayInputStream;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;

/**
 * 仅当环境中有 Nacos 时才会被加载，内部可安全使用 Nacos API
 */
@Slf4j
public class NacosLicenseConfigAdapter {
    private final int TIMEOUT_MS = 5000;

    // 通过构造器注入（Spring 会自动找到容器里的 NacosConfigManager Bean）
    private  NacosConfigManager nacosConfigManager;
    private final Map<String, String> dataCache = new ConcurrentHashMap<>();
    private final Map<String, Listener> dataListenerCache = new ConcurrentHashMap<>();


    /**
     * 从 Nacos 获取配置并注册监听器
     */
    public String getConfig(String dataId, String group) {
        try {
            String data = dataCache.get(dataId);
            if (StringUtils.isEmpty(data)) {
                data = nacosConfigManager.getConfigService().getConfig(dataId, group, TIMEOUT_MS);
                dataCache.put(dataId, data);
            }

            // 注册动态刷新监听器
            registerListenerIfAbsent(dataId, group);
            return data;
        } catch (Exception e) {
            log.warn("从 Nacos 获取 License 配置异常: {}", dataId, e);
            return null;
        }
    }

    private void registerListenerIfAbsent(String dataId, String group) {
        if (!dataListenerCache.containsKey(dataId)) {
            synchronized (this) {
                if (!dataListenerCache.containsKey(dataId)) {
                    Listener listener = new Listener() {
                        @Override
                        public Executor getExecutor() {
                            return ForkJoinPool.commonPool();
                        }

                        @Override
                        public void receiveConfigInfo(String configInfo) {
                            String oldConfigInfo = dataCache.get(dataId);
                            if (StringUtils.isNotEmpty(oldConfigInfo) && !StringUtils.equals(oldConfigInfo, configInfo)) {
                                triggerLicenseReinstall(configInfo);
                            }
                            dataCache.put(dataId, configInfo);
                        }
                    };

                    try {
                        nacosConfigManager.getConfigService().addListener(dataId, group, listener);
                        dataListenerCache.put(dataId, listener);
                    } catch (Exception e) {
                        log.error("注册 Nacos 监听器失败", e);
                    }
                }
            }
        }
    }

    private void triggerLicenseReinstall(String licenseContent) {
        LicenseVerifyService verifyService = SpringBeanFactory.getBean(LicenseVerifyService.class);
        if (Objects.nonNull(verifyService)) {
            try {
                Source source = () -> () -> new ByteArrayInputStream(licenseContent.getBytes(Charsets.UTF_8));
                verifyService.reinstall(source.map(BIOS.base64()));
            } catch (LicenseManagementException e) {
                log.error("License 热更新重装失败", e);
            }
        }
    }

    public void setNacosConfigManager(NacosConfigManager nacosConfigManager) {
        this.nacosConfigManager = nacosConfigManager;
    }
}