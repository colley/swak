package com.swak.license.spi.config;

import com.alibaba.fastjson2.JSON;
import com.swak.common.util.NetUtils;
import com.swak.license.Messages;
import com.swak.license.api.License;
import com.swak.license.api.LicenseValidationException;
import com.swak.license.spi.config.LicenseCheckExtra;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
import java.util.Objects;

import static com.swak.license.Messages.message;

@Slf4j
public class LicenseVerifyContext {

    private static final ThreadLocal<License> LICENSE = new ThreadLocal<>();

    public static void install(License license) {
        LICENSE.set(license);
    }

    public static License getLicense() {
       return LICENSE.get();
    }

    public static void clear() {
        LICENSE.remove();
    }
    public static void validateMacIp(final License content) throws LicenseValidationException {
        LicenseCheckExtra licenseCheckExtra = JSON.parseObject(JSON.toJSONString(content.getExtra()), LicenseCheckExtra.class);
        //当前服务器真实的参数信息
        List<String> ipAddressList = NetUtils.getIpAddress();
        List<String> macAddressList = NetUtils.getMacAddress();
        if (Objects.nonNull(licenseCheckExtra)) {
            List<String> expectedIpAddress = licenseCheckExtra.getIpAddress();
            List<String> expectedMacAddress = licenseCheckExtra.getMacAddress();
            if (CollectionUtils.isNotEmpty(expectedIpAddress)) {
                if (!checkAddress(expectedIpAddress, ipAddressList)) {
                    throw new LicenseValidationException(message(Messages.LICENSE_IP_ADDRESS),content);
                }
            }
            if (CollectionUtils.isNotEmpty(expectedMacAddress)) {
                if (!checkAddress(expectedMacAddress, macAddressList)) {
                    throw new LicenseValidationException(message(Messages.LICENSE_MAC_ADDRESS),content);
                }
            }
        }
    }

    public static void validateMacIp() throws LicenseValidationException {
         License license = getLicense();
         if(Objects.isNull(license)) {
             throw new LicenseValidationException(message(Messages.LICENSE_EXPIRED));
         }
         validateMacIp(license);
    }


    public static boolean checkAddress(List<String> expectedList, List<String> serverList) {
        if (CollectionUtils.isEmpty(expectedList)) {
            return true;
        }
        if (CollectionUtils.isEmpty(serverList)) {
            return false;
        }
        for (String expected : expectedList) {
            if (serverList.contains(expected.trim())) {
                return true;
            }
        }
        return false;
    }
}