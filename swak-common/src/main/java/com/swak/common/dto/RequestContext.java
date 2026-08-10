package com.swak.common.dto;

import com.alibaba.ttl.TransmittableThreadLocal;
import com.swak.common.util.UUIDHexGenerator;
import lombok.Data;

import java.util.*;

/**
 * RequestContext.java
 *
 * @author colley.ma
 * @since 2.3.3
 */
@Data
public class RequestContext {
	private static final ThreadLocal<RequestContext> TD_REQUEST_CONTEXT = new TransmittableThreadLocal<>();
	private static final RequestContext EMPTY_REQUEST_CONTEXT = new RequestContext();

	private String traceId;
	private Integer upstreamTraceLevel;
	private String requestUserId;
	private String requestHostname;
	private Long requestTime;
	private String requestLanguage;
	private String threadName;
	private String trxId;
	private String requestAppCode;
	private String systemAppCode;
	private Boolean enableFusionFilter;
	private String firstMsdId;
	private Locale locale;
	private Map<String, String> attachments = new HashMap<>();

	public static RequestContext createContext() {
		RequestContext context = new RequestContext();
		context.setThreadName(Thread.currentThread().getName());
		TD_REQUEST_CONTEXT.set(context);
		return context;
	}

	public static RequestContext getContext() {
		RequestContext context = TD_REQUEST_CONTEXT.get();
		if (context == null) {
			return createContext();
		}
		return context;
	}

	public static String getUserId() {
		return getContext().getRequestUserId();
	}

	public static String getI18nLocale() {
		return getContext().getRequestLanguage();
	}

	public static void removeContext() {
		if (TD_REQUEST_CONTEXT.get() != null) {
			TD_REQUEST_CONTEXT.remove();
		}
	}
}
