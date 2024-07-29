
package com.swak.core.extension;

public interface ExtensionEnableProvider<I, O> extends ExtensionConfigProvider<I, O> {
	/**
	 * 是否开启
	 */
	@Override
	boolean enable(I resource);
}
