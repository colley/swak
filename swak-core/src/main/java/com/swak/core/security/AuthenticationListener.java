package com.swak.core.security;


import com.swak.common.listener.SwakEventListener;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 登录成功或者退出的监听
 * <p>
 *     e.g
 *     登录成功之后用户信息放入缓存等
 *     登出成功清除缓存
 * </p>
 * @author colley.ma
 * @version v1.0
 * @since 2024/3/13 10:25
 **/
public interface AuthenticationListener extends SwakEventListener{

    /**
     * 登录成功的处理
     * @param request
     * @param response
     * @param userDetails
     */
    default void onLoginSuccess(HttpServletRequest request, HttpServletResponse response, TokenJwtDetails userDetails) {}

    /**
     * 登出成功的处理
     * @param request
     * @param response
     */
    default void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response) {}
}
