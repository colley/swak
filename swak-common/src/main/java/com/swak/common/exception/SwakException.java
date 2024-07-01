package com.swak.common.exception;

import com.swak.common.enums.BasicErrCode;
import com.swak.common.enums.IResultCode;
import com.swak.common.exception.core.BaseException;

/**
 * System Exception is unexpected Exception, retry might work again
 *
 * @author colley.ma
 * @since 2022/07/14
 */
public class SwakException extends BaseException {

    public SwakException() {
        super();
        this.setErrCode(BasicErrCode.SWAK_ERROR);
    }

    public SwakException(String errMessage) {
        super(errMessage);
        this.setErrCode(BasicErrCode.SWAK_ERROR);
    }

    public SwakException(IResultCode errCode, String errMessage,Object[] args) {
        super(errCode,errMessage,args);
    }

    public SwakException(Integer errCode, String errMessage,Object[] args) {
        super(errCode,errMessage,args);
    }

    public SwakException(IResultCode errCode,Object... args) {
        super(errCode,args);
    }

    public SwakException(String errMessage, Throwable e) {
        super(BasicErrCode.SWAK_ERROR,errMessage, e);
    }

    public SwakException(Throwable e) {
        super(e);
        this.setErrCode(BasicErrCode.SWAK_ERROR);
    }

    public SwakException(IResultCode errorCode,String errMessage, Throwable e) {
        super(errorCode, errMessage,e);
    }
}
