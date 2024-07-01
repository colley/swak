package com.swak.core.command.closure;

import java.lang.reflect.Method;

import com.swak.core.command.annotation.ExtAsyncResult;


public interface SwakClosureFactory {

    SwakClosure createClosure(final Method method, final Object o, final Object... args);


    /**
     * Checks that closureObj is instance of necessary class.
     *
     * @param closureObj the instance of an anonymous class
     * @return true of closureObj has expected type, otherwise - false
     */
    boolean isClosureCommand(final Object closureObj);

    /**
     * Gets type of expected closure type.
     *
     * @return closure (anonymous class) type
     */
    @SuppressWarnings("rawtypes")
    Class<? extends ExtAsyncResult> getSwakCommandType();

}
