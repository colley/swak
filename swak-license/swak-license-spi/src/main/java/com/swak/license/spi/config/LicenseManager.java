package com.swak.license.spi.config;


import com.swak.license.api.*;
import com.swak.license.api.io.Source;
import com.swak.license.api.passwd.PasswordProtection;
import com.swak.license.core.passwd.ObfuscatedPasswordProtection;
import com.swak.license.provider.V4;
import com.swak.common.util.ObfuscatedString;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class LicenseManager implements ConsumerLicenseManager {


    private LicenseConfig licenseConfig;

    private LicenseManagementContext _managementContext;

    private volatile ConsumerLicenseManager _manager;

    public LicenseManager(LicenseConfig licenseConfig) {
        this.licenseConfig = licenseConfig;
        this._managementContext = newManagementContext(licenseConfig);
    }

    private LicenseManagementContext newManagementContext(LicenseConfig licenseConfig) {
        return V4.builder()
                .validation(licenseConfig.getValidation())
                .subject(licenseConfig.getSubject())
                .build();
    }

    private ConsumerLicenseManager newManager() {
        return _managementContext
                .consumer()
                .encryption()
                .protection(protection(licenseConfig.getStorePass()) /* => "fdc2023" */)
                .up()
                .authentication()
                .alias(licenseConfig.getEdition())
                .loadFrom(licenseConfig.getPublicKeys())
                .storeProtection(protection(licenseConfig.getStorePass()) /* => "fdc2023" */)
                .up()
                .storeInUserPreferences(LicenseConfig.class) // must be a non-obfuscated class!
                .build();
    }

    private ConsumerLicenseManager manager() {
        // No need to synchronize because managers are virtually stateless.
        final ConsumerLicenseManager m = _manager;
        return null != m ? m : (_manager = newManager());
    }

    private static PasswordProtection protection(long[] obfuscated) {
        return new ObfuscatedPasswordProtection(new ObfuscatedString(obfuscated));
    }

    @Override
    public LicenseManagerParameters parameters() {
        return manager().parameters();
    }

    @Override
    public License install(Source source) throws LicenseManagementException {
        return manager().install(source);
    }

    @Override
    public License load() throws LicenseManagementException {
        return manager().load();
    }

    @Override
    public License verify() throws LicenseManagementException {
        return manager().verify();
    }

    @Override
    public void uninstall() throws LicenseManagementException {
        manager().uninstall();
    }

    public LicenseConfig getLicenseConfig() {
        return licenseConfig;
    }
}
