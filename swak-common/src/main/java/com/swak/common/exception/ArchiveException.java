/**
 * Copyright (C), 2018 store
 * Encoding: UTF-8
 * Date: 19-5-8 下午2:40
 * History:
 */
package com.swak.common.exception;

import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;

/**
 * ArchiveException.java
 * 
 * @author ColleyMa
 * @version 19-5-8 下午2:40
 */
public class ArchiveException extends RuntimeException {
	private static final long serialVersionUID = 4824029102947541570L;

	/**
	 *
	 * 空构造
	 */
	public ArchiveException() {
		super("ArchiveException");
	}

	/**
	 *
	 * 自定义错误日志
	 * 
	 * @param e
	 */
	public ArchiveException(String e) {
		super(e);
	}

	/**
	 * 只抛错误信息
	 * 
	 * @param e
	 */
	public ArchiveException(Throwable e) {
		super(e);
	}

	public static void throwException(String format, Object... arguments) throws ArchiveException {
		FormattingTuple ft = MessageFormatter.arrayFormat(format, arguments);
		throw new ArchiveException(ft.getMessage(), ft.getThrowable());
	}

	public static void throwException(Throwable e) throws ArchiveException {
		throw new ArchiveException(e);
	}

	/**
	 * 两者皆抛
	 * 
	 * @param er
	 * @param e
	 */
	public ArchiveException(String er, Throwable e) {
		super(er, e);
	}
}
