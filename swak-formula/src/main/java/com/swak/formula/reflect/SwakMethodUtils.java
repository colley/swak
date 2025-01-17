package com.swak.formula.reflect;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ClassUtils;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class SwakMethodUtils {

    public static Object invokeMethod(final Object object, final Method method) throws IllegalAccessException, InvocationTargetException {
        return invokeMethod(object, method, ArrayUtils.EMPTY_OBJECT_ARRAY, null);
    }

    public static Object invokeMethod(final Object object,Method method, Object[] args, Class<?>[] parameterTypes) throws InvocationTargetException, IllegalAccessException {
        Object[] lastArgs = toVarArgs(method, ArrayUtils.nullToEmpty(args));
        return method.invoke(object, lastArgs);
    }


    private static Object[] toVarArgs(final Method method, Object[] args) {
        if (method.isVarArgs()) {
            final Class<?>[] methodParameterTypes = method.getParameterTypes();
            args = getVarArgs(args, methodParameterTypes);
        }
        return args;
    }

    static Object[] getVarArgs(final Object[] args, final Class<?>[] methodParameterTypes) {
        if (args.length == methodParameterTypes.length
                && args[args.length - 1].getClass().equals(methodParameterTypes[methodParameterTypes.length - 1])) {
            // The args array is already in the canonical form for the method.
            return args;
        }

        // Construct a new array matching the method's declared parameter types.
        final Object[] newArgs = new Object[methodParameterTypes.length];

        // Copy the normal (non-varargs) parameters
        System.arraycopy(args, 0, newArgs, 0, methodParameterTypes.length - 1);

        // Construct a new array for the variadic parameters
        final Class<?> varArgComponentType = methodParameterTypes[methodParameterTypes.length - 1].getComponentType();
        final int varArgLength = args.length - methodParameterTypes.length + 1;

        Object varArgsArray = Array.newInstance(ClassUtils.primitiveToWrapper(varArgComponentType), varArgLength);
        // Copy the variadic arguments into the varargs array.
        System.arraycopy(args, methodParameterTypes.length - 1, varArgsArray, 0, varArgLength);

        if (varArgComponentType.isPrimitive()) {
            // unbox from wrapper type to primitive type
            varArgsArray = ArrayUtils.toPrimitive(varArgsArray);
        }

        // Store the varargs array in the last position of the array to return
        newArgs[methodParameterTypes.length - 1] = varArgsArray;

        // Return the canonical varargs array.
        return newArgs;
    }
}
