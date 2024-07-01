package com.swak.license.spi.filter;

import com.alibaba.fastjson2.JSON;
import com.swak.common.dto.Response;
import com.swak.common.enums.BasicErrCode;
import com.swak.common.util.GetterUtil;
import com.swak.core.web.SwakMvcPatterns;
import com.swak.license.api.License;
import com.swak.license.api.LicenseValidationException;
import com.swak.license.spi.config.LicenseManager;
import com.swak.license.spi.config.LicenseVerifyCallback;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Optional;

@Slf4j
public class LicenseCheckInterceptor implements HandlerInterceptor {
    private LicenseManager licenseManager;

    private LicenseVerifyCallback licenseVerifyCallback;

    @Setter
    @Getter
    private SwakMvcPatterns swakMvcPatterns;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        try {
            License license = licenseManager.verify();
            Optional.ofNullable(licenseVerifyCallback).ifPresent(call -> call.call(license));
            return true;
        } catch (LicenseValidationException e) {
            printJson(response, e.getMessage());
        } catch (Exception e) {
            printJson(response, null);
        }
        return false;
    }

    public void setLicenseManager(LicenseManager licenseManager) {
        this.licenseManager = licenseManager;
    }

    public void setLicenseVerifyCallback(LicenseVerifyCallback licenseVerifyCallback) {
        this.licenseVerifyCallback = licenseVerifyCallback;
    }

    private void printJson(HttpServletResponse response, String message) throws IOException {
        response.setCharacterEncoding("utf-8");
        response.setContentType("text/json;charset=UTF-8");
        Response<Void> resultImpl = Response.fail(BasicErrCode.SWAK_LICENSE.getCode(),
                GetterUtil.getString(message, BasicErrCode.SWAK_LICENSE.getI18nMsg()));
        try (PrintWriter writer = response.getWriter()) {
            writer.print(JSON.toJSONString(resultImpl));
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        Optional.ofNullable(licenseVerifyCallback).ifPresent(call -> call.clear());
    }
}
