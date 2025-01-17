package com.swak.formula.exception;

/**
 * 注册公式异常
 *
 * @author colley
 * @since 1.0
 */
public class RegistryFunctionException extends FormulaRuntimeException {

    /**
     * Instantiates a new Registry function exception.
     *
     * @param message the message
     */
    public RegistryFunctionException(String message) {
        super(message);
    }

    /**
     * Instantiates a new Registry function exception.
     *
     * @param message the message
     * @param cause   the cause
     */
    public RegistryFunctionException(String message, Throwable cause) {
        super(message, cause);
    }

}
