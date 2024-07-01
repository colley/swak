package com.swak.core.command;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.swak.core.command.annotation.SwakExecutionType;
import com.swak.core.command.closure.AsyncClosureFactory;
import com.swak.core.command.closure.SwakClosure;
import com.swak.core.command.exception.SwakExecutionException;
import org.apache.commons.lang3.exception.ExceptionUtils;




public class SwakMethodExecution {

    private static final Object[] EMPTY_ARGS = new Object[] {};

    private final Object object;
    private final Method method;
    private final Object[] _args;


    public SwakMethodExecution(Object object, Method method) {
        this.object = object;
        this.method = method;
        this._args = EMPTY_ARGS;
    }

    public SwakMethodExecution(Object object, Method method, Object[] args) {
        this.object = object;
        this.method = method;
        this._args = args;
    }

    public Object getObject() {
        return object;
    }

    public Method getMethod() {
        return method;
    }

    public Object[] getArgs() {
        return _args;
    }

    public Object execute(SwakExecutionType executionType) throws Throwable {
        return executeWithArgs(executionType, _args);
    }

    /**
     * Invokes the method. Also private method also can be invoked.
     *
     * @return result of execution
     */
    public Object executeWithArgs(SwakExecutionType executionType, Object[] args) throws Throwable {
        if (SwakExecutionType.ASYNCHRONOUS == executionType) {
            SwakClosure closure =
                AsyncClosureFactory.getInstance().createClosure(method, object, args);
            return executeClj(closure.getClosureObj(), closure.getClosureMethod());
        }

        return execute(object, method, args);
    }



    /**
     * Invokes the method.
     *
     * @return result of execution
     */
    private Object execute(Object o, Method m, Object... args) throws Throwable {
        Object result = null;
        try {
            m.setAccessible(true); // suppress Java language access
            result = m.invoke(o, args);
        } catch (IllegalAccessException e) {
            propagateCause(e);
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
        return result;
    }

    private Object executeClj(Object o, Method m, Object... args) throws Throwable {
        Object result = null;
        try {
            m.setAccessible(true); // suppress Java language access
            result = m.invoke(o, args);
        } catch (IllegalAccessException e) {
            propagateCause(e);
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
        return result;
    }

    /**
     * Retrieves cause exception and wraps to {@link SwakExecutionException}.
     *
     * @param throwable the throwable
     */
    private void propagateCause(Throwable throwable) throws Throwable {

        Throwable rootException = ExceptionUtils.getRootCause(throwable);
        if (rootException == null) {
            // 不存在多级cause
            throw new SwakExecutionException(throwable.getCause());
        }
        if (rootException instanceof RuntimeException) {
            throw (RuntimeException) rootException;
        }

        else if (rootException instanceof Exception) {
            throw (Exception) rootException;
        }

        throw new SwakExecutionException(rootException);
    }

}
