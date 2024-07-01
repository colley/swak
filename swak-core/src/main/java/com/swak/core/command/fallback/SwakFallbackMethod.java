package com.swak.core.command.fallback;

import com.swak.core.command.SwakTypeHelper;
import com.swak.core.command.annotation.SwakExecutionType;
import com.swak.core.command.annotation.SwakFallback;
import com.swak.core.command.closure.AsyncClosureFactory;
import com.swak.core.command.closure.SwakClosure;
import com.swak.core.command.exception.SwakFallBackException;
import com.swak.core.interceptor.SwakAnnotationUtils;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SwakFallbackMethod {
    public static final SwakFallbackMethod ABSENT =
            new SwakFallbackMethod(null, "getFallback", false, null);
    private final Method method;
    private final boolean extended;
    private String fallbackMethod;
    private SwakExecutionType executionType;
    private SwakFallback swakFallback;
    private boolean fallbackCommand;

    public SwakFallbackMethod(Method method) {
        this(method, "getFallback", false, null);
    }

    public SwakFallbackMethod(Method method, String fallbackMethod, boolean extended,
                              SwakFallback swakFallback) {
        this.method = method;
        this.extended = extended;
        this.fallbackMethod = fallbackMethod;
        if (method != null) {
            this.executionType = SwakExecutionType.getExecutionType(method.getReturnType());
            this.fallbackCommand =
                    SwakAnnotationUtils.isAnnotationPresent(method, SwakFallback.class);
        }
        this.swakFallback = swakFallback;
    }

    public boolean isCommand() {
        return this.fallbackCommand;
    }

    public boolean isPresent() {
        return method != null;
    }

    public Method getMethod() {
        return method;
    }

    public String getFallbackMethod() {
        return fallbackMethod;
    }

    public void setFallbackMethod(String fallbackMethod) {
        this.fallbackMethod = fallbackMethod;
    }

    public SwakExecutionType getExecutionType() {
        return executionType;
    }

    public Object executeWithArgs(Object object, Object... args) throws Throwable {
        if (SwakExecutionType.ASYNCHRONOUS == getExecutionType()) {
            return executeClj(object, args);
        }
        return execute(object, method, args);
    }

    private Object executeClj(Object o, Object... args) throws Throwable {
        SwakClosure closure = AsyncClosureFactory.getInstance().createClosure(method, o, args);
        return execute(closure.getClosureObj(), closure.getClosureMethod());
    }

    private Object execute(Object o, Method m, Object... args) throws Throwable {
        Object result = null;
        try {
            m.setAccessible(true); // suppress Java language access
            result = m.invoke(o, args);
        } catch (IllegalAccessException e) {
            throw new SwakFallBackException(e.getCause());
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
        return result;
    }

    public boolean isExtended() {
        return extended;
    }

    public void validateReturnType(Method commandMethod) {
        if (isPresent()) {
            Class<?> commandReturnType = commandMethod.getReturnType();

            if (SwakExecutionType.ASYNCHRONOUS == SwakExecutionType
                    .getExecutionType(commandReturnType, swakFallback.async())) {
                if (isCommand() && SwakExecutionType.ASYNCHRONOUS == getExecutionType()) {
                    validateReturnType(commandMethod, method);
                }
                if (SwakExecutionType.ASYNCHRONOUS != getExecutionType()) {
                    Type commandParametrizedType = commandMethod.getGenericReturnType();
                    if (SwakTypeHelper.isReturnTypeParametrized(commandMethod)) {
                        commandParametrizedType = getFirstParametrizedType(commandMethod);
                    }
                    validateParametrizedType(commandParametrizedType, method.getGenericReturnType(),
                            commandMethod, method);
                }
                if (!isCommand() && SwakExecutionType.ASYNCHRONOUS == getExecutionType()) {
                    throw new SwakFallBackException(createErrorMsg(commandMethod, method,
                            "fallback cannot return Future if the fallback isn't command when the command is async."));
                }
            } else {
                if (SwakExecutionType.ASYNCHRONOUS == getExecutionType()) {
                    throw new SwakFallBackException(createErrorMsg(commandMethod, method,
                            "fallback cannot return Future if command isn't asynchronous."));
                }
                validateReturnType(commandMethod, method);
            }

        }
    }

    private Type getFirstParametrizedType(Method m) {
        Type gtype = m.getGenericReturnType();
        if (gtype instanceof ParameterizedType) {
            ParameterizedType pType = (ParameterizedType) gtype;
            return pType.getActualTypeArguments()[0];
        }
        return null;
    }

    private void validateReturnType(Method commandMethod, Method fallbackMethod) {
        if (SwakTypeHelper.isReturnTypeParametrized(commandMethod)) {
            List<Type> commandParametrizedTypes = getParametrizedTypes(commandMethod);
            List<Type> fallbackParametrizedTypes = getParametrizedTypes(fallbackMethod);
            List<String> msg =
                    equalsParametrizedTypes(commandParametrizedTypes, fallbackParametrizedTypes);
            if (!msg.isEmpty()) {
                throw new SwakFallBackException(
                        createErrorMsg(commandMethod, method, StringUtils.join(msg, ", ")));
            }
        }
        validatePlainReturnType(commandMethod, fallbackMethod);
    }

    private void validatePlainReturnType(Method commandMethod, Method fallbackMethod) {
        validatePlainReturnType(commandMethod.getReturnType(), fallbackMethod.getReturnType(),
                commandMethod, fallbackMethod);
    }

    private void validatePlainReturnType(Class<?> commandReturnType, Class<?> fallbackReturnType,
                                         Method commandMethod, Method fallbackMethod) {
        if (!commandReturnType.isAssignableFrom(fallbackReturnType)) {
            throw new SwakFallBackException(
                    createErrorMsg(commandMethod, fallbackMethod, "Fallback method '" + fallbackMethod
                            + "' must return: " + commandReturnType + " or it's subclass"));
        }
    }

    private void validateParametrizedType(Type commandReturnType, Type fallbackReturnType,
                                          Method commandMethod, Method fallbackMethod) {
        if (!commandReturnType.equals(fallbackReturnType)) {
            throw new SwakFallBackException(
                    createErrorMsg(commandMethod, fallbackMethod, "Fallback method '" + fallbackMethod
                            + "' must return: " + commandReturnType + " or it's subclass"));
        }
    }

    private String createErrorMsg(Method commandMethod, Method fallbackMethod, String hint) {
        return "Incompatible return types. Command method: " + commandMethod + ", fallback method: "
                + fallbackMethod + ". " + (StringUtils.isNotBlank(hint) ? "Hint: " : "");
    }

    private List<Type> getParametrizedTypes(Method m) {
        return SwakTypeHelper.getAllParameterizedTypes(m.getGenericReturnType());
    }

    private List<String> equalsParametrizedTypes(List<Type> commandParametrizedTypes,
                                                 List<Type> fallbackParametrizedTypes) {
        List<String> msg = Collections.emptyList();
        if (commandParametrizedTypes.size() != fallbackParametrizedTypes.size()) {
            return Collections.singletonList(
                    "a different set of parametrized types, command: " + commandParametrizedTypes.size()
                            + " fallback: " + fallbackParametrizedTypes.size());
        }

        for (int i = 0; i < commandParametrizedTypes.size(); i++) {
            Type commandParametrizedType = commandParametrizedTypes.get(i);
            Type fallbackParametrizedType = fallbackParametrizedTypes.get(i);
            if (!commandParametrizedType.equals(fallbackParametrizedType)) {
                if (Collections.<String>emptyList() == msg) {
                    msg = new ArrayList<String>();
                }
                msg.add("wrong parametrized type. Expected: '" + commandParametrizedType
                        + "' but in fallback '" + fallbackParametrizedType + "', position: " + i);
                return msg;
            }
        }

        return msg;
    }
}
