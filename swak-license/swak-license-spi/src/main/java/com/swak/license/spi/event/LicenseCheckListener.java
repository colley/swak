package com.swak.license.spi.event;

import com.swak.common.listener.SwakEventListener;
import com.swak.license.api.License;
import com.swak.license.api.LicenseValidationException;
import com.swak.license.api.io.bios.BIOS;
import com.swak.license.spi.config.LicenseConfig;
import com.swak.license.spi.config.LicenseRuntimeException;
import com.swak.license.spi.config.LicenseVerifyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

/**
 * 在项目启动时安装证书
 */
@Slf4j
public class LicenseCheckListener implements ApplicationListener<ContextRefreshedEvent>, SwakEventListener {

    private LicenseVerifyService licenseVerifyService;

    private LicenseConfig licenseConfig;

    public volatile boolean isStart = true;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (isStart) {
            try {
                log.info("++++++++ 开始安装授权认证书 ++++++++");
                License license = licenseVerifyService.install(licenseConfig.getLicense().map(BIOS.base64()));
                //log.info(MessageFormat.format("证书安装成功，证书有效期：{0} - {1}", DateTimeUtils.date2String(license.getNotBefore()), DateTimeUtils.date2String(license.getNotAfter())));
                log.info(LicensePrinter.buildLicenseBanner(license));
            } catch (LicenseValidationException e) {
                log.error("++++++++ 安装授权认证书失败 ++++++++",e);
                if (licenseConfig.isThrowErr()) {
                    throw new LicenseRuntimeException(e);
                }
            } catch (Exception e) {
                log.error("++++++++ 安装授权认证书失败 ++++++++",e);
                if (licenseConfig.isThrowErr()) {
                    throw new LicenseRuntimeException(e);
                }
            }
            isStart = false;
        }
    }
    public void setLicenseVerifyService(LicenseVerifyService licenseVerifyService) {
        this.licenseVerifyService = licenseVerifyService;
    }

    public void setLicenseConfig(LicenseConfig licenseConfig) {
        this.licenseConfig = licenseConfig;
    }
}
