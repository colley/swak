package com.swak.security;

import com.google.common.collect.Sets;
import com.swak.security.authentication.SmsCodeAuthenticationProvider;
import com.swak.security.authentication.SwakUserDetailsService;
import com.swak.security.authentication.UserTokenService;
import com.swak.security.config.JwtConstants;
import com.swak.security.config.JwtTokenConfig;
import com.swak.security.config.WhitelistConfig;
import com.swak.security.filter.JwtAuthenticationTokenFilter;
import com.swak.security.filter.TokenAuthenticationConfigurer;
import com.swak.security.handle.AuthenticationEntryPointImpl;
import lombok.Getter;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.filter.CorsFilter;

import javax.annotation.Resource;
import java.util.Set;

public  class SwakWebSecurityConfigurer {

    @Resource
    @Getter
    protected JwtTokenConfig tokenProperties;

    @Resource
    @Getter
    protected UserTokenService userTokenService;

    @Resource
    @Getter
    protected SwakUserDetailsService swakUserDetailsService;

    @Resource
    @Getter
    protected LogoutSuccessHandler logoutSuccessHandler;

    @Resource
    @Getter
    protected AuthenticationSuccessHandler authenticationSuccessHandler;

    @Resource
    @Getter
    protected CorsFilter corsFilter;

    @Resource
    @Getter
    protected AccessDeniedHandler accessDeniedHandler;

    @Resource
    @Getter
    protected AuthenticationFailureHandler restfulFailureHandler;

    @Resource
    @Getter
    protected WhitelistConfig whitelistConfig;

    /**
     * anyRequest          |   匹配所有请求路径
     * access              |   SpringEl表达式结果为true时可以访问
     * anonymous           |   匿名可以访问
     * denyAll             |   用户不能访问
     * fullyAuthenticated  |   用户完全认证可以访问（非remember-me下自动登录）
     * hasAnyAuthority     |   如果有参数，参数表示权限，则其中任何一个权限可以访问
     * hasAnyRole          |   如果有参数，参数表示角色，则其中任何一个角色可以访问
     * hasAuthority        |   如果有参数，参数表示权限，则其权限可以访问
     * hasIpAddress        |   如果有参数，参数表示IP地址，如果用户IP和参数匹配，则可以访问
     * hasRole             |   如果有参数，参数表示角色，则其角色可以访问
     * permitAll           |   用户可以任意访问
     * rememberMe          |   允许通过remember-me登录的用户访问
     * authenticated       |   用户登录后可访问
     */
    public void configure(HttpSecurity httpSecurity) throws Exception {
        ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry
                registry = httpSecurity.authorizeRequests();
        userTokenService.getAuthWhitelist().forEach(url -> registry.antMatchers(url).permitAll());
        httpSecurity
                // CSRF禁用，因为不使用session
                .csrf().disable()
                // 禁用HTTP响应标头
                .headers().cacheControl().disable().and()
                // 认证失败处理类
                .exceptionHandling().authenticationEntryPoint(new AuthenticationEntryPointImpl())
                .and()
                // 基于token，所以不需要session
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
                // 过滤请求
                .authorizeRequests()
                // 对于登录login 注册register 验证码captchaImage 允许匿名访问
                .antMatchers(JwtConstants.AuthWhiteList.AUTH_WHITELIST).permitAll()
                // 静态资源，可匿名访问
                .antMatchers(HttpMethod.GET, JwtConstants.AuthWhiteList.STATIC_WHITELIST).anonymous()
                // 除上面外的所有请求全部需要鉴权认证
                .anyRequest().authenticated()
                .and()
                .formLogin().disable()
                .headers().frameOptions()
                .disable();
        // 添加Logout filter
        httpSecurity.logout().logoutRequestMatcher(new AntPathRequestMatcher(tokenProperties.getLogoutUrl()))
                .logoutSuccessHandler(logoutSuccessHandler).permitAll();
        // 添加JWT filter
        new TokenAuthenticationConfigurer().configure(httpSecurity);
        // 添加CORS filter
        httpSecurity.addFilterBefore(corsFilter, JwtAuthenticationTokenFilter.class);
        httpSecurity.addFilterBefore(corsFilter, LogoutFilter.class);
    }

    public void configure(AuthenticationManagerBuilder builder) throws Exception {
        builder.userDetailsService(swakUserDetailsService);
        builder.authenticationProvider(new SmsCodeAuthenticationProvider(swakUserDetailsService));
    }


    public void configure(WebSecurity web) {
        Set<String> authWhitelist = userTokenService.getAuthWhitelist();
        web.ignoring().antMatchers(authWhitelist.toArray(new String[0]));
        web.ignoring().antMatchers(HttpMethod.GET,userTokenService.getStaticWhitelist().toArray(new String[0]));
    }

    public SwakWebSecurityConfigurer addWhitelist(String... antPatterns) {
        if (antPatterns != null) {
            whitelistConfig.getAuthWhitelist().addAll(Sets.newHashSet(antPatterns));
        }
        return this;
    }

    public SwakWebSecurityConfigurer addStaticWhitelist(String... antPatterns) {
        if (antPatterns != null) {
            whitelistConfig.getStaticWhitelist().addAll(Sets.newHashSet(antPatterns));
        }
        return this;
    }
}
