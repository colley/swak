package com.swak.license.server.config;


import com.swak.license.server.domain.LicenseCreatorParam;
import com.swak.license.api.Codec;
import com.swak.license.api.License;
import com.swak.license.api.LicenseManagerParameters;
import com.swak.license.api.VendorLicenseManager;
import com.swak.license.api.io.Store;
import com.swak.license.api.io.bios.BIOS;
import lombok.extern.slf4j.Slf4j;

import java.text.MessageFormat;
import java.util.Date;

import static com.swak.license.api.io.bios.BIOS.file;
import static java.lang.System.err;

/**
 * License生成类
 */


@Slf4j
public class LicenseCreator {

    private volatile CreateLicenseManager manager;

    private LicenseCreatorParam licenseCreatorParam;

    public LicenseCreator(LicenseCreatorParam licenseCreatorParam) {
        this.licenseCreatorParam = licenseCreatorParam;
        this.manager = new CreateLicenseManager(licenseCreatorParam);
    }

    VendorLicenseManager manager() {
        return manager;
    }

    /**
     * 生成License证书
     *
     * @return boolean
     */
    public boolean generateLicense() {
        try {
            final License input = createOrDecodeInputLicense();
            final Store store = store();
            final License output = manager()
                    .generateKeyFrom(input)
                    .saveTo(store)
                    .license();
            err.println(output);
            //maybeEncodeOutputLicense(output);
           // err.println(BaseEncoding.base64().encode(store.content()));
            log.info("证书生成成功！");
            return true;
        } catch (Exception e) {
            log.error(MessageFormat.format("证书生成失败：{0}", licenseCreatorParam), e);
            return false;
        }
    }

    License createOrDecodeInputLicense() throws Exception {
        validateCreate(licenseCreatorParam);
        License license = parameters().licenseFactory().license();
        initLicense(license);
        return license;
    }

    private void initLicense(License license) {
        license.setSubject(licenseCreatorParam.getSubject());
        license.setIssued(licenseCreatorParam.getIssuedTime());
        license.setNotBefore(licenseCreatorParam.getIssuedTime());
        license.setNotAfter(licenseCreatorParam.getExpiryTime());
        license.setConsumerType(licenseCreatorParam.getConsumerType());
        license.setConsumerAmount(licenseCreatorParam.getConsumerAmount());
        license.setInfo(licenseCreatorParam.getDescription());
        license.setExtra(licenseCreatorParam.getLicenseCheckModel());
        //扩展校验服务器硬件信息
    }

    void maybeEncodeOutputLicense(final License license) throws Exception {
        final String path = licenseCreatorParam.getLicensePath();
        if (null != path) {
            codec().encoder("-".equals(path) ? BIOS.stdout() : file(path))
                    .encode(license);
        }
    }


    private Codec codec() {
        return parameters().codec();
    }


    private Store store() {
        Store store = null == licenseCreatorParam.getLicensePath() ? BIOS.memory() : file(licenseCreatorParam.getLicensePath());
        return store.map(BIOS.base64()); // send to /dev/null if no path
    }


    LicenseManagerParameters parameters() {
        return manager().parameters();
    }

    protected synchronized void validateCreate(final LicenseCreatorParam licenseCreatorParam) {
        final Date now = new Date();
        final Date notBefore = licenseCreatorParam.getIssuedTime();
        final Date notAfter = licenseCreatorParam.getExpiryTime();
        if (null != notAfter && now.after(notAfter)) {
            throw new IllegalArgumentException("证书失效时间不能早于当前时间");
        }
        if (null != notBefore && null != notAfter && notAfter.before(notBefore)) {
            throw new IllegalArgumentException("证书生效时间不能晚于证书失效时间");
        }
        final String consumerType = licenseCreatorParam.getConsumerType();
        if (null == consumerType) {
            throw new IllegalArgumentException("用户类型不能为空");
        }
    }
}
