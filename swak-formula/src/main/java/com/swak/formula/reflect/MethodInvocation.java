package com.swak.formula.reflect;

import com.alibaba.fastjson2.JSON;
import com.swak.formula.entity.StateContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 调用对象
 *
 * @author colley
 * @since 1.0
 */
@Slf4j
public class MethodInvocation implements java.io.Serializable {

	private final Method method;
	private final Object target;
	private final int parameterCount;
	private final Class<?>[] parameterTypes;

	/**
	 * Instantiates a new Invocation.
	 *
	 * @param method the method
	 * @param target the target
	 */
	public MethodInvocation(Method method, Object target) {
		this.method = method;
		this.target = target;
		this.parameterCount = method.getParameterCount();
		this.parameterTypes = method.getParameterTypes();
	}

	public boolean hasContextParam() {
		if (ArrayUtils.isNotEmpty(parameterTypes)) {
			return parameterTypes[0].isAssignableFrom(StateContext.class);
		}
		return false;
	}

	/**
	 * Invoke object.
	 *
	 * @param args the args
	 * @return the object
	 * @throws InvocationTargetException the invocation target exception
	 * @throws IllegalAccessException    the illegal access exception
	 */
	public Object invoke(Object[] args)
			throws InvocationTargetException, IllegalAccessException,NoSuchMethodException {
		if (log.isDebugEnabled()) {
			log.debug("{}.{}({}),parameter:{}", target.getClass().getName(), method.getName(),
					method.getParameterTypes(), JSON.toJSONString(args));
		}
		if (parameterCount == 0) {
			return SwakMethodUtils.invokeMethod(target, method);
		}
		if (this.parameterCount == 1) {
			// 如果是数组（不定数组），则作为一个参数传递
			if (parameterTypes[0].isArray() && !args[0].getClass().isArray()) {
				return SwakMethodUtils.invokeMethod(target, method, args, method.getParameterTypes());
			}
			if (args[0].getClass().isArray()) {
				return SwakMethodUtils.invokeMethod(target, method, (Object[]) args[0], method.getParameterTypes());
			}
		}
		// 如果方法没有参数，则忽略传递的参数
		return SwakMethodUtils.invokeMethod(target, method, args, method.getParameterTypes());
	}
}
