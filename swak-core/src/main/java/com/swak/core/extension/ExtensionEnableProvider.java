
package com.swak.core.extension;

public interface ExtensionEnableProvider<I, O> extends ExtensionConfigProvider<I, O> {
	/**
	 * 是否开启
	 * 
	 * @param @param  resource resource.enable
	 * @param @return
	 * @return boolean
	 */
	@Override
	boolean enable(I resource);
}
