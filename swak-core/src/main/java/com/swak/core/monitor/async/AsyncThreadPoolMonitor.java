package com.swak.core.monitor.async;

import com.google.common.collect.Maps;
import com.swak.common.key.ObjectKey;
import com.swak.common.timer.WheelTimerHolder;
import com.swak.core.SwakConstants;
import com.swak.core.aware.MonitorAware;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
public class AsyncThreadPoolMonitor implements MonitorAware {
    @Getter
    private final Map<ObjectKey, ThreadPoolTaskExecutor> taskExecutors = Maps.newConcurrentMap();


    @Autowired(required = false)
    public void setConfigurers(List<ThreadPoolTaskExecutor> configurers) {
        if (CollectionUtils.isNotEmpty(configurers)) {
            for (ThreadPoolTaskExecutor taskExecutor : configurers) {
                addTaskExecutor(taskExecutor);
            }
        }
    }

    private long monitoringPeriod;

    @Override
    public long getMonitorPeriod() {
        return this.monitoringPeriod;
    }

    @Override
    public void startup() {
        StringBuilder builder = new StringBuilder("[swak-AsyncThreadPool] - AsyncThreadPool监控系统启动  ......");
        builder.append(SwakConstants.LINE_SEPARATOR);
        taskExecutors.forEach((k, executor) -> {
            builder.append("线程池名称 : ").append(executor.getThreadNamePrefix());
            builder.append(" - 核心线程池大小 : ").append(executor.getCorePoolSize());
            builder.append(" - 最大线程池大小 : ").append(executor.getMaxPoolSize());
            builder.append(" - 队列容量 : ").append(executor.getQueueCapacity());
            builder.append(SwakConstants.LINE_SEPARATOR);
        });
        log.warn(builder.toString());
        ThreadPoolMonitorTask task = new ThreadPoolMonitorTask(this, getMonitorPeriod());
        WheelTimerHolder.monitorWheel().newTimeout(task, monitoringPeriod, TimeUnit.SECONDS);
    }


    public void addTaskExecutor(ThreadPoolTaskExecutor taskExecutor) {
        ObjectKey key = ObjectKey.asKey(taskExecutor.getThreadNamePrefix(),
                taskExecutor.getCorePoolSize(), taskExecutor.getMaxPoolSize());
        taskExecutors.putIfAbsent(key, taskExecutor);
    }

    public void setMonitoringPeriod(long monitoringPeriod) {
        this.monitoringPeriod = monitoringPeriod;
    }
}
