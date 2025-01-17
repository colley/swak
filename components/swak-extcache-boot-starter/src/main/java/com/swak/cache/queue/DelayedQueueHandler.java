package com.swak.cache.queue;

import com.swak.common.spi.SpiPriority;

import java.util.concurrent.TimeUnit;

/**
 * DelayedQueueHandler.java
 * 
 * @author colley.ma
 * @since 2.4.0
 **/
public interface DelayedQueueHandler extends SpiPriority {

     boolean remove(DelayEvent<?> element);

     boolean add(DelayEvent<?> element,long timeout, TimeUnit timeUnit);

    DelayEvent<?> take() throws InterruptedException;

    DelayEvent<?> poll();

    DelayEvent<?> poll(long timeout, TimeUnit unit) throws InterruptedException;

    void  addListeners(DelayedQueueListener... listeners);

    void onSubscribe(DelayEvent<?> delayEvent);
}
