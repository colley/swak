package com.swak.cache.queue;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RDelayedQueue;
import org.redisson.api.RedissonClient;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * SimpleDelayedQueueHandler.java
 *
 * @author colley.ma
 * @since 2.4.0
 */
public class SimpleDelayedQueueHandler implements DelayedQueueHandler {

    private final RDelayedQueue<DelayEvent> delayedQueue;
    private final RBlockingQueue<DelayEvent> blockingQueue;

    private Executor executor;

    private List<DelayedQueueListener> delayedListeners = Lists.newCopyOnWriteArrayList();

    public SimpleDelayedQueueHandler(String dequeName, RedissonClient redissonClient,Executor executor) {
        this.blockingQueue = redissonClient.getBlockingQueue(dequeName);
        this.delayedQueue = redissonClient.getDelayedQueue(blockingQueue);
        this.executor = executor;
    }

    @Override
    public boolean remove(DelayEvent<?> element) {
        return delayedQueue.remove(element);
    }

    @Override
    public boolean add(DelayEvent<?> element, long timeout, TimeUnit timeUnit) {
        delayedQueue.offer(element, timeout, timeUnit);
        return true;
    }

    @Override
    public DelayEvent<?> take() throws InterruptedException {
        return blockingQueue.take();
    }

    @Override
    public DelayEvent<?> poll() {
        return blockingQueue.poll();
    }

    @Override
    public DelayEvent<?> poll(long timeout, TimeUnit unit) throws InterruptedException {
        return blockingQueue.poll(timeout,unit);
    }

    @Override
    public void addListeners(DelayedQueueListener... listeners) {
        if (ArrayUtils.isNotEmpty(listeners)) {
            Map<Class<?>, DelayedQueueListener> listenerMap = Maps.newHashMap();
            for (DelayedQueueListener delayedListener : delayedListeners) {
                listenerMap.put(delayedListener.getClass(), delayedListener);
            }
            for (DelayedQueueListener listener : listeners) {
                listenerMap.put(listener.getClass(), listener);
            }
            delayedListeners.clear();
            delayedListeners.addAll(listenerMap.values().stream()
                    .sorted(Comparator.comparing(DelayedQueueListener::priority))
                    .collect(Collectors.toList()));
        }
    }

    @Override
    public void onSubscribe(DelayEvent<?> delayEvent) {
        if (Objects.isNull(delayEvent) || CollectionUtils.isEmpty(delayedListeners)) {
            return;
        }
        executor.execute(()-> delayedListeners.forEach(listener -> listener.onSubscribe(delayEvent, this)));
    }
}
