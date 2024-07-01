package com.swak.core.extension;

/**
 * 
 * 扩展执行器 ClassName: ExtensionHandler.java
 * 
 * @author colley.ma
 * @date 2021年3月19日 上午11:20:25
 */
public interface ExtensionHandler<O, I> extends ExtensionPoint {

	public O exchange(I request);

	public O invoke(I request);
}
