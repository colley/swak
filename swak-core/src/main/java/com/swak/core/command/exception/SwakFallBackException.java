/**
 * Copyright (C), 2018 store
 * Encoding: UTF-8
 * Date: 19-10-25 下午1:56
 * History:
 */
package com.swak.core.command.exception;

/**
 * MarsFallBackException.java
 *
 * @author ColleyMa
 * @version 19-10-25 下午1:56
 */
public class SwakFallBackException extends RuntimeException {
    private static final long serialVersionUID = -5864728012203299643L;

    public SwakFallBackException() {
        super("No fallback available.");
    }

    public SwakFallBackException(String e) {
        super(e);
    }

    public SwakFallBackException(Throwable e) {
        super(e);
    }

    public SwakFallBackException(String message, Throwable cause) {
        super(message, cause);
    }
}
