package com.swak.license.spi.controller;


import com.swak.common.dto.Response;
import com.swak.common.enums.BasicErrCode;
import com.swak.license.AbstractServerInfo;
import com.swak.license.api.License;
import com.swak.license.spi.config.LicenseManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
        AbstractServerInfo abstractServerInfo = new AbstractServerInfo() {
        };
        Map<String, List<String>> licenseCheckModel = new HashMap<>();
        try {
            licenseCheckModel.put("macAddress", abstractServerInfo.getMacAddress());
            licenseCheckModel.put("IpAddress", abstractServerInfo.getIpAddress());
        } catch (Exception e) {
            log.error("", e);
        }
        return Response.success(licenseCheckModel);
    }
}