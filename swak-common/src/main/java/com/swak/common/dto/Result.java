package com.swak.common.dto;

import com.swak.common.enums.BasicErrCode;
import com.swak.common.enums.IResultCode;
import com.swak.common.util.TraceIdUtil;

import java.util.Optional;

/**
 * @author colley
 */
public class Result<T> implements Response<T> {
	private Integer code;
	private String msg;
	private T data;

	private long timestamp;

	private String traceId;

	private final Boolean success;

	public Result(Integer code, String msg) {
		this(code, msg, BasicErrCode.SUCCESS.eq(code), null);
	}

	public Result(T data) {
		this(BasicErrCode.SUCCESS.getCode(), BasicErrCode.SUCCESS.getI18nMsg(), true, data);
	}

	public Result(IResultCode errCode) {
		this(errCode.getCode(), errCode.getI18nMsg());
	}

	public Result(IResultCode errCode, Object... args) {
		this(errCode.getCode(), errCode.getI18nMsg(args));
	}

	public Result() {
		this(BasicErrCode.SUCCESS);
	}

	public Result(Integer code, String msg, boolean success, T data) {
		this.code = code;
		this.msg = msg;
		this.timestamp = System.currentTimeMillis();
		this.traceId = TraceIdUtil.getTraceId();
		this.success = success;
		this.data = data;
	}

	@Override
	public Integer getCode() {
		return this.code;
	}

	@Override
	public String getMsg() {
		return this.msg;
	}

	@Override
	public T getData() {
		return this.data;
	}

	@Override
	public boolean isSuccess() {
		return Optional.ofNullable(success).orElse(BasicErrCode.SUCCESS.eq(getCode()));
	}

	@Override
	public void setMsg(String msg) {
		this.msg = msg;
	}

	@Override
	public void setCode(Integer code) {
		this.code = code;
	}

	@Override
	public void setData(T data) {
		this.data = data;
	}

	@Override
	public long getTimestamp() {
		return this.timestamp;
	}

	@Override
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	@Override
	public String getTraceId() {
		return this.traceId;
	}

	@Override
	public void setTraceId(String traceId) {
		this.traceId = traceId;
	}


	public static <T> Result<T> success() {
		return success(null);
	}

	public static <T> Result<T> success(T data) {
		return new Result<>(BasicErrCode.SUCCESS.getCode(), BasicErrCode.SUCCESS.getMsg(), true, data);
	}

	public static <T> Result<T> success(T data, String msg) {
		return new Result<>(BasicErrCode.SUCCESS.getCode(), msg, true, data);
	}

	public static <T> Result<T> fail() {
		return new Result<>(BasicErrCode.INTERNAL_ERROR.getCode(), BasicErrCode.INTERNAL_ERROR.getI18nMsg(), false, null);
	}

	public static <T> Result<T> fail(IResultCode resultCode) {
		return new Result<>(resultCode.getCode(), resultCode.getI18nMsg(), false, null);
	}

	public static <T> Result<T> fail(IResultCode resultCode, T data) {
		return new Result<>(resultCode.getCode(), resultCode.getI18nMsg(), false, data);
	}

	public static <T> Result<T> failFormat(IResultCode resultCode, Object... args) {
		return new Result<>(resultCode.getCode(), resultCode.getI18nMsg(args), false, null);
	}

	public static <T> Result<T> fail(Integer code, String msg) {
		return new Result<>(code, msg, false, null);
	}

	public static <U, T> Result<T> compose(Response<U> response) {
		return new Result<>(response.getCode(), response.getMsg(), response.isSuccess(), null);
	}

}
