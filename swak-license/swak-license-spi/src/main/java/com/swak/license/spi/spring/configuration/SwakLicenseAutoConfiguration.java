
package com.swak.license.spi.spring.configuration;

import com.swak.license.spi.config.LicenseConfig;
import com.swak.license.spi.config.LicenseManager;
import com.swak.license.spi.config.LicenseVerifyService;
import com.swak.license.spi.config.LicenseVerifyServiceImpl;
import com.swak.license.spi.event.LicenseCheckListener;
import com.swak.license.spi.filter.LicenseCheckInterceptor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnBean(LicenseConfig.class)
public class SwakLicenseAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(LicenseManager.class)
    public LicenseManager licenseManager(LicenseConfig licenseConfig) {
        return new LicenseManager(licenseConfig);
    }



    @Bean
    @ConditionalOnMissingBean(LicenseVerifyService.class)
    public LicenseVerifyService licenseVerifyService(LicenseManager licenseManager){
        return new LicenseVerifyServiceImpl(licenseManager);
    }

    @Bean
    @ConditionalOnMissingBean(LicenseCheckListener.class)
    public LicenseCheckListener LicenseCheckListener(LicenseVerifyService licenseVerifyService,LicenseManager licenseManager) {
        LicenseCheckListener licenseCheckListener =  new LicenseCheckListener();
        licenseCheckListener.setLicenseVerifyService(licenseVerifyService);
        licenseCheckListener.setLicenseConfig(licenseManager.getLicenseConfig());
        return licenseCheckListener;
    }

    @Bean
    @ConditionalOnMissingBean(LicenseCheckInterceptor.class)
    public LicenseCheckInterceptor licenseCheckInterceptor(LicenseManager licenseManager) {
        LicenseCheckInterceptor interceptor = new LicenseCheckInterceptor();
        interceptor.setLicenseManager(licenseManager);
        interceptor.setLicenseVerifyCallback(licenseManager.getLicenseConfig().getLicenseVerifyCallback());
        interceptor.setSwakMvcPatterns(licenseManager.getLicenseConfig().getLicenseMvcConfig());
        return interceptor;
    }
}