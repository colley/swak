package com.swak.core.eventbus;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

/** 
 * @className EventHandler.java
 * @author yuanchao.ma
 * @date 2022/06/07
 */
public interface EventHandler extends InitializingBean,DisposableBean {

	public void postEvent(Object event);
	
	public void register();
	
}
