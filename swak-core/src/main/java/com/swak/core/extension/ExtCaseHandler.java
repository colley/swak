package com.swak.core.extension;

/**
 * 
 * 扩展执行器 ClassName: ExtensionHandler.java
 *
 * @author colley.ma
 * @since 2.4.0
 */
public interface ExtCaseHandler<O, I> extends ExtensionPoint {

    public O exchange(I request);

    public O invoke(I request);
}
