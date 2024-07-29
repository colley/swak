package com.swak.core.eventbus;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * EventHandler.java
 * 
 * @author colley.ma
 * @since 2.4.0
 **/
public interface EventHandler extends InitializingBean,DisposableBean {

	public void postEvent(Object event);
	
	public void register();
	
}
