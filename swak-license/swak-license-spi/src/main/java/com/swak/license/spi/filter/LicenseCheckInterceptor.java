package com.swak.license.spi.filter;

import com.alibaba.fastjson2.JSON;
import com.swak.common.dto.Response;
import com.swak.common.enums.BasicErrCode;
import com.swak.common.util.GetterUtil;
import com.swak.core.web.SwakMvcPatterns;
import com.swak.license.api.License;
import com.swak.license.api.LicenseValidationException;
import com.swak.license.spi.config.LicenseVerifyContext;
import com.swak.license.spi.config.LicenseManager;
import com.swak.license.spi.config.LicenseVerifyCallback;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Optional;

/**
 * @author colley
 */
@Setter
public class LicenseCheckInterceptor implements HandlerInterceptor {
    private LicenseManager licenseManager;

    private LicenseVerifyCallback licenseVerifyCallback;

    @Getter
    private SwakMvcPatterns swakMvcPatterns;

    @Override
    public boolean preHandle(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull Object handler) throws Exception {
        try {
            License license = licenseManager.verify();
            Optional.ofNullable(licenseVerifyCallback).ifPresent(call -> call.call(license));
            LicenseVerifyContext.install(license);
            return true;
        } catch (LicenseValidationException e) {
            printJson(response, e.getMessage());
        } catch (Exception e) {
            printJson(response, null);
        }
        return false;
    }

	private void printJson(HttpServletResponse response, String message) throws IOException {
        response.setCharacterEncoding("utf-8");
        response.setContentType("text/json;charset=UTF-8");
        Response<Void> resultImpl = Response.fail(BasicErrCode.LICENSE_EXPIRED.getCode(),
                GetterUtil.getString(message, BasicErrCode.LICENSE_EXPIRED.getI18nMsg()));
        try (PrintWriter writer = response.getWriter()) {
            writer.print(JSON.toJSONString(resultImpl));
        }
    }

    @Override
    public void afterCompletion(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull Object handler, Exception ex) throws Exception {
        Optional.ofNullable(licenseVerifyCallback).ifPresent(LicenseVerifyCallback::clear);
        LicenseVerifyContext.remove();
    }
}
