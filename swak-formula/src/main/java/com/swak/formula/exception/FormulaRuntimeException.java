package com.swak.formula.exception;

import com.swak.common.enums.IResultCode;
import com.swak.common.exception.core.BaseException;
import com.swak.formula.common.FormulaResultCode;
import groovy.lang.GroovyRuntimeException;
import lombok.Getter;

/**
 * 公式异常
 *
 * @author colley
 * @since 1.0
 */
public class FormulaRuntimeException extends BaseException {

    @Getter
    private GroovyRuntimeException original;

    public FormulaRuntimeException(String message) {
        super(FormulaResultCode.FORMULA_RUN_ERR, message, (Object[]) null);
    }

    public FormulaRuntimeException(IResultCode resultCode, Throwable cause) {
        super(resultCode, resultCode.getI18nMsg(), cause);
        if (cause instanceof GroovyRuntimeException) {
            this.original = (GroovyRuntimeException) cause;
        }
    }

    public FormulaRuntimeException(IResultCode resultCode) {
        super(resultCode);
    }

    public FormulaRuntimeException(String message, Throwable cause) {
        super(FormulaResultCode.FORMULA_RUN_ERR, message, cause);
        if (cause instanceof GroovyRuntimeException) {
            this.original = (GroovyRuntimeException) cause;
        }
    }
}
