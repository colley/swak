package com.swak.license.server.config;

import com.swak.common.util.ObfuscatedString;
import com.swak.license.server.domain.LicenseCreatorParam;
import com.swak.license.api.*;
import com.swak.license.api.io.Source;
import com.swak.license.api.passwd.PasswordProtection;
import com.swak.license.core.passwd.ObfuscatedPasswordProtection;
import com.swak.license.provider.V4;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;

import javax.security.auth.x500.X500Principal;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Optional;


@Slf4j
public class CreateLicenseManager implements VendorLicenseManager {
    private LicenseCreatorParam licenseCreatorParam;
    private LicenseManagementContext _managementContext;
    private volatile VendorLicenseManager _manager;

    public CreateLicenseManager(LicenseCreatorParam licenseCreatorParam) {
        this.licenseCreatorParam = licenseCreatorParam;
        this._managementContext = newManagementContext(licenseCreatorParam);
    }

    private LicenseManagementContext newManagementContext(LicenseCreatorParam licenseCreatorParam) {
        return V4.builder()
                .initialization(bean -> {
                    if (null == bean.getIssuer()) {
                        bean.setIssuer(new X500Principal(licenseCreatorParam.getDistinguishedName()));
                    }
                    if(bean.getHolder()==null){
                        bean.setHolder(bean.getIssuer());
                    }
                }).subject(licenseCreatorParam.getSubject())
                .build();
    }


    private VendorLicenseManager manager() {
        // No need to synchronize because managers are virtually stateless.
        final VendorLicenseManager m = _manager;
        return null != m ? m : (_manager = newManager());
    }

    private VendorLicenseManager newManager() {
        return  _managementContext.vendor()
                .encryption()
                .protection(protection(licenseCreatorParam.getKeyPass()) /* => "fdc2023" */)
                .up()
                .authentication()
                .alias(licenseCreatorParam.getEdition())
                .loadFrom(privateKeyPath(licenseCreatorParam.getPrivateKeyPath()))
                .storeProtection(protection(licenseCreatorParam.getKeyPass()) /* => "fdc2023" */)
                .up()
                .build();
    }

    private Source privateKeySource(String name) {
        return () -> () -> Optional
                .ofNullable(new ClassPathResource(name).getInputStream())
                .orElseThrow(() -> new FileNotFoundException(name));
    }

    private Source privateKeyPath(String name) {
        return () -> () -> Optional
                .ofNullable(new FileInputStream(name))
                .orElseThrow(() -> new FileNotFoundException(name));
    }

    private static PasswordProtection protection(String obfuscated) {
        return new ObfuscatedPasswordProtection(new ObfuscatedString(ObfuscatedString.array(obfuscated)));
    }

    @Override
    public LicenseManagerParameters parameters() {
        return manager().parameters();
    }

    @Override
    public LicenseKeyGenerator generateKeyFrom(License bean) throws LicenseManagementException {
        return manager().generateKeyFrom(bean);
    }
}
