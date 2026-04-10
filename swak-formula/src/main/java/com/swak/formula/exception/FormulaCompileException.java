package com.swak.formula.exception;

import com.swak.common.enums.IResultCode;
import com.swak.common.exception.core.BaseException;
import com.swak.formula.common.FormulaResultCode;
import lombok.Getter;
import org.codehaus.groovy.control.MultipleCompilationErrorsException;

/**
 * 公式编译异常
 * @author colley
 * @since 1.0
 */
public class FormulaCompileException extends BaseException {

    @Getter
    private MultipleCompilationErrorsException original;

    public FormulaCompileException(IResultCode resultCode, Throwable cause){
        super(resultCode,cause);
        if(cause instanceof MultipleCompilationErrorsException){
            this.original = (MultipleCompilationErrorsException)cause;
        }
    }

    /**
     * Instantiates a new Formula exception.
     *
     * @param message the message
     */
    public FormulaCompileException(String message) {
        super(FormulaResultCode.FORMULA_COMPILE_ERR,message,(Object[]) null);

    }
}
