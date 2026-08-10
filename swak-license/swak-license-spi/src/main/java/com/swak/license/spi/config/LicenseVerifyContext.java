package com.swak.license.spi.config;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.swak.license.api.License;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

@Slf4j
@Data
public class LicenseVerifyContext {
    private static final ThreadLocal<LicenseVerifyContext> LOCAL =
            ThreadLocal.withInitial(() -> new LicenseVerifyContext());
    private License license;
    private LicenseCheckExtra licenseExtra;

    protected LicenseVerifyContext(){}

    public static LicenseVerifyContext getContext() {
        LicenseVerifyContext context = LOCAL.get();
        if (Objects.nonNull(context)) {
            return context;
        }
        context = new LicenseVerifyContext();
        restoreContext(context);
        return context;
    }
    public static void restoreContext(LicenseVerifyContext oldContext) {
        LOCAL.set(oldContext);
    }

    public static void remove() {
        LOCAL.remove();
    }

    public static void install(License license) {
        LicenseVerifyContext context = getContext();
        context.setLicense(license);
        context.setLicenseExtra(getLicenseCheckExtra(license));
    }

    public static License getLicenseContext() {
        LicenseVerifyContext context = getContext();
        return context.getLicense();
    }
    public static LicenseCheckExtra getLicenseCheckExtra(License license) {
        JSONObject jsonObject = JSON.parseObject(JSON.toJSONString(license.getExtra()));
        return new LicenseCheckExtra(jsonObject);
    }

    public static LicenseCheckExtra getLicenseCheckExtra() {
        LicenseVerifyContext context = getContext();
        return context.getLicenseExtra();
    }
}