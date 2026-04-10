package com.swak.cache.queue;

import com.google.common.collect.Maps;
import com.swak.cache.spi.DelayedConfigurer;
import com.swak.cache.spi.DelayedQueueManager;
import com.swak.common.exception.ThrowableWrapper;
import com.swak.common.timer.CycleTask;
import com.swak.common.timer.WheelTimerHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.redisson.api.RedissonClient;

import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * RedissonDelayedQueueManager.java
 *
 * @author colley.ma
 * @since 2.4.0
 */
@Slf4j
public class RedissonDelayedQueueManager implements DelayedQueueManager {

    private volatile Map<String, DelayedQueueHandler> delayedQueueHandlers = Maps.newConcurrentMap();

    private volatile boolean threadToStop = false;
    private final AtomicBoolean started = new AtomicBoolean(false);

    private final Map<String, Thread> subscribeThreads = Maps.newConcurrentMap();

    private final RedissonClient redissonClient;

    private final Executor executor;

    private final Map<String, Long> delayedSpin = Maps.newConcurrentMap();

    public RedissonDelayedQueueManager(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
        this.executor = initializeExecutor(DelayedConfigurer.getDelayedConfigurer());
    }

    @Override
    public DelayedQueueHandler createQueue(String dequeName) {
        DelayedQueueHandler delayedQueueHandler = delayedQueueHandlers.get(dequeName);
        if (Objects.isNull(delayedQueueHandler)) {
            delayedQueueHandler = new SimpleDelayedQueueHandler(dequeName, redissonClient, executor);
            delayedQueueHandlers.putIfAbsent(dequeName, delayedQueueHandler);
        }
        return delayedQueueHandler;
    }

    @Override
    public void addListeners(String dequeName, DelayedQueueListener... listeners) {
        DelayedQueueHandler delayedQueueHandler = delayedQueueHandlers.computeIfAbsent(dequeName, v ->
                this.createQueue(dequeName));
        delayedQueueHandler.addListeners(listeners);
    }

    @Override
    public DelayedQueueHandler getDelayedQueue(String dequeName) {
        return delayedQueueHandlers.computeIfAbsent(dequeName, v -> this.createQueue(dequeName));
    }


    @Override
    public void onSubscribe(long emptyWaitMs) {
        if (started.compareAndSet(false, true)) {
            Thread subscribeThread = new Thread(() -> {
                while (!threadToStop) {
                    for (String dequeName : delayedQueueHandlers.keySet()) {
                        if (delayedSpin.containsKey(dequeName)) {
                            continue;
                        }
                        boolean isNotEmpty = onSubscribe(dequeName);
                        if (!isNotEmpty) {
                            delayedSpin.putIfAbsent(dequeName, System.currentTimeMillis());
                        }
                    }
                }
            });
            subscribeThread.setDaemon(true);
            subscribeThread.setName("delayedSingle");
            subscribeThread.start();
            SpinCycle spinCycle = new SpinCycle(emptyWaitMs, emptyWaitMs);
            WheelTimerHolder.lockWatchWheel().newTimeout(spinCycle, emptyWaitMs, TimeUnit.SECONDS);
            subscribeThreads.put("SINGLE_SUBSCRIBE", subscribeThread);
        }
    }

    @Override
    public void onMultiSubscribe(long emptyWaitMs, String... dequeNames) {
        if (ArrayUtils.isEmpty(dequeNames)) {
            return;
        }
        if (started.compareAndSet(false, true)) {
            for (String dequeName : dequeNames) {
                Thread subscribeThread = new Thread(() -> {
                    while (!threadToStop) {
                        boolean isNotEmpty = onWaitSubscribe(dequeName);
                        if (!isNotEmpty) {
                            try {
                                log.info("[Swak-DelayedQueue] >>>>>>>>>>> Wait {} ms onSubscribe", emptyWaitMs);
                                TimeUnit.MILLISECONDS.sleep(emptyWaitMs);
                            } catch (InterruptedException e) {
                                log.error("onWaitSubscribe error", e);
                            }
                        }
                    }
                });
                subscribeThread.setName(dequeName + "-Thread");
                subscribeThread.setDaemon(true);
                subscribeThread.start();
                subscribeThreads.put(dequeName, subscribeThread);
            }
        }
    }


    @Override
    public void stop() {
        this.threadToStop = true;
        this.started.set(false);
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
        }
        if (MapUtils.isEmpty(subscribeThreads)) {
            return;
        }
        subscribeThreads.forEach((k, subscribeThread) -> {
            log.info("[Swak-DelayedQueue] >>>>>>>>>>> {}  Thread onSubscribe stop", k);
            if (subscribeThread.getState() != Thread.State.TERMINATED) {
                // interrupt and wait
                subscribeThread.interrupt();
                try {
                    subscribeThread.join();
                } catch (InterruptedException e) {
                    log.error(e.getMessage(), e);
                }
            }
        });
        log.info("[Swak-DelayedQueue] >>>>>>>>>>> all onSubscribe stop");
    }

    @Override
    public RedissonClient getClient() {
        return this.redissonClient;
    }

    private boolean onSubscribe(String dequeName) {
        if (!delayedQueueHandlers.containsKey(dequeName)) {
            return false;
        }
        DelayedQueueHandler delayedQueue = getDelayedQueue(dequeName);
        if (Objects.isNull(delayedQueue)) {
            return false;
        }
        DelayEvent<?> delayEvent = delayedQueue.poll();
        if (Objects.nonNull(delayEvent)) {
            delayedQueue.onSubscribe(delayEvent);
            return true;
        }
        return false;
    }

    private boolean onWaitSubscribe(String dequeName) {
        if (!delayedQueueHandlers.containsKey(dequeName)) {
            return false;
        }
        DelayedQueueHandler delayedQueue = getDelayedQueue(dequeName);
        if (Objects.isNull(delayedQueue)) {
            return false;
        }
        DelayEvent<?> delayEvent = delayedQueue.poll();
        if (Objects.nonNull(delayEvent)) {
            delayedQueue.onSubscribe(delayEvent);
            return true;
        }
        return false;
    }

    protected Executor initializeExecutor(DelayedConfigurer delayedConfigurer) {
        Executor executor = Optional.ofNullable(delayedConfigurer).map(DelayedConfigurer::getDelayedExecutor).orElse(null);
        if (Objects.nonNull(executor)) {
            return executor;
        }
        BlockingQueue<Runnable> queue = new LinkedBlockingQueue<>(100);
        int corePoolSize = Math.max(1, Runtime.getRuntime().availableProcessors() - 1);
        int maxPoolSize = (int) Math.max(corePoolSize * 1.5, corePoolSize + 1);
        return new ThreadPoolExecutor(corePoolSize, maxPoolSize, 60L, TimeUnit.SECONDS, queue);
    }

    protected class SpinCycle extends CycleTask {
        private final long spinTime;

        public SpinCycle(long monitoringPeriod, long spinTime) {
            super.config(monitoringPeriod, TimeUnit.MILLISECONDS, true);
            this.spinTime = spinTime;
        }

        @Override
        protected void invoke() throws ThrowableWrapper {
            Long timeMillis = System.currentTimeMillis();
            Iterator<Map.Entry<String, Long>> iterator = delayedSpin.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, Long> next = iterator.next();
                if (timeMillis - next.getValue() >= spinTime) {
                    log.warn("SpinCycle {}-{}", next.getValue(), spinTime);
                    iterator.remove();
                }
            }
        }
    }

    @Override
    public int priority() {
        return 0;
    }
}
