package com.swak.license.spi.config;


import com.swak.core.web.SwakMvcPatterns;
import com.swak.license.api.LicenseValidation;
import com.swak.license.api.io.Source;
import lombok.Data;

@Data
public class LicenseConfig {
    /**
     * 证书subject
     */
    private String subject;

    /**
     * 版本
     */
    private String edition ="standard";

    /**
     * 访问公钥库的密码
     */
    private long[]  storePass;

    /**
     * 证书生成路径
     */
    private Source license;


    /**
     * 密钥库
     */
    private Source publicKeys;

    /**
     * 验证
     */
    private LicenseValidation validation = bean -> {};

    /**
     * 验证callback
     */
    private LicenseVerifyCallback licenseVerifyCallback;

    private boolean throwErr;

    private SwakMvcPatterns licenseMvcConfig;
}
