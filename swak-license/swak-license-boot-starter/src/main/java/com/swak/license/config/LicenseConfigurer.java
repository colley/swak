package com.swak.license.config;

import com.swak.license.spi.config.LicenseMvcConfig;

import java.util.List;

/**
 * LicenseConfigurer.java
 *
 * @author colley.ma
 * @since 2.3.3
 */
public interface LicenseConfigurer {
     String getLicenseDataId();
     String getPublicKeyPath();
     long[] getStorePass();
    /**
     * 证书subject
     */
     String getSubject();
    /**
     * 版本
     */
     String getEdition();

      LicenseMvcConfig getLicenseMvcConfig();

     boolean isValidErrThrow();
}
