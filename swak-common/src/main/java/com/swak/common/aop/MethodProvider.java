package com.swak.common.aop;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.objectweb.asm.Opcodes.*;

/**
 * MethodProvider
 * @author colley.ma
 * @since 2022/9/9 14:28
 */
public final class MethodProvider {

	private MethodProvider() {
	}

	private static final MethodProvider INSTANCE = new MethodProvider();

	public static MethodProvider getInstance() {
		return INSTANCE;
	}

	private Map<Method, Method> cache = new ConcurrentHashMap<Method, Method>();

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
				public MethodVisitor visitMethod(int access, String name, String desc, String signature,
						String[] exceptions) {
					boolean bridge = (access & ACC_BRIDGE) != 0 && (access & ACC_SYNTHETIC) != 0;
					if (bridge && bridgeMethod.getName().equals(name)
							&& getParameterCount(desc) == bridgeMethod.getParameterTypes().length) {
						return new MethodFinder(methodSignature);
					}
					return super.visitMethod(access, name, desc, signature, exceptions);
				}
			}, 0);
			Method method = aClass.getDeclaredMethod(methodSignature.name, methodSignature.getParameterTypes());
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

	private static class MethodFinder extends MethodVisitor {
		private MethodSignature methodSignature;

		public MethodFinder(MethodSignature methodSignature) {
			super(ASM5);
			this.methodSignature = methodSignature;
		}

		@Override
		public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
			methodSignature.name = name;
			methodSignature.desc = desc;
			super.visitMethodInsn(opcode, owner, name, desc, itf);
		}
	}
}
