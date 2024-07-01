package com.swak.lock.sync;

import com.swak.core.spectator.metrics.Monitors;
import com.swak.core.sync.DistributedLock;
import com.swak.lock.config.LockProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.util.concurrent.TimeUnit;

/**
 * @author colley.ma
 * @since 3.0.0
 */
@Slf4j
public class RedissonLock implements DistributedLock {
    private final LockProperties properties;
    private final RedissonClient redissonClient;
    private static String LOCK_NAMESPACE = "";

    public RedissonLock(RedissonClient redissonClient, LockProperties properties) {
        this.properties = properties;
        this.redissonClient = redissonClient;
        LOCK_NAMESPACE = properties.getNamespace();
    }

    @Override
    public boolean acquireLock(String lockId) {
        try {
            RLock lock = redissonClient.getLock(parseLockId(lockId));
            lock.lock();
        } catch (Exception e) {
            return handleAcquireLockFailure(lockId, e);
        }
        return true;
    }

    @Override
    public boolean acquireLock(String lockId, long timeToTry, TimeUnit unit) {
        RLock lock = redissonClient.getLock(parseLockId(lockId));
        try {
            return lock.tryLock(timeToTry, unit);
        } catch (Exception e) {
            return handleAcquireLockFailure(lockId, e);
        }
    }

    /**
     * @param lockId    resource to lock on
     * @param timeToTry blocks up to timeToTry duration in attempt to acquire the lock
     * @param leaseTime Lock lease expiration duration. Redisson default is -1, meaning it holds the
     *                  lock until explicitly unlocked.
     * @param unit      time unit
     */
    @Override
    public boolean acquireLock(String lockId, long timeToTry, long leaseTime, TimeUnit unit) {
        RLock lock = redissonClient.getLock(parseLockId(lockId));
        try {
            return lock.tryLock(timeToTry, leaseTime, unit);
        } catch (Exception e) {
            return handleAcquireLockFailure(lockId, e);
        }
    }

    @Override
    public void releaseLock(String lockId) {
        RLock lock = redissonClient.getLock(parseLockId(lockId));
        try {
            lock.unlock();
        } catch (IllegalMonitorStateException e) {
            // Releasing a lock twice using Redisson can cause this exception, which can be ignored.
        }
    }

    @Override
    public void deleteLock(String lockId) {
        // Noop for Redlock algorithm as releaseLock / unlock deletes it.
    }

    private String parseLockId(String lockId) {
        if (StringUtils.isEmpty(lockId)) {
            throw new IllegalArgumentException("lockId cannot be NULL or empty: lockId=" + lockId);
        }
        return LOCK_NAMESPACE + "." + lockId;
    }

    private boolean handleAcquireLockFailure(String lockId, Exception e) {
        log.error("Failed to acquireLock for lockId: {}", lockId, e);
        Monitors.recordAcquireLockFailure(e.getClass().getName());
        // A Valid failure to acquire lock when another thread has acquired it returns false.
        // However, when an exception is thrown while acquiring lock, due to connection or others
        // issues,
        // we can optionally continue without a "lock" to not block executions until Locking service
        // is available.
        return properties.isIgnoreLockingExceptions();
    }
}
