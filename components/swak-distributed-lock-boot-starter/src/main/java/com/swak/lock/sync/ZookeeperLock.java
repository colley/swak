package com.swak.lock.sync;

import com.swak.core.spectator.metrics.Monitors;
import com.swak.core.sync.DistributedLock;
import com.swak.lock.config.LockProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.InterProcessLock;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Zookeeper锁实现
 * @author colley.ma
 * @since 2.3.3
 */
@Slf4j
public class ZookeeperLock implements DistributedLock {

    private final CuratorFramework curator;
    private final LockProperties properties;

    // 本地重入锁，用于保护线程安全的锁获取
    private final Map<String, ReentrantLock> localLocks = new ConcurrentHashMap<>();

    // 线程隔离的锁实例映射：Thread -> (lockId -> InterProcessLock)
    private final ThreadLocal<Map<String, InterProcessLock>> threadLockMap =
            ThreadLocal.withInitial(ConcurrentHashMap::new);

    public ZookeeperLock(CuratorFramework curator, LockProperties properties) {
        this.curator = curator;
        this.properties = properties;
    }

    @Override
    public boolean acquireLock(String lockId) {
        return acquireLockInternal(lockId, -1, -1, null);
    }

    @Override
    public boolean acquireLock(String lockId, long timeToTry, TimeUnit unit) {
        return acquireLockInternal(lockId, timeToTry, -1, unit);
    }

    @Override
    public boolean acquireLock(String lockId, long timeToTry, long leaseTime, TimeUnit unit) {
        // Zookeeper锁通常不需要单独的leaseTime参数
        return acquireLockInternal(lockId, timeToTry, -1, unit);
    }

    private boolean acquireLockInternal(String lockId, long timeToTry, long leaseTime, TimeUnit unit) {
        // 1. 获取本地重入锁，确保同一JVM内线程安全
        ReentrantLock localLock = localLocks.computeIfAbsent(lockId, k -> new ReentrantLock());
        localLock.lock();

        try {
            // 2. 获取当前线程的锁映射
            Map<String, InterProcessLock> currentThreadLocks = threadLockMap.get();

            // 3. 检查是否已持有锁（可重入）
            InterProcessLock existingLock = currentThreadLocks.get(lockId);
            if (existingLock != null && existingLock.isAcquiredInThisProcess()) {
                log.debug("[Swak-Lock] Thread {} already holds lock {}",
                        Thread.currentThread().getName(), lockId);
                return true;
            }

            // 4. 创建新的分布式锁实例（每个线程独立实例）
            InterProcessLock lock = new InterProcessMutex(curator, "/locks/" + lockId);

            boolean acquired;
            if (timeToTry > 0 && unit != null) {
                acquired = lock.acquire(timeToTry, unit);
            } else {
                lock.acquire();
                acquired = true;
            }

            if (acquired) {
                currentThreadLocks.put(lockId, lock);
                log.info("[Swak-Lock] Thread {} successfully acquired lock {}",
                        Thread.currentThread().getName(), lockId);
            } else {
                log.warn("[Swak-Lock] Thread {} failed to acquire lock {} within timeout",
                        Thread.currentThread().getName(), lockId);
            }

            return acquired;

        } catch (Exception e) {
            return handleAcquireLockFailure(lockId, e);
        } finally {
            localLock.unlock();
        }
    }

    @Override
    public void releaseLock(String lockId) {
        // 1. 获取本地重入锁
        ReentrantLock localLock = localLocks.get(lockId);
        if (localLock == null) {
            log.warn("[Swak-Lock] No local lock found for lockId: {}", lockId);
            return;
        }

        localLock.lock();
        try {
            // 2. 获取当前线程的锁映射
            Map<String, InterProcessLock> currentThreadLocks = threadLockMap.get();
            InterProcessLock lock = currentThreadLocks.get(lockId);

            if (lock != null) {
                try {
                    if (lock.isAcquiredInThisProcess()) {
                        lock.release();
                        log.info("[Swak-Lock] Thread {} released lock {}",
                                Thread.currentThread().getName(), lockId);
                    } else {
                        log.warn("[Swak-Lock] Thread {} does not hold lock {}",
                                Thread.currentThread().getName(), lockId);
                    }
                } catch (Exception e) {
                    log.error("[Swak-Lock] Failed to release lock for lockId: {}", lockId, e);
                } finally {
                    // 从线程本地映射中移除
                    currentThreadLocks.remove(lockId);

                    // 如果当前线程没有持有任何锁，清理ThreadLocal
                    if (currentThreadLocks.isEmpty()) {
                        threadLockMap.remove();
                    }
                }
            } else {
                log.warn("[Swak-Lock] Thread {} does not have lock instance for lockId: {}",
                        Thread.currentThread().getName(), lockId);
            }
        } finally {
            localLock.unlock();
        }
    }

    @Override
    public void deleteLock(String lockId) {
        // 对于Zookeeper锁，释放就是删除
        releaseLock(lockId);
    }

    /**
     * 强制清理某个锁的所有实例（危险操作，慎用）
     */
    public void forceCleanup(String lockId) {
        log.warn("[Swak-Lock] Force cleaning up lock: {}", lockId);

        // 清理本地重入锁
        ReentrantLock localLock = localLocks.remove(lockId);
        if (localLock != null) {
            try {
                // 清理所有线程中该锁的实例
                for (Map<String, InterProcessLock> threadLocks : getAllThreadLocks()) {
                    InterProcessLock lock = threadLocks.remove(lockId);
                    if (lock != null) {
                        try {
                            if (lock.isAcquiredInThisProcess()) {
                                lock.release();
                            }
                        } catch (Exception e) {
                            log.error("[Swak-Lock] Error during force cleanup for lock: {}", lockId, e);
                        }
                    }
                }
            } finally {
                // 清理ThreadLocal
                threadLockMap.remove();
            }
        }
    }

    /**
     * 获取当前线程持有的所有锁（用于调试）
     */
    public Map<String, InterProcessLock> getCurrentThreadLocks() {
        return new HashMap<>(threadLockMap.get());
    }

    /**
     * 获取所有线程的锁信息（需要谨慎使用）
     */
    private List<Map<String, InterProcessLock>> getAllThreadLocks() {
        // 注意：这个方法只能获取到当前JVM中所有ThreadLocal的引用
        // 实际生产环境中，可能需要更复杂的管理
        return Collections.singletonList(threadLockMap.get());
    }

    private boolean handleAcquireLockFailure(String lockId, Exception e) {
        log.error("[Swak-Lock] Failed to acquireLock for lockId: {}", lockId, e);
        Monitors.recordAcquireLockFailure(e.getClass().getName());

        // 根据配置决定是否忽略异常继续执行
        return properties.isIgnoreLockingExceptions();
    }
}