package com.swak.security.spring.configuration;

import com.swak.core.SwakConstants;
import com.swak.core.security.AuthenticationListener;
import com.swak.core.security.SwakAuthenticationFilter;
import com.swak.security.SwakWebSecurityConfigurer;
import com.swak.security.authentication.*;
import com.swak.security.config.JwtTokenConfig;
import com.swak.security.config.WhitelistConfig;
import com.swak.security.handle.AuthenticationSuccessHandlerImpl;
import com.swak.security.handle.LogoutSuccessHandlerImpl;
import com.swak.security.handle.RestfulAccessDeniedHandler;
import com.swak.security.handle.RestfulFailureHandler;
import com.swak.security.service.SecurityAuthClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.firewall.DefaultHttpFirewall;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.util.CollectionUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;


@Configuration
@EnableConfigurationProperties({JwtTokenConfig.class})
@ConditionalOnBean(SecurityAuthClientService.class)
@ConditionalOnProperty(prefix = SwakConstants.SWAK_SECURITY, name = "enabled", havingValue = "true")
public abstract class SwakJwtSecurityAutoConfiguration {

    @Resource
    protected JwtTokenConfig jwtTokenConfig;

    protected List<AuthenticationListener> authenticationListeners = new ArrayList<>();

    protected List<SwakAuthenticationFilter> authenticationFilters = new ArrayList<>();


    @Autowired(required = false)
    public void setAuthenticationHandlers(List<AuthenticationListener> authenticationListeners) {
        if (!CollectionUtils.isEmpty(authenticationListeners)) {
            this.authenticationListeners.addAll(authenticationListeners);
        }
    }

    @Autowired(required = false)
    public void setSwakAuthenticationTokenFilters(List<SwakAuthenticationFilter> authenticationFilters) {
        if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(authenticationFilters)) {
            this.authenticationFilters.addAll(authenticationFilters);
        }
    }

    @Bean
    @ConditionalOnMissingBean(UserTokenService.class)
    public UserTokenService userTokenService(SecurityAuthClientService securityAuthClientService) {
        UserTokenServiceImpl userTokenService = new UserTokenServiceImpl();
        userTokenService.setJwtTokenConfig(jwtTokenConfig);
        userTokenService.setSecurityAuthClientService(securityAuthClientService);
        return userTokenService;
    }

    /**
     * 退出处理类
     */
    @Bean
    public LogoutSuccessHandler logoutSuccessHandler(UserTokenService userTokenService) {
        return new LogoutSuccessHandlerImpl(userTokenService, authenticationListeners);
    }

    /**
     * 认证成功处理类
     */
    @Bean
    public AuthenticationSuccessHandler authenticationSuccessHandler(UserTokenService userTokenService) {
        AuthenticationSuccessHandlerImpl successHandler = new AuthenticationSuccessHandlerImpl(userTokenService);
        successHandler.setAuthenticationListeners(authenticationListeners);
        return successHandler;
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return new RestfulAccessDeniedHandler();
    }


    @Bean
    public AuthenticationFailureHandler restfulFailureHandler() {
        return new RestfulFailureHandler();
    }


    @Bean
    @ConditionalOnMissingBean(SwakUserDetailsService.class)
    public SwakUserDetailsService swakUserDetailsService(UserTokenService userTokenService) {
        return new SwakUserDetailsServiceImpl(userTokenService);
    }


    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.setAllowCredentials(true);
        corsConfiguration.addAllowedHeader("*");
        corsConfiguration.addAllowedMethod("*");
        //corsConfiguration.addAllowedOriginPattern("*");
        corsConfiguration.addAllowedOrigin("*");
        corsConfiguration.setMaxAge(10000L);
        // 匹配所有API
        source.registerCorsConfiguration("/**", corsConfiguration);
        return new CorsFilter(source);

    }

    @Bean
    @ConditionalOnMissingBean(SwakWebSecurityConfigurer.class)
    public SwakWebSecurityConfigurer swakWebSecurityConfigurer() {
       return new SwakWebSecurityConfigurer();
    }

    /**
     * 强散列哈希加密实现
     */
    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @ConditionalOnMissingBean(TokenLoginService.class)
    public TokenLoginService tokenLoginService(UserTokenService userTokenService) {
        return new TokenLoginServiceImpl(userTokenService);
    }

    @Bean
    public HttpFirewall httpFirewall() {
        return new DefaultHttpFirewall();
    }

    /**
     * 配置不需要登录的白名单
     */
    @Bean
    @ConditionalOnMissingBean(WhitelistConfig.class)
    public WhitelistConfig whitelistConfig() {
        return new WhitelistConfig();
    }

}
