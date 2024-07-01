package com.swak.license.spi.config;


import com.swak.license.api.License;
import com.swak.license.api.LicenseManagementException;
import com.swak.license.api.io.Source;

public class LicenseVerifyServiceImpl implements LicenseVerifyService {

    private LicenseManager licenseManager;

    public LicenseVerifyServiceImpl(LicenseManager licenseManager){
        this.licenseManager = licenseManager;
    }

    @Override
    public License install(Source source) throws LicenseManagementException {
       return licenseManager.install(source);
    }

    @Override
    public License load() throws LicenseManagementException {
        return licenseManager.load();
    }

    @Override
    public License verify() throws LicenseManagementException {
        return licenseManager.verify();
    }

    @Override
    public void uninstall() throws LicenseManagementException {
        licenseManager.uninstall();
    }
}
