package com.swak.i18n;

import com.swak.core.SwakConstants;
import com.swak.core.web.SwakMvcConfigurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;

import java.util.Objects;

/**
 * @author colley.ma
 * @since 3.0.0
 */
@Order(SwakConstants.ORDER_PRECEDENCE +2)
@Component
public class I18nSwakMvcConfigurer implements SwakMvcConfigurer {

    @Autowired(required = false)
    private LocaleCompositeInterceptor localeCompositeInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        if(Objects.isNull(localeCompositeInterceptor)){
            return;
        }
        registry.addInterceptor(localeCompositeInterceptor);
    }
}
