
package com.swak.core.extension;

public interface ExtensionConfigProvider<I,O> extends ExtensionPoint {

	/**
	 * 	配置信息上报
	 * @param @param resource   
	 * @return void
	 */
	default public void report(O resource) {};
	
	/**获取配置**/
	O getConfig(I resource);
	
	default public boolean enable(I resource) {
		return false;
	}
}
