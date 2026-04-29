package com.swak.license.spi.controller;


import com.swak.common.dto.Response;
import com.swak.common.enums.BasicErrCode;
import com.swak.common.util.NetUtils;
import com.swak.license.api.License;
import com.swak.license.provider.V4License;
import com.swak.license.spi.config.LicenseManager;
import com.swak.license.spi.event.LicensePrinter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.security.auth.x500.X500Principal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Slf4j
@RestController
@RequestMapping(value = "/license")
public class licIdxController {

    @Autowired(required = false)
    private LicenseManager licenseManager;

    @GetMapping("/info")
    public Response<License> getLicense() {
        try {
            License license = licenseManager.load();
            return Response.success(license);
        } catch (Exception e) {
            return Response.fail(BasicErrCode.SWAK_ERROR.getCode(), "获取授权证书信息失败");
        }
    }

    /**
     * 获取服务器硬件信息
     */
    @RequestMapping(value = "/getServerInfos")
    public Response<?> getServerInfos() {
        // 操作系统类型
        Map<String, List<String>> licenseCheckModel = new HashMap<>();
        try {
            licenseCheckModel.put("macAddress", NetUtils.getMacAddress());
            licenseCheckModel.put("IpAddress", NetUtils.getIpAddress());
        } catch (Exception e) {
            log.error("", e);
        }
        return Response.success(licenseCheckModel);
    }

    public static void main(String[] args) {
        License license = new V4License() ;
        license.setSubject("swak");
        license.setIssuer(new X500Principal("CN=swak"));
        license.setHolder(new X500Principal("CN=swak"));
        Map<String,Object> extra = new HashMap<>();
        extra.put("processTypeAmount",20);
        extra.put("eqpAmount",20);
        extra.put("gatherAmount",-1);
        license.setExtra(extra);
        license.setNotAfter(new Date());
        license.setNotBefore(new Date());
        final String licenseBanner = LicensePrinter.buildLicenseBanner(license);
        System.out.println(licenseBanner);
    }
}