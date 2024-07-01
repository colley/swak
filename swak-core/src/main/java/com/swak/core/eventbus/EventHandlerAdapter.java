
package com.swak.core.eventbus;

import com.google.common.eventbus.AsyncEventBus;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

/**
 * EventHandlerAdapter.java
 *
 * @author ColleyMa
 * @version 20-3-24 下午1:46
 */
@Slf4j
public class EventHandlerAdapter extends AsyncEventBus implements EventHandler {

	public EventHandlerAdapter(Executor executor) {
		super("configEvent", executor);
	}

	private List<EventBusListener> eventListeners = new ArrayList<>();

	@Autowired(required = false)
	public void setEventListener(List<EventBusListener> eventListeners) {
		if (CollectionUtils.isNotEmpty(eventListeners)) {
			this.eventListeners.addAll(eventListeners);
		}
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		this.register();
	}

	@Override
	public void destroy() throws Exception {
		eventListeners.forEach(eventListener -> {
			log.info("event listener unregister,className:{}", eventListener.getClass().getName());
			this.unregister(eventListener);
		});
	}

	@Override
	public void postEvent(Object event) {
		this.post(event);
	}

	@Override
	public void register() {
		eventListeners.forEach(eventListener -> {
			log.info("event listener register,className:{}", eventListener.getClass().getName());
			this.register(eventListener);
		});
	}

}
