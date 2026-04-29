package com.swak.core.monitor.async;

import com.swak.common.exception.ThrowableWrapper;
import com.swak.common.i18n.I18nMessageFormat;
import com.swak.common.key.ObjectKey;
import com.swak.common.timer.CycleTask;
import com.swak.core.SwakConstants;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.swak.core.SwakConstants.LINE_SEPARATOR;

/**
 * 线程监控
 *
 * @author colley.ma
 * @since 2.4.0
 */
@Slf4j
public class ThreadPoolMonitorTask extends CycleTask implements IThreadPoolMonitorService {


    private final AsyncThreadPoolMonitor threadPoolMonitor;

    private final ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();



    public ThreadPoolMonitorTask(AsyncThreadPoolMonitor threadPoolMonitor, long monitoringPeriod) {
        super.config(monitoringPeriod, TimeUnit.SECONDS, true);
        this.threadPoolMonitor = threadPoolMonitor;
    }

    @Override
    public void monitor() {
        monitorV2();
    }

    public void monitorV1() {
        Map<ObjectKey, ThreadPoolTaskExecutor> taskExecutors = threadPoolMonitor.getTaskExecutors();
        if (MapUtils.isEmpty(taskExecutors)) {
            return;
        }
        for (ThreadPoolTaskExecutor taskExecutor : taskExecutors.values()) {
            monitorV1(taskExecutor);
        }
    }

    public void monitorV1(ThreadPoolTaskExecutor executor) {
        StringBuilder strBuff = new StringBuilder();
        ThreadPoolExecutor tp = executor.getThreadPoolExecutor();
        strBuff.append("线程池名称 : ").append(executor.getThreadNamePrefix());
        strBuff.append(" 当前线程数 : ").append(tp.getPoolSize());
        strBuff.append(" - 核心线程池大小 : ").append(tp.getCorePoolSize());
        strBuff.append(" - 最大线程池大小 : ").append(tp.getMaximumPoolSize());
        strBuff.append(" - 队列容量 : ").append(executor.getQueueCapacity());
        strBuff.append(" - 活动线程数量 : ").append(tp.getActiveCount());
        strBuff.append(" - 完成的线程数量 : ").append(tp.getCompletedTaskCount());
        strBuff.append(" - 任务总数 : ").append(tp.getTaskCount());
        strBuff.append(" - 队列任务大小 : ").append(tp.getQueue().size());
        strBuff.append(" - 是否所有任务都已完成 : ").append(tp.isTerminated());
        log.warn(strBuff.toString());
    }


    public void monitorV2() {
        Map<ObjectKey, ThreadPoolTaskExecutor> taskExecutors = threadPoolMonitor.getTaskExecutors();
        if (MapUtils.isEmpty(taskExecutors)) {
            return;
        }
        // 1. Fetch all thread stack info once to minimize overhead
        long[] allThreadIds = threadMXBean.getAllThreadIds();
        Map<Long, ThreadInfo> threadInfoMap = Arrays.stream(allThreadIds)
                .mapToObj(threadMXBean::getThreadInfo)
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(ThreadInfo::getThreadId, info -> info));

        for (ThreadPoolTaskExecutor taskExecutor : taskExecutors.values()) {
            monitorV2(taskExecutor, threadInfoMap);
        }
    }

    /**
     * Monitor a single thread pool
     */
    public void monitorV2(ThreadPoolTaskExecutor executor, Map<Long, ThreadInfo> threadInfoMap) {
        ThreadPoolExecutor tp = executor.getThreadPoolExecutor();
        String poolName = executor.getThreadNamePrefix();
        // --- 1. Core Configuration ---
        int corePoolSize = tp.getCorePoolSize();
        int maxPoolSize = tp.getMaximumPoolSize();
        long keepAliveTime = tp.getKeepAliveTime(TimeUnit.SECONDS);
        int queueCapacity = executor.getQueueCapacity();
        RejectedExecutionHandler rejectedHandler = tp.getRejectedExecutionHandler();

        // --- 2. Runtime Metrics ---
        int poolSize = tp.getPoolSize();
        int activeCount = tp.getActiveCount();
        int queueSize = tp.getQueue().size();
        long taskCount = tp.getTaskCount();

        // Calculate usage rates
        double activeRate = poolSize == 0 ? 0 : (activeCount * 1.0 / poolSize);
        double queueUsage = queueCapacity == 0 ? 0 : (queueSize * 1.0 / queueCapacity);
        StringBuilder  builder = new StringBuilder();
        builder.append(SwakConstants.LINE_SEPARATOR);
        builder.append("线程池名称 : ").append("[").append(poolName).append("]");
        builder.append(" - 核心线程池大小: ").append(corePoolSize);
        builder.append(" - 最大线程池大小: ").append(maxPoolSize);
        builder.append(" - 队列容量 : ").append(queueCapacity);
        builder.append(" - KeepAlive : ").append(keepAliveTime).append("s");
        builder.append(" - Policy : ").append(rejectedHandler.getClass().getSimpleName());
        builder.append(SwakConstants.LINE_SEPARATOR);
        builder.append(" 当前线程数 : ").append(poolSize);
        builder.append(" - 活动线程数量 : ").append(activeCount);
        builder.append(" - Load : ").append(String.format("%.1f", activeRate * 100)).append("%");
        builder.append(" - Queue : ").append(I18nMessageFormat.format("{}/{} (Usage: {}%)",queueSize, queueCapacity, String.format("%.1f", queueUsage * 100)));
        builder.append(SwakConstants.LINE_SEPARATOR);
        builder.append(" 任务总数 : ").append(taskCount);
        builder.append(" - 完成的线程数量 : ").append(tp.getCompletedTaskCount());
        builder.append(" - 是否所有任务都已完成 : ").append(tp.isTerminated());
        // --- 4. Print Active Thread Stacks ---
        if (activeCount > 0) {
            for (ThreadInfo info : threadInfoMap.values()) {
                String threadName = info.getThreadName();
                // Match thread name prefix
                if (threadName.startsWith(poolName)) {
                    topStackMonitor(poolName, info,builder);
                }
            }
        }
        // --- 5. Alert on High Queue Usage ---
        if (queueUsage > 0.8) {
            builder.append(LINE_SEPARATOR).append(I18nMessageFormat.format("  ALERT: ThreadPool [{}] queue is critically full, usage > 80%!", poolName));
        }

        log.warn(builder.toString());
    }

    public void topStackMonitor(String poolName, ThreadInfo info,StringBuilder  builder) {
        String threadName = info.getThreadName();
        builder.append(LINE_SEPARATOR).append(" >>> ActiveThreadDetails:      ").append(LINE_SEPARATOR);
        StackTraceElement[] stackTrace = info.getStackTrace();
        if (stackTrace.length > 0) {
            StackTraceElement topStack = stackTrace[0];
            builder.append(" - 线程池和线程名称 : ").append(String.format("[%s] - [%s]",poolName, threadName));
            builder.append(" - 线程状态 : ").append(info.getThreadState());
            builder.append(" - Executing : ").append(LINE_SEPARATOR)
                    .append(String.format("%s.%s(%s:%s)", topStack.getClassName(),
                    topStack.getMethodName(),
                    topStack.getFileName(),
                    topStack.getLineNumber()));
        }
    }

    @Override
    protected void invoke() throws ThrowableWrapper {
        monitor();
    }
}
