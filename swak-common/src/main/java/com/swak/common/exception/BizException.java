package com.swak.common.exception;

import com.swak.common.enums.BasicErrCode;
import com.swak.common.enums.IResultCode;
import com.swak.common.exception.core.BaseException;

/**
 * BizException is known Exception, no need retry
 */
public class BizException extends BaseException {

    public BizException() {
        super();
        this.setErrCode(BasicErrCode.BIZ_ERROR);
    }

    public BizException(String errMessage) {
        super(errMessage);
        this.setErrCode(BasicErrCode.BIZ_ERROR);
    }

    public BizException(IResultCode errCode, String errMessage,Object... args) {
        super(errCode,errMessage,args);
    }

    public BizException(IResultCode errCode,Object... args) {
        super(errCode,args);
    }

    public BizException(Integer code, String msg) {
        super(code,msg);
    }

    public BizException(IResultCode errCode, String errMessage,Throwable e) {
        super(errCode,errMessage,e);
    }

    public BizException(String errMessage, Throwable e) {
        super(BasicErrCode.BIZ_ERROR,errMessage, e);
    }

    public BizException(Throwable e) {
        super(BasicErrCode.BIZ_ERROR.getMsg(), e);
        this.setErrCode(BasicErrCode.BIZ_ERROR);
    }
}