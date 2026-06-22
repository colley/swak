
package com.swak.license.spring.configuration;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.swak.core.web.SwakMvcPatterns;
import com.swak.license.api.io.Source;
import com.swak.license.config.*;
import com.swak.license.nacos.NacosLicenseConfigAdapter;
import com.swak.license.spi.config.LicenseConfig;
import com.swak.license.spi.config.LicenseMvcConfig;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.util.*;

/**
 * @author colley
 */
@Configuration
@ConditionalOnBean(LicenseConfigurer.class)
public class SwakLicenseAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(LicenseConfigService.class)
    public LicenseConfigService licenseConfigService() {
        return new LicenseConfigServiceImpl();
    }

    @Bean
    public LicenseConfig licenseConfig(@Autowired(required = false) LicenseConfigurer licenseConfigurer,
                                       LicenseConfigService licenseConfigService, @Autowired(required = false) LicenseRegistry licenseRegistry) {
        LicenseConfig licenseConfig = new LicenseConfig();
        licenseConfig.setStorePass(licenseConfigurer.getStorePass());
        licenseConfig.setSubject(licenseConfigurer.getSubject());
        licenseConfig.setEdition(licenseConfigurer.getEdition());
        licenseConfig.setPublicKeys(publicKeySource(licenseConfigurer));
        licenseConfig.setLicense(licenseContentSource(licenseConfigurer, licenseConfigService));
        licenseConfig.setLicenseVerifyCallback(new LicenseRegistryCallBack(licenseRegistry));
        licenseConfig.setLicenseMvcConfig(licenseMvcConfig(licenseConfigurer));
        licenseConfig.setThrowErr(licenseConfigurer.isValidErrThrow());
        return licenseConfig;
    }

    private Source licenseContentSource(LicenseConfigurer licenseConfigurer, LicenseConfigService licenseConfigService) {
        String licenseContent = licenseConfigService.getConfig(licenseConfigurer.getLicenseDataId());
        return () -> () -> new ByteArrayInputStream(licenseContent.getBytes(Charsets.UTF_8));
    }

    private Source publicKeySource(LicenseConfigurer licenseConfigurer) {
        return () -> () -> Optional
                .of(new ClassPathResource(licenseConfigurer.getPublicKeyPath()).getInputStream())
                .orElseThrow(() -> new FileNotFoundException(licenseConfigurer.getPublicKeyPath()));
    }

    private SwakMvcPatterns licenseMvcConfig(LicenseConfigurer licenseConfigurer) {
        LicenseMvcConfig licenseMvcConfig = Optional.ofNullable(licenseConfigurer.getLicenseMvcConfig()).orElse(new LicenseMvcConfig());
        Set<String> includePatterns = Sets.newHashSet(Optional.ofNullable(licenseMvcConfig.getIncludePatterns()).orElse(new HashSet<>()));
        Set<String> excludePatterns = Sets.newHashSet(Optional.ofNullable(licenseMvcConfig.getExcludePatterns()).orElse(new HashSet<>()));
        if (CollectionUtils.isEmpty(includePatterns)) {
            includePatterns.add("/**");
        }
        if (CollectionUtils.isEmpty(excludePatterns)) {
            excludePatterns.add("/updated/**");
        }
        excludePatterns.add("/license/**");
        return new SwakMvcPatterns().addPathPatterns(Lists.newArrayList(includePatterns)).excludePathPatterns(Lists.newArrayList(excludePatterns));
    }

    @Configuration
    @ConditionalOnClass(name = "com.alibaba.cloud.nacos.NacosConfigManager")
    static class NacosLicenseConfiguration {
        @Bean
        @ConditionalOnMissingBean(NacosLicenseConfigAdapter.class)
        public NacosLicenseConfigAdapter nacosLicenseConfigAdapter(@Autowired(required = false)
                com.alibaba.cloud.nacos.NacosConfigManager nacosConfigManager) {
            NacosLicenseConfigAdapter nacosLicenseConfigAdapter = new NacosLicenseConfigAdapter();
            nacosLicenseConfigAdapter.setNacosConfigManager(nacosConfigManager);
            return nacosLicenseConfigAdapter;
        }
    }
}