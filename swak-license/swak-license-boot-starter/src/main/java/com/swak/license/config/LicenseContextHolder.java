package com.swak.license.config;

import com.swak.common.util.NetUtils;
import com.swak.core.support.SpringBeanFactory;
import com.swak.license.Messages;
import com.swak.license.api.License;
import com.swak.license.api.LicenseValidationException;
import com.swak.license.spi.config.LicenseCheckExtra;
import com.swak.license.spi.config.LicenseVerifyContext;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.swak.license.Messages.message;


public class LicenseContextHolder {


    protected LicenseContextHolder() {
    }

    public static License getLicense() {
        LicenseRegistry licenseRegistry = SpringBeanFactory.getBean(LicenseRegistry.class);
        if (Objects.isNull(licenseRegistry)) {
            return LicenseVerifyContext.getLicenseContext();
        }
        return Optional.ofNullable(licenseRegistry.getLicense()).orElse(LicenseVerifyContext.getLicenseContext());
    }

    public static LicenseCheckExtra getLicenseExtra() {
        LicenseRegistry licenseRegistry = SpringBeanFactory.getBean(LicenseRegistry.class);
        if (Objects.isNull(licenseRegistry)) {
            return LicenseVerifyContext.getLicenseCheckExtra();
        }
        return Optional.ofNullable(licenseRegistry.getLicenseExtra()).orElse(LicenseVerifyContext.getLicenseCheckExtra());
    }


    public static void validateMacIp() throws LicenseValidationException {
        License license = getLicense();
        if (Objects.isNull(license)) {
            throw new LicenseValidationException(message(Messages.LICENSE_EXPIRED));
        }
        validateMacIp(license, getLicenseExtra());
    }

    public static void validateMac() throws LicenseValidationException {
        License license = getLicense();
        if (Objects.isNull(license)) {
            throw new LicenseValidationException(message(Messages.LICENSE_EXPIRED));
        }
        validateMac(license, getLicenseExtra());
    }

    public static void validateIp() throws LicenseValidationException {
        License license = getLicense();
        if (Objects.isNull(license)) {
            throw new LicenseValidationException(message(Messages.LICENSE_EXPIRED));
        }
        validateIp(license, getLicenseExtra());
    }

    private static void validateMac(License license, LicenseCheckExtra licenseCheckExtra) throws LicenseValidationException {
        //当前服务器真实的参数信息
        if (Objects.isNull(licenseCheckExtra)) {
            licenseCheckExtra = getLicenseExtra();
        }
        List<String> macAddressList = NetUtils.getMacAddress();
        List<String> expectedMacAddress = licenseCheckExtra.getMacAddress();
        if (CollectionUtils.isNotEmpty(expectedMacAddress)) {
            if (checkAddress(expectedMacAddress, macAddressList)) {
                throw new LicenseValidationException(message(Messages.LICENSE_MAC_ADDRESS), license);
            }
        }
    }

    private static void validateIp(License license, LicenseCheckExtra licenseCheckExtra) throws LicenseValidationException {
        //当前服务器真实的参数信息
        if (Objects.isNull(licenseCheckExtra)) {
            licenseCheckExtra = getLicenseExtra();
        }
        List<String> ipAddressList = NetUtils.getIpAddress();
        List<String> expectedIpAddress = licenseCheckExtra.getIpAddress();
        if (CollectionUtils.isNotEmpty(expectedIpAddress)) {
            if (checkAddress(expectedIpAddress, ipAddressList)) {
                throw new LicenseValidationException(message(Messages.LICENSE_IP_ADDRESS), license);
            }
        }
    }

    private static void validateMacIp(License license, LicenseCheckExtra licenseCheckExtra) throws LicenseValidationException {
        //当前服务器真实的参数信息
        if (Objects.isNull(licenseCheckExtra)) {
            licenseCheckExtra = getLicenseExtra();
        }
        List<String> ipAddressList = NetUtils.getIpAddress();
        List<String> macAddressList = NetUtils.getMacAddress();
        List<String> expectedIpAddress = licenseCheckExtra.getIpAddress();
        List<String> expectedMacAddress = licenseCheckExtra.getMacAddress();
        if (CollectionUtils.isNotEmpty(expectedIpAddress)) {
            if (checkAddress(expectedIpAddress, ipAddressList)) {
                throw new LicenseValidationException(message(Messages.LICENSE_IP_ADDRESS), license);
            }
        }
        if (CollectionUtils.isNotEmpty(expectedMacAddress)) {
            if (checkAddress(expectedMacAddress, macAddressList)) {
                throw new LicenseValidationException(message(Messages.LICENSE_MAC_ADDRESS), license);
            }
        }
    }

    private static boolean checkAddress(List<String> expectedList, List<String> serverList) {
        if (CollectionUtils.isEmpty(serverList)) {
            return true;
        }
        if (CollectionUtils.isEmpty(expectedList)) {
            return true;
        }
        for (String expected : expectedList) {
            if (serverList.contains(expected.trim())) {
                return false;
            }
        }
        return true;
    }
}