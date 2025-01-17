package com.swak.cache.queue;

import com.swak.common.listener.SwakEventListener;
import com.swak.common.spi.SpiPriority;

/**
 * DelayedQueue.java
 *
 * @author colley.ma
 * @since 2.4.0
 */
public interface DelayedQueueListener extends SpiPriority, SwakEventListener {

     void onSubscribe(DelayEvent<?> delayEvent,DelayedQueueHandler handler);
}
