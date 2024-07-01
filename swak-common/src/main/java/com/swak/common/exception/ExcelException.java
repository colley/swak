package com.swak.common.exception;

import com.swak.common.enums.BasicErrCode;
import com.swak.common.enums.IResultCode;
import com.swak.common.exception.core.BaseException;

public class ExcelException extends BaseException {

    public ExcelException() {
        super();
    }

    public ExcelException(String message) {
        super(message);
    }

    public ExcelException(String message, Throwable e) {
        super(message, e);
    }

    protected ExcelException(Throwable cause) {
        super(cause);
    }

    public ExcelException(IResultCode errCode, Object... args) {
        super(errCode, args);
    }

    public ExcelException(Integer code, String msg) {
        super(code, msg);
    }

    public ExcelException(IResultCode errCode, String errMessage, Object[] args) {
        super(errCode, errMessage, args);
    }

    public ExcelException(IResultCode errCode, Throwable e) {
        super(errCode,errCode.getI18nMsg(), e);
    }
}