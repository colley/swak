package com.swak.license.config;

import com.swak.license.api.License;
import com.swak.license.spi.config.LicenseCheckExtra;
import com.swak.license.spi.config.LicenseVerifyContext;

/**
 * @author colley.ma
 * @since 2.3.3
 */
public interface LicenseRegistry {

    default void install(License license) {
    }

    default void remove() {
        LicenseVerifyContext.remove();
    }

    default License getLicense() {
        return LicenseVerifyContext.getLicenseContext();
    }

    default LicenseCheckExtra getLicenseExtra() {
        return LicenseVerifyContext.getLicenseCheckExtra();
    }
}
