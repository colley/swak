package com.swak.core.interceptor;

import com.swak.common.exception.ThrowableWrapper;
import org.springframework.lang.Nullable;

@FunctionalInterface
public interface SwakOperationInvoker {

    /**
     * Invoke the swak operation defined by this instance. Wraps any exception
     * that is thrown during the invocation in a {@link ThrowableWrapper}.
     * 
     * @return the result of the operation
     * @throws ThrowableWrapper if an error occurred while invoking the operation
     */
    @Nullable
    Object invoke() throws ThrowableWrapper;

}
