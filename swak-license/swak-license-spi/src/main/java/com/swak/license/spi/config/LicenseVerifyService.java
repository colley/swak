package com.swak.license.spi.config;


import com.swak.license.api.License;
import com.swak.license.api.LicenseManagementException;
import com.swak.license.api.io.Source;

public interface LicenseVerifyService {

    License install(Source source) throws LicenseManagementException;

    License load() throws LicenseManagementException;


    License verify() throws LicenseManagementException;

    void uninstall() throws LicenseManagementException;
}
