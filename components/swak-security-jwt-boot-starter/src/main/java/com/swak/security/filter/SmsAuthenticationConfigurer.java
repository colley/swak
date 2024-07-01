package com.swak.security.filter;

import com.google.common.collect.Lists;
import com.swak.core.security.AuthenticationListener;
import com.swak.security.authentication.SmsCodeAuthenticationProvider;
import com.swak.security.authentication.SwakUserDetailsService;
import com.swak.security.authentication.UserTokenService;
import com.swak.security.config.JwtTokenConfig;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.savedrequest.RequestCacheAwareFilter;

import java.util.Map;

/**
 * @author colley.ma
 * @since 3.0.0
 */
public class SmsAuthenticationConfigurer extends AbstractHttpConfigurer<SmsAuthenticationConfigurer, HttpSecurity> {

    @Override
    public void configure(HttpSecurity builder) throws Exception {
        UserTokenService userTokenService = builder.getSharedObject(ApplicationContext.class).getBean(UserTokenService.class);
        AuthenticationManager authenticationManager = builder.getSharedObject(ApplicationContext.class).getBean(AuthenticationManager.class);
        JwtTokenConfig jwtTokenConfig = builder.getSharedObject(ApplicationContext.class).getBean(JwtTokenConfig.class);
        Map<String, AuthenticationListener> swakAuthenticationFilters = builder.getSharedObject(ApplicationContext.class)
                .getBeansOfType(AuthenticationListener.class);
        AuthenticationSuccessHandler successHandler= builder.getSharedObject(ApplicationContext.class).getBean(AuthenticationSuccessHandler.class);
        AuthenticationFailureHandler failureHandler = builder.getSharedObject(ApplicationContext.class).getBean(AuthenticationFailureHandler.class);
        DynamicAuthenticationFilter filter = dynamicAuthenticationFilter(jwtTokenConfig,authenticationManager);
        filter.setAuthenticationSuccessHandler(successHandler);
        filter.setAuthenticationFailureHandler(failureHandler);
        builder.addFilterBefore(filter, RequestCacheAwareFilter.class);
    }

    public DynamicAuthenticationFilter dynamicAuthenticationFilter(JwtTokenConfig jwtTokenConfig,
                                                                   AuthenticationManager authenticationManager) {
        DynamicAuthenticationFilter dynamicAuthenticationFilter =
                new DynamicAuthenticationFilter(jwtTokenConfig.getLoginUrl());
        dynamicAuthenticationFilter.setAuthenticationManager(authenticationManager);
        return dynamicAuthenticationFilter;
    }
}
