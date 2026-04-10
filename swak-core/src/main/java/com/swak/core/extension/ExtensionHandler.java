package com.swak.core.extension;

/**
 * 
 * 扩展执行器 ClassName: ExtensionHandler.java
 *
 * @author colley.ma
 * @since 2.4.0
 */
public interface ExtensionHandler<O, I> extends ExtensionPoint {

	 O exchange(I request);

	 O invoke(I request);
}
