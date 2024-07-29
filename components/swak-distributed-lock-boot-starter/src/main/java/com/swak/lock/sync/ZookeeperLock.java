package com.swak.lock.sync;

import com.swak.core.spectator.metrics.Monitors;
import com.swak.core.sync.DistributedLock;
import com.swak.lock.config.LockProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.InterProcessLock;
import org.apache.curator.framework.recipes.locks.InterProcessSemaphoreMutex;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @author colley.ma
 * @since 3.0.0
 */
@Slf4j
public class ZookeeperLock implements DistributedLock {

    private final CuratorFramework curator;

    private final LockProperties properties;

    private final Map<String, InterProcessLock> interProcessLockMap = new ConcurrentHashMap<>();

    public ZookeeperLock(CuratorFramework curator, LockProperties properties) {
        this.curator = curator;
        this.properties = properties;
    }

    @Override
    public boolean acquireLock(String lockId) {
        try {
            interProcessLockMap.computeIfAbsent(lockId, v ->
                            new InterProcessSemaphoreMutex(curator, lockId))
                    .acquire();
        } catch (Exception e) {
           return handleAcquireLockFailure(lockId, e);
        }
        return true;
    }

    @Override
    public boolean acquireLock(String lockId, long timeToTry, TimeUnit unit) {
        try {
            InterProcessLock interProcessLock = interProcessLockMap.computeIfAbsent(lockId, v ->
                    new InterProcessSemaphoreMutex(curator, lockId));
            return interProcessLock.acquire(timeToTry, unit);
        } catch (Exception e) {
            return handleAcquireLockFailure(lockId, e);
        }
    }

    @Override
    public boolean acquireLock(String lockId, long timeToTry, long leaseTime, TimeUnit unit) {
        try {
            InterProcessLock interProcessLock = interProcessLockMap.computeIfAbsent(lockId, v ->
                    new InterProcessSemaphoreMutex(curator, lockId));
            return interProcessLock.acquire(timeToTry, unit);
        } catch (Exception e) {
            return handleAcquireLockFailure(lockId, e);
        }
    }

    @Override
    public void releaseLock(String lockId) {
        try {
            InterProcessLock interProcessLock = interProcessLockMap.get(lockId);
            if (Objects.nonNull(interProcessLock)) {
                interProcessLock.release();
            }
        } catch (Exception e) {
            log.error("[Swak-Lock] Failed to releaseLock for lockId: {}", lockId, e);
        } finally {
            interProcessLockMap.remove(lockId);
        }
    }

    @Override
    public void deleteLock(String lockId) {
        releaseLock(lockId);
    }

    private boolean handleAcquireLockFailure(String lockId, Exception e) {
        log.error("[Swak-Lock] Failed to acquireLock for lockId: {}", lockId, e);
        Monitors.recordAcquireLockFailure(e.getClass().getName());
        // A Valid failure to acquire lock when another thread has acquired it returns false.
        // However, when an exception is thrown while acquiring lock, due to connection or others
        // issues,
        // we can optionally continue without a "lock" to not block executions until Locking service
        // is available.
        return properties.isIgnoreLockingExceptions();
    }
}
