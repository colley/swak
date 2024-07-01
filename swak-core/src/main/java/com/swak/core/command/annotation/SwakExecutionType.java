package com.swak.core.command.annotation;

import java.util.concurrent.Future;

public enum SwakExecutionType {
    /**
     * Used for FORCE asynchronous
     */
    FORCE_ASYNC,
    /**
     * Used for asynchronous execution of command.
     */
    ASYNCHRONOUS,

    /**
     * Used for synchronous execution of command.
     */
    SYNCHRONOUS;

    /**
     * Gets execution type for specified class type.
     * 
     * @param type the type
     * @return the execution type {@link SwakExecutionType}
     */
    public static SwakExecutionType getExecutionType(Class<?> type, SwakExecutionType async) {
        if (Future.class.isAssignableFrom(type)) {
            return SwakExecutionType.ASYNCHRONOUS;
        }
        if (async != null) {
            return async;
        }
        return SwakExecutionType.SYNCHRONOUS;
    }

    public static SwakExecutionType getExecutionType(Class<?> type) {
        if (Future.class.isAssignableFrom(type)) {
            return SwakExecutionType.ASYNCHRONOUS;
        } else {
            return SwakExecutionType.SYNCHRONOUS;
        }
    }
}
