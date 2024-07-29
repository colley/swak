package com.swak.common.dto;

import com.swak.common.enums.BasicErrCode;
import com.swak.common.enums.IResultCode;
import org.apache.commons.lang3.ArrayUtils;

/**
 * Response
 *
 * @author colley.ma
 * @version v1.0
 * @since 2024/3/11 15:24
 **/
public interface Response<T> extends java.io.Serializable {

    /**
     *  code
     * @return Integer
     */
    Integer getCode();

    /**
     * message
     * @return String
     */
    String getMsg();

    /**
     * 返回 data
     * @return T
     */
    T getData();

    void setMsg(String msg);

    void setCode(Integer code);

    void setData(T data);

    /**
     *  0=成功。900=框架异常 901=系统异常 902=
     */
   default boolean isSuccess() {
        return BasicErrCode.SUCCESS.eq(getCode());
    }

    static <T> Response<T> build(IResultCode errCode) {
        return new Result<>(errCode);
    }

    static <T> Response<T> build(Integer code, String msg) {
        return new Result<>(code, msg);
    }

    static <T> Response<T> build() {
        return build(BasicErrCode.SUCCESS);
    }

    static <T> Response<T> fail(Integer code, String msg) {
        return new Result<>(code, msg);
    }

    static <T> Response<T> fail(IResultCode resultCode, Object... args) {
        return new Result<>(resultCode, args);
    }

    static <T> Response<T> fail(IResultCode resultCode) {
        return new Result<>(resultCode, ArrayUtils.EMPTY_OBJECT_ARRAY);
    }

    static <T> Response<T> success() {
        return success(null);
    }

    static <T> Response<T> success(T data) {
        return new Result<>(data);
    }

    static <U,T> Response<T> compose(Response<U> result) {
       return  Response.build(result.getCode(),result.getMsg());
    }

    default Response<T> apply(IResultCode resultCode) {
        setCode(resultCode.getCode());
        setMsg(resultCode.getMsg());
        return this;
    }

    default Response<T> apply(Integer errCode) {
        setCode(errCode);
        return this;
    }

    default Response<T> apply(String errMessage, Object... args) {
        setMsg(IResultCode.i18nMessage(errMessage, args));
        return this;
    }

    default Response<T> apply(Integer errCode, String errMessage, Object... args) {
        return this.apply(errCode).apply(errMessage, args);
    }

    default Response<T> apply(Integer errCode, String errMessage) {
        return this.apply(errCode).apply(errMessage);
    }

    default Response<T> apply(T data) {
        setData(data);
        return this;
    }

    default <U> Response<U> copy() {
        Response<U> response = new Result<>();
        response.setMsg(this.getMsg());
        response.setCode(this.getCode());
        return response;
    }
}
