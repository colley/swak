package com.swak.autoconfigure.interceptor;

import com.google.common.collect.Lists;
import com.swak.core.web.SwakInterceptor;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.util.CollectionUtils;
import org.springframework.web.servlet.AsyncHandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * SwakCompositeInterceptor.java
 *
 * @author colley.ma
 * @since 2.3.3
 */
public class SwakCompositeInterceptor implements AsyncHandlerInterceptor {

	private final List<SwakInterceptor> delegates = Lists.newArrayList();

	@Autowired(required = false)
	public void setConfigurers(List<SwakInterceptor> configurers) {
		if (!CollectionUtils.isEmpty(configurers)) {
			delegates.addAll(configurers);
		}
		Collections.sort(delegates, Comparator.comparingInt(SwakInterceptor::priority));
	}

	@Override
	public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) throws ServletException {
		for (SwakInterceptor delegate : delegates) {
			if (!delegate.preHandle(request, response)) {
				return false; // 关键改动：遇到 false 立即返回
			}
		}
		return true;
	}

	@Override
	public void postHandle(@NonNull HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable ModelAndView modelAndView) throws Exception {
		for (SwakInterceptor delegate : delegates) {
			delegate.postHandle(request, response);
		}
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable Exception ex) throws Exception {
		for (SwakInterceptor delegate : delegates) {
			delegate.afterCompletion(request, response,handler,ex);
		}
	}
}
