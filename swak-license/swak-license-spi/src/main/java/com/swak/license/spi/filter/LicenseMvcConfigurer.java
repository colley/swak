package com.swak.license.spi.filter;

import com.swak.core.SwakConstants;
import com.swak.core.web.SwakMvcConfigurer;
import com.swak.core.web.SwakMvcPatterns;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;

import java.util.Collections;
import java.util.Objects;
import java.util.Optional;

/**
 * @author colley.ma
 * @since 3.0.0
 */
@Order(SwakConstants.ORDER_PRECEDENCE + 3)
@Component
public class LicenseMvcConfigurer implements SwakMvcConfigurer {

    @Autowired(required = false)
    private LicenseCheckInterceptor licenseCheckInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        if (Objects.isNull(licenseCheckInterceptor)) {
            return;
        }
        SwakMvcPatterns swakMvcPatterns = Optional.ofNullable(licenseCheckInterceptor.getSwakMvcPatterns())
                .orElse(new SwakMvcPatterns());
        InterceptorRegistration registration = registry.addInterceptor(licenseCheckInterceptor);
        if (CollectionUtils.isNotEmpty(swakMvcPatterns.getIncludePatterns())) {
            registration.addPathPatterns(swakMvcPatterns.getIncludePatterns());
        }
        if (CollectionUtils.isNotEmpty(swakMvcPatterns.getExcludePatterns())) {
            registration.excludePathPatterns(swakMvcPatterns.getExcludePatterns());
        }
    }
}
