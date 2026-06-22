package com.swak.license.config;

import com.swak.license.api.License;
import com.swak.license.spi.config.LicenseVerifyCallback;

import java.util.Optional;

/**
 * LicenseRegistryCallBack.java
 *
 * @author colley.ma
 * @since 2.3.3
 */
public class LicenseRegistryCallBack implements LicenseVerifyCallback {
    private final LicenseRegistry licenseRegistry;
    public LicenseRegistryCallBack(LicenseRegistry licenseRegistry) {
        this.licenseRegistry = licenseRegistry;
    }
    @Override
    public void call(License license) {
        Optional.ofNullable(licenseRegistry).ifPresent(lic -> lic.install(license));
    }
    @Override
    public void clear() {
        Optional.ofNullable(licenseRegistry).ifPresent(LicenseRegistry::remove);
    }
}
