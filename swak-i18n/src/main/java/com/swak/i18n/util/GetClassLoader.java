package com.swak.i18n.util;

import com.swak.common.exception.SwakAssert;

import java.security.PrivilegedAction;


/**
 * @author colley.ma
 * @since 3.0.0
 */
public final class GetClassLoader implements PrivilegedAction<ClassLoader> {

    private static final GetClassLoader CONTEXT = new GetClassLoader(null);

    private final Class<?> clazz;

    public static GetClassLoader fromContext() {
        return CONTEXT;
    }

    public static GetClassLoader fromClass(Class<?> clazz) {
        SwakAssert.notNull(clazz);
        return new GetClassLoader(clazz);
    }

    private GetClassLoader(Class<?> clazz) {
        this.clazz = clazz;
    }

    @Override
    public ClassLoader run() {
        if (clazz != null) {
            return clazz.getClassLoader();
        } else {
            return Thread.currentThread().getContextClassLoader();
        }
    }
}