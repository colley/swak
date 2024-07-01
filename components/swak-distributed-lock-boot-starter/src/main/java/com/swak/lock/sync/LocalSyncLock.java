
package com.swak.lock.sync;

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;

import com.swak.core.sync.DistributedLock;
import lombok.extern.slf4j.Slf4j;


import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

/**
 * LocalLock
 * @author colley.ma
 * @since 3.0.0
 **/
@Slf4j
public class LocalSyncLock implements DistributedLock {

    private static final CacheLoader<String, ReentrantLock> LOADER = key -> new ReentrantLock(true);
    private static final ConcurrentHashMap<String, ScheduledFuture<?>> SCHEDULED_FUTURES =
            new ConcurrentHashMap<>();
    private static final LoadingCache<String, ReentrantLock> LOCKIDTOSEMAPHOREMAP =
            Caffeine.newBuilder().build(LOADER);
    private static final ThreadGroup THREAD_GROUP = new ThreadGroup("LocalOnlyLock-scheduler");
    private static final ThreadFactory THREAD_FACTORY =
            runnable -> new Thread(THREAD_GROUP, runnable);
    private static final ScheduledExecutorService SCHEDULER =
            Executors.newScheduledThreadPool(1, THREAD_FACTORY);

    @Override
    public boolean acquireLock(String lockId) {
        log.trace("Locking {}", lockId);
        Objects.requireNonNull(LOCKIDTOSEMAPHOREMAP.get(lockId)).lock();
        return true;
    }

    @Override
    public boolean acquireLock(String lockId, long timeToTry, TimeUnit unit) {
        try {
            log.trace("Locking {} with timeout {} {}", lockId, timeToTry, unit);
            return Objects.requireNonNull(LOCKIDTOSEMAPHOREMAP.get(lockId)).tryLock(timeToTry, unit);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean acquireLock(String lockId, long timeToTry, long leaseTime, TimeUnit unit) {
        log.trace(
                "Locking {} with timeout {} {} for {} {}",
                lockId,
                timeToTry,
                unit,
                leaseTime,
                unit);
        if (acquireLock(lockId, timeToTry, unit)) {
            log.trace("Releasing {} automatically after {} {}", lockId, leaseTime, unit);
            SCHEDULED_FUTURES.put(
                    lockId, SCHEDULER.schedule(() -> deleteLock(lockId), leaseTime, unit));
            return true;
        }
        return false;
    }

    private void removeLeaseExpirationJob(String lockId) {
        ScheduledFuture<?> scheduledFuture = SCHEDULED_FUTURES.get(lockId);
        if (scheduledFuture != null && scheduledFuture.cancel(false)) {
            SCHEDULED_FUTURES.remove(lockId);
            log.trace("lockId {} removed from lease expiration job", lockId);
        }
    }

    @Override
    public void releaseLock(String lockId) {
        // Synchronized to prevent race condition between semaphore check and actual release
        synchronized (LOCKIDTOSEMAPHOREMAP) {
            if (LOCKIDTOSEMAPHOREMAP.getIfPresent(lockId) == null) {
                return;
            }
            log.trace("Releasing {}", lockId);
            Objects.requireNonNull(LOCKIDTOSEMAPHOREMAP.get(lockId)).unlock();
            removeLeaseExpirationJob(lockId);
        }
    }

    @Override
    public void deleteLock(String lockId) {
        log.trace("Deleting {}", lockId);
        LOCKIDTOSEMAPHOREMAP.invalidate(lockId);
    }

    LoadingCache<String, ReentrantLock> cache() {
        return LOCKIDTOSEMAPHOREMAP;
    }

    ConcurrentHashMap<String, ScheduledFuture<?>> scheduledFutures() {
        return SCHEDULED_FUTURES;
    }
}
