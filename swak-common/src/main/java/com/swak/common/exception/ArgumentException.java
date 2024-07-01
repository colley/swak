package com.swak.common.exception;

import com.swak.common.enums.BasicErrCode;
import com.swak.common.enums.IResultCode;
import com.swak.common.exception.core.BaseException;

/**
 * 参数异常
 *
 * @author colley.ma
 * @since  2022/9/20 14:21
 */
public class ArgumentException extends BaseException {

    public ArgumentException() {
        super();
        this.setErrCode(BasicErrCode.INVALID_PARAMETER);
    }

    public ArgumentException(Throwable cause) {
        super(cause);
        this.setErrCode(BasicErrCode.INVALID_PARAMETER);
    }

    public ArgumentException(String errMessage) {
        super(errMessage);
        this.setErrCode(BasicErrCode.INVALID_PARAMETER);
    }

    public ArgumentException(String errMessage, Throwable e) {
        super(BasicErrCode.INVALID_PARAMETER,errMessage, e);
    }


    public ArgumentException(IResultCode errCode, String errMessage,Object[] args) {
        super(errCode,errMessage,args);
    }

    public ArgumentException(IResultCode errCode,Object... args) {
        super(errCode,args);
    }


    public ArgumentException(Integer errCode, String errMessage) {
        super(errCode, errMessage);
    }
}
