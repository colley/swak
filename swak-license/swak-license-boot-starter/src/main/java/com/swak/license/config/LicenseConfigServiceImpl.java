package com.swak.license.config;

import com.swak.license.nacos.NacosLicenseConfigAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

@Slf4j
public class LicenseConfigServiceImpl implements LicenseConfigService {

    private final String DEFAULT_GROUP = "DEFAULT_GROUP";

    @Value(value = "${swak.license.source:}")
    private String defaultLicenseSource;

    // 【核心】使用 required=false，如果没有 Nacos，这里就是 null，不会报错
    @Autowired(required = false)
    private NacosLicenseConfigAdapter nacosLicenseConfigAdapter;

    @Override
    public String getConfig(String dataId) {
        return getConfig(dataId, DEFAULT_GROUP);
    }

    @Override
    public String getConfig(String dataId, String group) {
        // 如果有 Nacos 适配器，优先走 Nacos
        if (nacosLicenseConfigAdapter != null) {
            String config = nacosLicenseConfigAdapter.getConfig(dataId, group);
            if (config != null) {
                return config;
            }
        }
        // 没有 Nacos，或者 Nacos 里没配，优雅降级到本地默认值
        log.info("未启用 Nacos 或未找到配置 [{}]，使用本地默认 License 源", dataId);
        return defaultLicenseSource;
    }
}
