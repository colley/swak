
package com.swak.core.handler;

public interface Handler<R, I,C> {
	/**
	 * 预处理
	 *
	 * @param command the command
	 * @param context the context
	 * @return the r
	 */
	default R nextHandle(I command,C context) {
		return invoke(command,context);
	}

	/**
	 * 逻辑处理
	 *
	 * @param command the command
	 * @param context the context
	 * @return the r
	 */
	  R invoke(I command,C context);


	/**
	 * 兜底
	 *
	 * @param command the input
	 * @return o
	 */
	default R fallback(I command,C context) {
		return null;
	}
}
