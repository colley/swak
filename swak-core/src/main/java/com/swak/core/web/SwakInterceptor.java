package com.swak.core.web;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author colley.ma
 * @since 3.0.0
 */
public interface SwakInterceptor {

    default void preHandle(HttpServletRequest request, HttpServletResponse response,Object result) throws ServletException {
    }

    default void postHandle(HttpServletRequest request, HttpServletResponse response) throws ServletException {
    }
}
