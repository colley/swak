package com.swak.i18n.util;

import java.lang.reflect.Method;
import java.security.PrivilegedAction;

/**
 * @author colley.ma
 * @since 3.0.0
 */
public class GetMethod implements PrivilegedAction<Method> {
    private final Class<?> clazz;
    private final String methodName;

    public static GetMethod action(Class<?> clazz, String methodName) {
        return new GetMethod(clazz, methodName);
    }

    private GetMethod(Class<?> clazz, String methodName) {
        this.clazz = clazz;
        this.methodName = methodName;
    }

    @Override
    public Method run() {
        try {
            return clazz.getMethod(methodName);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }
}