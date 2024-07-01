package com.swak.core.monitor.async;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.swak.common.key.ObjectKey;
import com.swak.common.timer.WheelTimerHolder;
import com.swak.core.aware.InitializedAware;
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
    private final Map<ObjectKey,ThreadPoolTaskExecutor> taskExecutors = Maps.newConcurrentMap();


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
        log.warn("[swak-AsyncThreadPool] - AsyncThreadPool监控系统启动  ......");
        ThreadPoolMonitorTask task = new ThreadPoolMonitorTask(this, getMonitorPeriod());
        WheelTimerHolder.monitorWheel().newTimeout(task, monitoringPeriod, TimeUnit.SECONDS);
    }


    public void addTaskExecutor(ThreadPoolTaskExecutor taskExecutor) {
        ObjectKey key = ObjectKey.asKey(taskExecutor.getThreadNamePrefix(),
                taskExecutor.getCorePoolSize(),taskExecutor.getMaxPoolSize());
        taskExecutors.putIfAbsent(key,taskExecutor);
    }

    public void setMonitoringPeriod(long monitoringPeriod) {
        this.monitoringPeriod = monitoringPeriod;
    }
}
