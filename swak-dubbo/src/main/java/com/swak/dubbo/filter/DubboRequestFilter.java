package com.swak.dubbo.filter;

import com.swak.common.dto.RequestContext;
import com.swak.common.util.TraceIdUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.*;

import java.net.InetAddress;

@Activate
public class DubboRequestFilter implements Filter {
	@Override
	public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
		if (RpcContext.getContext().isConsumerSide()) {
			processOnConsumerSide();
		} else {
			processOnProviderSide();
		}
		return invoker.invoke(invocation);
	}

	private void processOnConsumerSide() {
		String traceId = RequestContext.getContext().getTraceId();
		if (StringUtils.isEmpty(traceId)) {
			RequestContext.createContext();
			traceId = TraceIdUtil.getTraceId();
			long requestTime = System.currentTimeMillis();
			String requestHostname = getCurrentHostname();

			RequestContext.getContext().setTraceId(traceId);
			RequestContext.getContext().setRequestTime(requestTime);
			RequestContext.getContext().setRequestHostname(requestHostname);
		}
		setRpcAttachment("traceId", RequestContext.getContext().getTraceId());
		setRpcAttachment("trxId", RequestContext.getContext().getTrxId());
		setRpcAttachment("requestTime", RequestContext.getContext().getRequestTime());
		setRpcAttachment("requestHostname", RequestContext.getContext().getRequestHostname());
		setRpcAttachment("requestUserId", RequestContext.getContext().getRequestUserId());
		setRpcAttachment("requestLanguage", RequestContext.getContext().getRequestLanguage());
		setRpcAttachment("upstreamTraceLevel", "1");
	}

	private void processOnProviderSide() {
		RequestContext.createContext();
		RequestContext.getContext().setTraceId(RpcContext.getContext().getAttachment("traceId"));
		RequestContext.getContext().setTrxId(RpcContext.getContext().getAttachment("trxId"));
		RequestContext.getContext().setRequestHostname(RpcContext.getContext().getAttachment("requestHostname"));
		RequestContext.getContext().setRequestUserId(RpcContext.getContext().getAttachment("requestUserId"));
		RequestContext.getContext().setRequestLanguage(RpcContext.getContext().getAttachment("requestLanguage"));

		String requestTime = RpcContext.getContext().getAttachment("requestTime");
		if (StringUtils.isNotEmpty(requestTime)) {
			RequestContext.getContext().setRequestTime(Long.valueOf(requestTime));
		}
		String upstreamTraceLevel = RpcContext.getContext().getAttachment("upstreamTraceLevel");
		if (StringUtils.isNotEmpty(upstreamTraceLevel)) {
			RequestContext.getContext().setUpstreamTraceLevel(Integer.valueOf(upstreamTraceLevel));
		}
	}

	private void setRpcAttachment(String key, Object value) {
		if (value == null) {
			return;
		}
		String valueString = value.toString();
		if (StringUtils.isEmpty(valueString)) {
			return;
		}
		RpcContext.getContext().setAttachment(key, valueString);
	}

	private String getCurrentHostname() {
		try {
			return InetAddress.getLocalHost().getHostName();
		} catch (Exception e) {
			return "";
		}
	}
}