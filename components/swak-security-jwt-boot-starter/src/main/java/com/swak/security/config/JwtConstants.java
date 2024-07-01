package com.swak.security.config;

/**
 * 通用常量信息
 */
public class JwtConstants {
    /**
     * 所有权限标识
     */
    public static final String ALL_PERMISSION = "*:*:*";

    /**
     * 令牌前缀
     */
    public static final String USER_NAME = "userName";

    public static final String USER_ID = "userId";

    /**
     * 令牌前缀
     */
    public static final String TOKEN_PREFIX = "Bearer ";


    public static final long MILLIS_SECOND = 1000;

    public interface  AuthWhiteList {
        /**
         * 需要放行的URL
         */
        String[] AUTH_WHITELIST = {
                // -- register url
                "/",
                "/login",
                "/register",
                "/**/login",
                "/**/register",
                "/**/captcha",
                // -- swagger ui
                "/v2/api-docs",
                "/swagger-resources",
                "/swagger-resources/**",
                "/swagger-ui.html",
                "/webjars/**"
                // other public endpoints of your API may be appended to this array
        };

        String[] STATIC_WHITELIST = {
                "/",
                "/*.html",
                "/**/*.html",
                "/**/*.css",
                "/**/*.js"};
    }
}
