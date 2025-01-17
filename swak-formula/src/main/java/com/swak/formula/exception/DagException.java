package com.swak.formula.exception;

/**
 * DAG异常
 *
 * @author colley
 * @since 1.0
 */
public class DagException extends FormulaRuntimeException {

    /**
     * Instantiates a new Dag exception.
     *
     * @param message the message
     */
    public DagException(String message) {
        super(message);
    }

    /**
     * Instantiates a new Dag exception.
     *
     * @param message the message
     * @param cause   the cause
     */
    public DagException(String message, Throwable cause) {
        super(message, cause);
    }

}
