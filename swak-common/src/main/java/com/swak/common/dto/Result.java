package com.swak.common.dto;

import com.swak.common.enums.BasicErrCode;
import com.swak.common.enums.IResultCode;

/**
 * @author colley
 */
public class Result<T> implements Response<T> {
    private Integer code;
    private String msg;
    private T data;

    public Result(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public Result(T data) {
        this();
        this.data = data;
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
        return BasicErrCode.SUCCESS.eq(getCode());
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
}
