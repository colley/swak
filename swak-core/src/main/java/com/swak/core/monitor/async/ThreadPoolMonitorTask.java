package com.swak.core.monitor.async;

import com.swak.common.exception.ThrowableWrapper;
import com.swak.common.key.ObjectKey;
import com.swak.common.timer.CycleTask;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 线程监控
 *
 * @author colley.ma
 * @since 2.4.0
 */
@Slf4j
public class ThreadPoolMonitorTask extends CycleTask implements IThreadPoolMonitorService {


    private final AsyncThreadPoolMonitor threadPoolMonitor;


    public ThreadPoolMonitorTask(AsyncThreadPoolMonitor threadPoolMonitor, long monitoringPeriod) {
        super.config(monitoringPeriod, TimeUnit.SECONDS, true);
        this.threadPoolMonitor = threadPoolMonitor;
    }

    @Override
    public void monitor() {
        Map<ObjectKey, ThreadPoolTaskExecutor> taskExecutors = threadPoolMonitor.getTaskExecutors();
        if (MapUtils.isEmpty(taskExecutors)) {
            return;
        }
        for (ThreadPoolTaskExecutor taskExecutor : taskExecutors.values()) {
            monitor(taskExecutor);
        }
    }

    public void monitor(ThreadPoolTaskExecutor executor) {
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


    @Override
    protected void invoke() throws ThrowableWrapper {
        monitor();
    }
}
