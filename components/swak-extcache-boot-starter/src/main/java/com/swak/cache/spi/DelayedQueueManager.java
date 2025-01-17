package com.swak.cache.spi;

import com.swak.cache.queue.DelayedQueueHandler;
import com.swak.cache.queue.DelayedQueueListener;
import com.swak.common.spi.SpiPriority;
import com.swak.common.spi.SpiServiceFactory;
import org.redisson.api.RedissonClient;

/**
 * DelayedQueueManager.java
 *
 * @author colley.ma
 * @since 2.4.0
 */
public interface DelayedQueueManager extends SpiPriority {

    DelayedQueueHandler createQueue(String dequeName);

    DelayedQueueHandler getDelayedQueue(String dequeName);


    void addListeners(String dequeName, DelayedQueueListener... listeners);

    void onMultiSubscribe(long emptyWaitMs, String... dequeNames);

    void onSubscribe(long emptyWaitMs);

    void stop();

    RedissonClient getClient();

    static DelayedQueueManager getDelayedQueueManager() {
        return SpiServiceFactory.loadFirst(DelayedQueueManager.class);
    }
}
