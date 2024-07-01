package com.swak.core.command.closure;

import static org.slf4j.helpers.MessageFormatter.format;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.swak.core.command.annotation.ExtAsyncResult;

/**
 * 支持hystrix
 * 
 * @ClassName: AsyncClosureFactory.java
 * @author: colley.ma
 * @date: 2022/01/21
 */
public class AsyncClosureFactory implements SwakClosureFactory {

    static final String ERROR_TYPE_MESSAGE = "return type of '{}' method should be {}.";
    static final String INVOKE_METHOD = "invoke";

    private static final AsyncClosureFactory INSTANCE = new AsyncClosureFactory();

    private AsyncClosureFactory() {}

    public static AsyncClosureFactory getInstance() {
        return INSTANCE;
    }

    @Override
    public SwakClosure createClosure(Method method, Object o, Object... args) {
        try {
            method.setAccessible(true);
            Object closureObj = method.invoke(o, args); // creates instance of an anonymous class
            return createClosure(method.getName(), closureObj);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates closure.
     *
     * @param rootMethodName the name of external method within which closure is
     *        created.
     * @param closureObj the instance of specific anonymous class
     * @return new {@link Closure} instance
     * @throws Exception
     */
    SwakClosure createClosure(String rootMethodName, final Object closureObj) throws Exception {
        if (!isClosureCommand(closureObj)) {
            throw new RuntimeException(
                format(ERROR_TYPE_MESSAGE, rootMethodName, getSwakCommandType().getName())
                    .getMessage());
        }
        Method closureMethod = closureObj.getClass().getMethod(INVOKE_METHOD);
        return new SwakClosure(closureMethod, closureObj);
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    public boolean isClosureCommand(Object closureObj) {
        return closureObj instanceof ExtAsyncResult;
    }

    /**
     * {@inheritDoc}.
     */
    @SuppressWarnings("rawtypes")
    @Override
    public Class<? extends ExtAsyncResult> getSwakCommandType() {
        return ExtAsyncResult.class;
    }

}
