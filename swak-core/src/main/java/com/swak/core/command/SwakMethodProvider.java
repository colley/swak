/**
 * Copyright 2015 Netflix, Inc.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.swak.core.command;

import com.google.common.base.Optional;
import com.netflix.hystrix.contrib.javanica.utils.FallbackMethod;
import com.swak.core.command.annotation.SwakFallback;
import com.swak.core.command.exception.SwakFallBackException;
import com.swak.core.command.fallback.SwakFallbackMethod;
import com.swak.core.interceptor.SwakAnnotationUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.objectweb.asm.Opcodes.*;

public final class SwakMethodProvider {

    public static final Set<Class<? extends Annotation>> FALLBACK_ANNOTATIONS =
            new LinkedHashSet<>(1);
    private static final SwakMethodProvider INSTANCE = new SwakMethodProvider();

    static {
        FALLBACK_ANNOTATIONS.add(SwakFallback.class);
    }

    private Map<Method, Method> cache = new ConcurrentHashMap<Method, Method>();

    private SwakMethodProvider() {
    }

    public static SwakMethodProvider getInstance() {
        return INSTANCE;
    }

    private static int getParameterCount(String desc) {
        return parseParams(desc).length;
    }

    private static String[] parseParams(String desc) {
        String params = desc.split("\\)")[0].replace("(", "");
        if (params.length() == 0) {
            return new String[0];
        }
        return params.split(";");
    }

    public SwakFallbackMethod getFallbackMethod(Class<?> type, Method commandMethod) {
        return getFallbackMethod(type, commandMethod, false);
    }

    /**
     * Gets fallback method for command method.
     *
     * @param type          type
     * @param commandMethod the command method. in the essence it can be a fallback
     *                      method annotated with HystrixCommand annotation that has
     *                      a fallback as well.
     * @param extended      true if the given commandMethod was derived using
     *                      additional parameter, otherwise - false
     * @return new instance of {@link FallbackMethod} or
     * {@link FallbackMethod#ABSENT} if there is no suitable fallback method
     * for the given command
     */
    public SwakFallbackMethod getFallbackMethod(Class<?> type, Method commandMethod,
                                                boolean extended) {
        SwakFallback swakFallback = null;
        Collection<? extends Annotation> fallbackColls =
                SwakAnnotationUtils.computeOperations(commandMethod, type, FALLBACK_ANNOTATIONS);
        if (CollectionUtils.isEmpty(fallbackColls)) {
            return SwakFallbackMethod.ABSENT;
        }
        swakFallback = (SwakFallback) fallbackColls.iterator().next();
        if (swakFallback == null || StringUtils.isBlank(swakFallback.fallbackMethod())) {
            return SwakFallbackMethod.ABSENT;
        }
        Class<?>[] parameterTypes = commandMethod.getParameterTypes();
        if (extended && parameterTypes[parameterTypes.length - 1] == Throwable.class) {
            parameterTypes = ArrayUtils.remove(parameterTypes, parameterTypes.length - 1);
        }
        Class<?>[] exParameterTypes = Arrays.copyOf(parameterTypes, parameterTypes.length + 1);
        exParameterTypes[parameterTypes.length] = Throwable.class;
        Optional<Method> exFallbackMethod =
                getMethod(type, swakFallback.fallbackMethod(), exParameterTypes);
        Optional<Method> fMethod = getMethod(type, swakFallback.fallbackMethod(), parameterTypes);
        Method method = exFallbackMethod.or(fMethod).orNull();
        if (method == null) {
            throw new SwakFallBackException("fallback method wasn't found: "
                    + swakFallback.fallbackMethod() + "(" + Arrays.toString(parameterTypes) + ")");
        }
        return new SwakFallbackMethod(method, swakFallback.fallbackMethod(),
                exFallbackMethod.isPresent(), swakFallback);
    }

    public Optional<Method> getMethod(Class<?> type, String name, Class<?>... parameterTypes) {
        Method[] methods = type.getDeclaredMethods();
        for (Method method : methods) {
            if (method.getName().equals(name)
                    && Arrays.equals(method.getParameterTypes(), parameterTypes)) {
                return Optional.of(method);
            }
        }
        Class<?> superClass = type.getSuperclass();
        if (superClass != null && !superClass.equals(Object.class)) {
            return getMethod(superClass, name, parameterTypes);
        } else {
            return Optional.absent();
        }
    }

    /**
     * Finds generic method for the given bridge method.
     *
     * @param bridgeMethod the bridge method
     * @param aClass       the type where the bridge method is declared
     * @return generic method
     * @throws IOException
     * @throws NoSuchMethodException
     * @throws ClassNotFoundException
     */
    public Method unbride(final Method bridgeMethod, Class<?> aClass)
            throws IOException, NoSuchMethodException, ClassNotFoundException {
        if (bridgeMethod.isBridge() && bridgeMethod.isSynthetic()) {
            if (cache.containsKey(bridgeMethod)) {
                return cache.get(bridgeMethod);
            }

            ClassReader classReader = new ClassReader(aClass.getName());
            final MethodSignature methodSignature = new MethodSignature();
            classReader.accept(new ClassVisitor(ASM5) {
                @Override
                public MethodVisitor visitMethod(int access, String name, String desc,
                                                 String signature, String[] exceptions) {
                    boolean bridge = (access & ACC_BRIDGE) != 0 && (access & ACC_SYNTHETIC) != 0;
                    if (bridge && bridgeMethod.getName().equals(name)
                            && getParameterCount(desc) == bridgeMethod.getParameterTypes().length) {
                        return new MethodFinder(methodSignature);
                    }
                    return super.visitMethod(access, name, desc, signature, exceptions);
                }
            }, 0);
            Method method =
                    aClass.getDeclaredMethod(methodSignature.name, methodSignature.getParameterTypes());
            cache.put(bridgeMethod, method);
            return method;

        } else {
            return bridgeMethod;
        }
    }

    private static class MethodSignature {
        String name;
        String desc;

        public Class<?>[] getParameterTypes() throws ClassNotFoundException {
            if (desc == null) {
                return new Class[0];
            }
            String[] params = parseParams(desc);
            Class<?>[] parameterTypes = new Class[params.length];

            for (int i = 0; i < params.length; i++) {
                String arg = params[i].substring(1).replace("/", ".");
                parameterTypes[i] = Class.forName(arg);
            }
            return parameterTypes;
        }
    }

    private static class MethodFinder extends MethodVisitor {
        private MethodSignature methodSignature;

        public MethodFinder(MethodSignature methodSignature) {
            super(ASM5);
            this.methodSignature = methodSignature;
        }

        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String desc,
                                    boolean itf) {
            methodSignature.name = name;
            methodSignature.desc = desc;
            super.visitMethodInsn(opcode, owner, name, desc, itf);
        }
    }

}
