package com.swak.common.exception;

import com.swak.common.enums.BasicErrCode;
import com.swak.common.enums.IResultCode;
import com.swak.common.exception.core.BaseException;

/**
 * @author colley.ma
 * @since 3.0.0
 */
public class DistributedLockException extends BaseException {

    public DistributedLockException() {
        super();
    }

    protected DistributedLockException(Throwable cause) {
        super(cause);
    }

    public DistributedLockException(IResultCode errCode, Object... args) {
        super(errCode, args);
    }

    public DistributedLockException(Integer code, String msg) {
        super(code, msg);
    }

    public DistributedLockException(IResultCode errCode, String errMessage, Object[] args) {
        super(errCode, errMessage, args);
    }


}
