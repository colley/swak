package com.swak.core.web;

import org.springframework.lang.Nullable;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * @author colley.ma
 * @since 3.0.0
 */
public interface SwakInterceptor {

	int PRIORITY = 100;

	default boolean preHandle(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		return true;
	}

	default void postHandle(HttpServletRequest request, HttpServletResponse response) throws ServletException {
	}

	default void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable Exception ex) throws Exception {
	}

	default int priority() {
		return PRIORITY;
	}
}
