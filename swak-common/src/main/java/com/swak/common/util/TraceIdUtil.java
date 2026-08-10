package com.swak.common.util;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;

/**
 * TraceIdUtil.java
 *
 * @author colley.ma
 * @since 2.3.3
 */
public class TraceIdUtil {

	public static final  String TRACE_ID = "traceId";

	private static   String tracePre;

	public static void setTracePre(String tracePre) {
		TraceIdUtil.tracePre = tracePre;
	}

	public static  String getTraceId() {
		return GetterUtil.getString(MDC.get(TRACE_ID));
	}

	public static void  setTraceId(String traceId) {
		MDC.put(TRACE_ID,traceId);
	}

	public static void remove() {
		MDC.remove(TRACE_ID);
	}

	public static void clear() {
		MDC.clear();
	}

	public static String generateTraceId(String tracePre) {
		String uuid = UUIDHexGenerator.generator();
		if(StringUtils.isEmpty(tracePre)) {
			return uuid;
		}
		return tracePre+"_"+ uuid;
	}
}
