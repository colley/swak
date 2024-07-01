package com.swak.license.spi.event;

import com.swak.license.api.License;
import com.swak.license.api.LicenseValidationException;
import com.swak.license.api.io.bios.BIOS;
import com.swak.license.spi.config.LicenseConfig;
import com.swak.license.spi.config.LicenseVerifyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;

/**
 * 在项目启动时安装证书
 */
@Slf4j
public class LicenseCheckListener implements ApplicationListener<ContextRefreshedEvent> {

    private LicenseVerifyService licenseVerifyService;

    private LicenseConfig licenseConfig;

    public volatile boolean isStart = true;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        //root application context 没有parent
        if (isStart) {
            try {
                DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                log.info("++++++++ 开始安装授权认证书 ++++++++");
                License license = licenseVerifyService.install(licenseConfig.getLicense().map(BIOS.base64()));
                log.info(MessageFormat.format("证书安装成功，证书有效期：{0} - {1}", format.format(license.getNotBefore()), format.format(license.getNotAfter())));
            } catch (LicenseValidationException e) {
                if (licenseConfig.isThrowErr()) {
                    throw new RuntimeException(e.getMessage());
                }
                log.error("", e);
            } catch (Exception e) {
                if (licenseConfig.isThrowErr()) {
                    throw new RuntimeException(e);
                }
                log.error("", e);
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
