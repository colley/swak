package com.swak.core.extension.executor;

import com.swak.common.dto.base.BizScenario;

import java.util.function.Consumer;
import java.util.function.Function;

public interface ExtensionExecutor {

	default public <R, T> R execute(Class<T> targetClz, BizScenario bizScenario, Function<T, R> exeFunction) {
		T component = locateComponent(targetClz, bizScenario);
		return exeFunction.apply(component);
	}

	@SuppressWarnings("unchecked")
	default public <R, T> R execute(ExtensionCoordinate extensionCoordinate, Function<T, R> exeFunction) {
		return execute((Class<T>) extensionCoordinate.getExtensionPointClass(), extensionCoordinate.getBizScenario(),
				exeFunction);
	}

	/**
	 * Execute extension without Response
	 *
	 * @param targetClz
	 * @param context
	 * @param exeFunction
	 * @param <T>         Parameter Type
	 */
	default public <T> void executeVoid(Class<T> targetClz, BizScenario context, Consumer<T> exeFunction) {
		T component = locateComponent(targetClz, context);
		exeFunction.accept(component);
	}

	@SuppressWarnings("unchecked")
	default public <T> void executeVoid(ExtensionCoordinate extensionCoordinate, Consumer<T> exeFunction) {
		executeVoid(extensionCoordinate.getExtensionPointClass(), extensionCoordinate.getBizScenario(), exeFunction);
	}

	public <C> C locateComponent(Class<C> targetClz, BizScenario context);

	/**
	 * 	获取扩展实例
	 * @param @param <C>
	 * @param @param targetClz
	 * @param @param context
	 * @param @return   
	 * @return C
	 */
	default public <C> C getExtensionPt(Class<C> targetClz, BizScenario context) {
		return locateComponent(targetClz, context);
	}
}
