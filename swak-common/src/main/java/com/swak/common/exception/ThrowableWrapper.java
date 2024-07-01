package com.swak.common.exception;

@SuppressWarnings("serial")
public class ThrowableWrapper extends RuntimeException {

	private final Throwable original;

	public ThrowableWrapper(Throwable original) {
		super(original.getMessage(), original);
		this.original = original;
	}

	public Throwable getOriginal() {
		return this.original;
	}

	public static ThrowableWrapper throwableWrapper(Throwable original) {
		return new ThrowableWrapper(original);
	}
}