package com.swak.license.config;

import com.swak.license.spi.config.LicenseMvcConfig;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * LicenseConfigurer.java
 *
 * @author colley.ma
 * @since 2.3.3
 */
@Data
public class SimpleLicenseConfigurer implements LicenseConfigurer{
    private String licenseDataId = "license";
    private String publicKeyPath = "/license/rsa_public.key";
    private long[] storePass = new long[]{0x694b8df3751f696cL, 0xa6208e2a0645976fL, 0x8a65b409619b7d1dL};
    private String subject = "fdcStandard";
    private String edition = "standard";
    private boolean validErrThrow;
    private LicenseMvcConfig licenseMvcConfig = new LicenseMvcConfig();
}
