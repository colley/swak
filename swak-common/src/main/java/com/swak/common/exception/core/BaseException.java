package com.swak.common.exception.core;

import com.swak.common.enums.BasicErrCode;
import com.swak.common.enums.IResultCode;
import com.swak.common.util.GetterUtil;
import org.apache.commons.lang3.ArrayUtils;

import java.text.MessageFormat;

/**
 * Base Exception is the parent of all exceptions
 *
 * @author colley.ma
 * @since 2022/07/14
 */
public abstract class BaseException extends RuntimeException {

    private Integer code;

    private String msg;

    protected BaseException() {
        super();
    }

    protected BaseException(Throwable cause) {
        super(cause);
        this.msg = cause.getMessage();
    }

    public BaseException(Integer code, String msg) {
        super(msg);
        this.msg = msg;
        this.code = code;
    }

    public BaseException(IResultCode errCode, String errMessage,Object[] args) {
        this(errCode.getCode(),errMessage,args);
    }

    public BaseException(Integer errCode, String errMessage,Object[] args) {
        super(format(errMessage,args));
        this.msg = super.getMessage();
        this.setErrCode(errCode);
    }


    public BaseException(IResultCode errorCode,String errMessage, Throwable e) {
        super(errMessage,e);
        this.setErrCode(errorCode);
    }

    public BaseException(IResultCode errCode, Object... args) {
        super(errCode.getI18nMsg(args));
        this.setErrCode(errCode,args);
    }


    public BaseException(String msg) {
        super(msg);
        this.msg = msg;
    }

    public BaseException(String msg, Throwable e) {
        super(msg, e);
        this.msg = msg;
    }

    public Integer getErrCode() {
        return code;
    }

    public void setErrCode(IResultCode errCode,Object... args) {
        this.code = errCode.getCode();
        this.msg = GetterUtil.getString(this.msg, errCode.getI18nMsg(args));
    }

    public void setErrCode(Integer errCode) {
        this.code = errCode;
    }

    public String getErrMessage() {
        return this.msg;
    }

    static String format(String message, Object... args) {
        if (ArrayUtils.isNotEmpty(args)) {
            return MessageFormat.format(message, args);
        }
        return message;
    }

}
