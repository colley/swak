package com.swak.autoconfigure.interceptor;

import com.swak.common.dto.RequestContext;
import com.swak.common.util.TraceIdUtil;
import com.swak.core.web.SwakInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

/**
 * SwakHttpInterceptor.java
 *
 * @author colley.ma
 * @since 2.3.3
 */
@Slf4j
public class SwakHttpInterceptor implements SwakInterceptor {
	private final String systemAppCode;

	private final String traceIdPre;

	private final String userKey;

	public SwakHttpInterceptor(String systemAppCode,String userKey, String traceIdPre) {
		this.traceIdPre = traceIdPre;
		this.userKey = userKey;
		this.systemAppCode = Optional.ofNullable(systemAppCode).orElse("__UNSET__");
		TraceIdUtil.setTracePre(traceIdPre);
	}

	public SwakHttpInterceptor() {
		this("__UNSET__", "swak", "userId");
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response) {
		RequestContext.getContext();
		// 生成TraceId
		String traceId = request.getHeader(TraceIdUtil.TRACE_ID);
		if (StringUtils.isBlank(traceId)) {
			traceId = TraceIdUtil.generateTraceId(traceIdPre);
			TraceIdUtil.setTraceId(traceId);
		} else {
			TraceIdUtil.setTraceId(traceId);
		}
		// 生成请求时间
		long requestTime = System.currentTimeMillis();
		String requestUserId = request.getHeader(Optional.ofNullable(this.userKey).orElse("userId"));
		if (StringUtils.isBlank(requestUserId)) {
			RequestContext.getContext().setRequestUserId(requestUserId);
		}
		String trxId = request.getParameter("trxId");
		String requestAppCode = request.getParameter("appCode");
		// 存储到上下文
		RequestContext.getContext().setTraceId(traceId);
		RequestContext.getContext().setRequestTime(requestTime);
		RequestContext.getContext().setTrxId(trxId);
		RequestContext.getContext().setRequestAppCode(requestAppCode);
		RequestContext.getContext().setSystemAppCode(systemAppCode);
		RequestContext.getContext().setEnableFusionFilter(!StringUtils.isBlank(requestAppCode) &&
				!systemAppCode.equalsIgnoreCase("__UNSET__")
				&& !requestAppCode.equalsIgnoreCase(systemAppCode));
		// 打印追踪Meta日志
		log.info("requestTime={}, requestUserId={}, requestAppCode={}, systemAppCode={}, enableFusionFilter={}", requestTime, requestUserId, requestAppCode, systemAppCode, RequestContext.getContext().getEnableFusionFilter());
		return true;
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable Exception ex) throws Exception {
		RequestContext.removeContext();
		TraceIdUtil.remove();
	}

	@Override
	public int priority() {
		return -5;
	}
}
