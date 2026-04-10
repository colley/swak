/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.swak.common.util;


import com.swak.common.exception.SwakAssert;
import com.swak.common.exception.SwakException;

import java.lang.reflect.*;
import java.util.*;

public class ClassUtils {


    private static ClassLoader systemClassLoader;

    static {
        try {
            systemClassLoader = ClassLoader.getSystemClassLoader();
        } catch (SecurityException ignored) {
            // AccessControlException on Google App Engine
        }
    }


    /**
     * Suffix for array class names: "[]"
     */
    public static final String ARRAY_SUFFIX = "[]";
    /**
     * Prefix for internal array class names: "[L"
     */
    private static final String INTERNAL_ARRAY_PREFIX = "[L";
    /**
     * Map with primitive type name as key and corresponding primitive type as
     * value, for example: "int" -> "int.class".
     */
    private static final Map<String, Class<?>> primitiveTypeNameMap = new HashMap<String, Class<?>>(16);
    /**
     * Map with primitive wrapper type as key and corresponding primitive type
     * as value, for example: Integer.class -> int.class.
     */
    private static final Map<Class<?>, Class<?>> primitiveWrapperTypeMap = new HashMap<Class<?>, Class<?>>(8);

    private static final char PACKAGE_SEPARATOR_CHAR = '.';

    static {
        primitiveWrapperTypeMap.put(Boolean.class, boolean.class);
        primitiveWrapperTypeMap.put(Byte.class, byte.class);
        primitiveWrapperTypeMap.put(Character.class, char.class);
        primitiveWrapperTypeMap.put(Double.class, double.class);
        primitiveWrapperTypeMap.put(Float.class, float.class);
        primitiveWrapperTypeMap.put(Integer.class, int.class);
        primitiveWrapperTypeMap.put(Long.class, long.class);
        primitiveWrapperTypeMap.put(Short.class, short.class);

        Set<Class<?>> primitiveTypeNames = new HashSet<>(16);
        primitiveTypeNames.addAll(primitiveWrapperTypeMap.values());
        primitiveTypeNames.addAll(Arrays
                .asList(boolean[].class, byte[].class, char[].class, double[].class,
                        float[].class, int[].class, long[].class, short[].class));
        for (Class<?> primitiveTypeName : primitiveTypeNames) {
            primitiveTypeNameMap.put(primitiveTypeName.getName(), primitiveTypeName);
        }
    }

    public static Class<?> forNameWithThreadContextClassLoader(String name)
            throws ClassNotFoundException {
        return forName(name, Thread.currentThread().getContextClassLoader());
    }

    public static Class<?> forNameWithCallerClassLoader(String name, Class<?> caller)
            throws ClassNotFoundException {
        return forName(name, caller.getClassLoader());
    }

    public static ClassLoader getCallerClassLoader(Class<?> caller) {
        return caller.getClassLoader();
    }


    public static ClassLoader getClassLoader(Class<?> clazz) {
        ClassLoader cl = null;
        try {
            cl = Thread.currentThread().getContextClassLoader();
        } catch (Throwable ex) {
            // Cannot access thread context ClassLoader - falling back to system class loader...
        }
        if (cl == null) {
            // No thread context class loader -> use class loader of this class.
            cl = clazz.getClassLoader();
            if (cl == null) {
                // getClassLoader() returning null indicates the bootstrap ClassLoader
                try {
                    cl = ClassLoader.getSystemClassLoader();
                } catch (Throwable ex) {
                    // Cannot access system ClassLoader - oh well, maybe the caller can live with null...
                }
            }
        }

        return cl;
    }

    /**
     * Return the default ClassLoader to use: typically the thread context
     * ClassLoader, if available; the ClassLoader that loaded the ClassUtils
     * class will be used as fallback.
     * <p>
     * Call this method if you intend to use the thread context ClassLoader in a
     * scenario where you absolutely need a non-null ClassLoader reference: for
     * example, for class path resource loading (but not necessarily for
     * <code>Class.forName</code>, which accepts a <code>null</code> ClassLoader
     * reference as well).
     *
     * @return the default ClassLoader (never <code>null</code>)
     * @see java.lang.Thread#getContextClassLoader()
     */
    public static ClassLoader getClassLoader() {
        return getClassLoader(ClassUtils.class);
    }

    /**
     * Same as <code>Class.forName()</code>, except that it works for primitive
     * types.
     */
    public static Class<?> forName(String name) throws ClassNotFoundException {
        return forName(name, getClassLoader());
    }

    /**
     * Replacement for <code>Class.forName()</code> that also returns Class
     * instances for primitives (like "int") and array class names (like
     * "String[]").
     *
     * @param name        the name of the Class
     * @param classLoader the class loader to use (may be <code>null</code>,
     *                    which indicates the default class loader)
     * @return Class instance for the supplied name
     * @throws ClassNotFoundException if the class was not found
     * @throws LinkageError           if the class file could not be loaded
     * @see Class#forName(String, boolean, ClassLoader)
     */
    public static Class<?> forName(String name, ClassLoader classLoader)
            throws ClassNotFoundException, LinkageError {

        Class<?> clazz = resolvePrimitiveClassName(name);
        if (clazz != null) {
            return clazz;
        }

        // "java.lang.String[]" style arrays
        if (name.endsWith(ARRAY_SUFFIX)) {
            String elementClassName = name.substring(0, name.length() - ARRAY_SUFFIX.length());
            Class<?> elementClass = forName(elementClassName, classLoader);
            return Array.newInstance(elementClass, 0).getClass();
        }

        // "[Ljava.lang.String;" style arrays
        int internalArrayMarker = name.indexOf(INTERNAL_ARRAY_PREFIX);
        if (internalArrayMarker != -1 && name.endsWith(";")) {
            String elementClassName = null;
            if (internalArrayMarker == 0) {
                elementClassName = name
                        .substring(INTERNAL_ARRAY_PREFIX.length(), name.length() - 1);
            } else if (name.startsWith("[")) {
                elementClassName = name.substring(1);
            }
            Class<?> elementClass = forName(elementClassName, classLoader);
            return Array.newInstance(elementClass, 0).getClass();
        }

        ClassLoader classLoaderToUse = classLoader;
        if (classLoaderToUse == null) {
            classLoaderToUse = getClassLoader();
        }
        return classLoaderToUse.loadClass(name);
    }

    /**
     * Resolve the given class name as primitive class, if appropriate,
     * according to the JVM's naming rules for primitive classes.
     * <p>
     * Also supports the JVM's internal class names for primitive arrays. Does
     * <i>not</i> support the "[]" suffix notation for primitive arrays; this is
     * only supported by {@link #forName}.
     *
     * @param name the name of the potentially primitive class
     * @return the primitive class, or <code>null</code> if the name does not
     * denote a primitive class or primitive array class
     */
    public static Class<?> resolvePrimitiveClassName(String name) {
        Class<?> result = null;
        // Most class names will be quite long, considering that they
        // SHOULD sit in a package, so a length check is worthwhile.
        if (name != null && name.length() <= 8) {
            // Could be a primitive - likely.
            result = (Class<?>) primitiveTypeNameMap.get(name);
        }
        return result;
    }

    public static String toShortString(Object obj) {
        if (obj == null) {
            return "null";
        }
        return obj.getClass().getSimpleName() + "@" + System.identityHashCode(obj);

    }

    public static String simpleClassName(Class<?> clazz) {
        if (clazz == null) {
            throw new NullPointerException("clazz");
        }
        String className = clazz.getName();
        final int lastDotIdx = className.lastIndexOf(PACKAGE_SEPARATOR_CHAR);
        if (lastDotIdx > -1) {
            return className.substring(lastDotIdx + 1);
        }
        return className;
    }


    public static boolean isPrimitive(Class<?> type) {
        return type.isPrimitive()
                || type == String.class
                || type == Character.class
                || type == Boolean.class
                || type == Byte.class
                || type == Short.class
                || type == Integer.class
                || type == Long.class
                || type == Float.class
                || type == Double.class
                || type == Object.class;
    }

    public static Object convertPrimitive(Class<?> type, String value) {
        if (value == null) {
            return null;
        } else if (type == char.class || type == Character.class) {
            return value.length() > 0 ? value.charAt(0) : '\0';
        } else if (type == boolean.class || type == Boolean.class) {
            return Boolean.valueOf(value);
        }
        try {
            if (type == byte.class || type == Byte.class) {
                return Byte.valueOf(value);
            } else if (type == short.class || type == Short.class) {
                return Short.valueOf(value);
            } else if (type == int.class || type == Integer.class) {
                return Integer.valueOf(value);
            } else if (type == long.class || type == Long.class) {
                return Long.valueOf(value);
            } else if (type == float.class || type == Float.class) {
                return Float.valueOf(value);
            } else if (type == double.class || type == Double.class) {
                return Double.valueOf(value);
            }
        } catch (NumberFormatException e) {
            return null;
        }
        return value;
    }


    /**
     * We only check boolean value at this moment.
     *
     * @param type
     * @param value
     */
    public static boolean isTypeMatch(Class<?> type, String value) {
        if ((type == boolean.class || type == Boolean.class)
                && !("true".equals(value) || "false".equals(value))) {
            return false;
        }
        return true;
    }

    public static Class<?> getClass(Type type) {
        if (null != type) {
            if (type instanceof Class) {
                return (Class<?>) type;
            } else if (type instanceof ParameterizedType) {
                return (Class<?>) ((ParameterizedType) type).getRawType();
            } else if (type instanceof TypeVariable) {
                Type[] bounds = ((TypeVariable<?>) type).getBounds();
                if (bounds.length == 1) {
                    return getClass(bounds[0]);
                }
            } else if (type instanceof WildcardType) {
                final Type[] upperBounds = ((WildcardType) type).getUpperBounds();
                if (upperBounds.length == 1) {
                    return getClass(upperBounds[0]);
                }
            }
        }
        return null;
    }


    /**
     * 代理 class 的名称
     */
    private static final List<String> PROXY_CLASS_NAMES = Arrays.asList("net.sf.cglib.proxy.Factory"
            // cglib
            , "org.springframework.cglib.proxy.Factory"
            , "javassist.util.proxy.ProxyObject"
            // javassist
            , "org.apache.ibatis.javassist.util.proxy.ProxyObject");

    private ClassUtils() {
    }

    /**
     * 判断传入的类型是否是布尔类型
     *
     * @param type 类型
     * @return 如果是原生布尔或者包装类型布尔，均返回 true
     */
    public static boolean isBoolean(Class<?> type) {
        return type == boolean.class || Boolean.class == type;
    }

    /**
     * 判断是否为代理对象
     *
     * @param clazz 传入 class 对象
     * @return 如果对象class是代理 class，返回 true
     */
    public static boolean isProxy(Class<?> clazz) {
        if (clazz != null) {
            for (Class<?> cls : clazz.getInterfaces()) {
                if (PROXY_CLASS_NAMES.contains(cls.getName())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * <p>
     * 获取当前对象的 class
     * </p>
     *
     * @param clazz 传入
     * @return 如果是代理的class，返回父 class，否则返回自身
     */
    public static Class<?> getUserClass(Class<?> clazz) {
        SwakAssert.notNull(clazz, "Class must not be null");
        return isProxy(clazz) ? clazz.getSuperclass() : clazz;
    }

    /**
     * <p>
     * 获取当前对象的class
     * </p>
     *
     * @param object 对象
     * @return 返回对象的 user class
     */
    public static Class<?> getUserClass(Object object) {
        SwakAssert.notNull(object, "Instance must not be null");
        return getUserClass(object.getClass());
    }

    /**
     * <p>
     * 根据指定的 class ， 实例化一个对象，根据构造参数来实例化
     * </p>
     * <p>
     * 在 java9 及其之后的版本 Class.newInstance() 方法已被废弃
     * </p>
     */
    public static <T> T newInstance(Class<T> clazz) {
        try {
            Constructor<T> constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                NoSuchMethodException e) {
            throw new SwakException("实例化对象时出现错误,请尝试给" + clazz.getName() + " 添加无参的构造方法", e);
        }
    }

    /**
     * 实例化对象.
     *
     * @param clazzName 类名
     * @param <T>       类型
     * @return 实例
     * @since 3.3.2
     */
    @SuppressWarnings("unchecked")
    public static <T> T newInstance(String clazzName) {
        return (T) newInstance(toClassConfident(clazzName));
    }


    /**
     * <p>
     * 请仅在确定类存在的情况下调用该方法
     * </p>
     *
     * @param name 类名称
     * @return 返回转换后的 Class
     */
    public static Class<?> toClassConfident(String name) {
        return toClassConfident(name, null);
    }

    /**
     * @param name
     * @param classLoader
     * @since 3.4.3
     */
    public static Class<?> toClassConfident(String name, ClassLoader classLoader) {
        try {
            return loadClass(name, getClassLoaders(classLoader));
        } catch (ClassNotFoundException e) {
            throw new SwakException("找不到指定的class！请仅在明确确定会有 class 的时候，调用该方法", e);
        }
    }

    private static Class<?> loadClass(String className, ClassLoader[] classLoaders) throws ClassNotFoundException {
        for (ClassLoader classLoader : classLoaders) {
            if (classLoader != null) {
                try {
                    return Class.forName(className, true, classLoader);
                } catch (ClassNotFoundException e) {
                    // ignore
                }
            }
        }
        throw new ClassNotFoundException("Cannot find class: " + className);
    }


    /**
     * Determine the name of the package of the given class,
     * e.g. "java.lang" for the {@code java.lang.String} class.
     *
     * @param clazz the class
     * @return the package name, or the empty String if the class
     * is defined in the default package
     */
    public static String getPackageName(Class<?> clazz) {
        SwakAssert.notNull(clazz, "Class must not be null");
        return getPackageName(clazz.getName());
    }

    /**
     * Determine the name of the package of the given fully-qualified class name,
     * e.g. "java.lang" for the {@code java.lang.String} class name.
     *
     * @param fqClassName the fully-qualified class name
     * @return the package name, or the empty String if the class
     * is defined in the default package
     */
    public static String getPackageName(String fqClassName) {
        SwakAssert.notNull(fqClassName, "Class name must not be null");
        int lastDotIndex = fqClassName.lastIndexOf(StringPool.DOT);
        return (lastDotIndex != -1 ? fqClassName.substring(0, lastDotIndex) : StringPool.EMPTY);
    }

    /**
     * Return the default ClassLoader to use: typically the thread context
     * ClassLoader, if available; the ClassLoader that loaded the ClassUtils
     * class will be used as fallback.
     * <p>Call this method if you intend to use the thread context ClassLoader
     * in a scenario where you clearly prefer a non-null ClassLoader reference:
     * for example, for class path resource loading (but not necessarily for
     * {@code Class.forName}, which accepts a {@code null} ClassLoader
     * reference as well).
     *
     * @return the default ClassLoader (only {@code null} if even the system
     * ClassLoader isn't accessible)
     * @see Thread#getContextClassLoader()
     * @see ClassLoader#getSystemClassLoader()
     * @since 3.3.2
     */
    @Deprecated
    public static ClassLoader getDefaultClassLoader() {
        ClassLoader cl = null;
        try {
            cl = Thread.currentThread().getContextClassLoader();
        } catch (Throwable ex) {
            // Cannot access thread context ClassLoader - falling back...
        }
        if (cl == null) {
            // No thread context class loader -> use class loader of this class.
            cl = ClassUtils.class.getClassLoader();
            if (cl == null) {
                // getClassLoader() returning null indicates the bootstrap ClassLoader
                try {
                    cl = ClassLoader.getSystemClassLoader();
                } catch (Throwable ex) {
                    // Cannot access system ClassLoader - oh well, maybe the caller can live with null...
                }
            }
        }
        return cl;
    }

    private static ClassLoader[] getClassLoaders(ClassLoader classLoader) {
        return new ClassLoader[]{
                classLoader,
                ClassUtils.getDefaultClassLoader(),
                Thread.currentThread().getContextClassLoader(),
                ClassUtils.class.getClassLoader(),
                systemClassLoader};
    }
}
