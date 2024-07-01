package com.swak.core.command.exception;

public class SwakExecutionException extends RuntimeException {

    /**
     * 
     */
    private static final long serialVersionUID = -6420032976522502055L;

    /**
     * Creates exceptions instance with cause is original exception.
     *
     * @param cause the original exception
     */
    public SwakExecutionException(Throwable cause) {
        super(cause);
    }

    /**
     * Default constructor.
     */
    public SwakExecutionException() {}
}
