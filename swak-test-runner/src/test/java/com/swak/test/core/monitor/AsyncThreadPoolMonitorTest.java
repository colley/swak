package com.swak.test.core.monitor;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.swak.core.monitor.async.AsyncThreadPoolMonitor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link AsyncThreadPoolMonitor} 的线程池登记与启动。
 * <p>
 * startup 会把周期任务投递到全局时间轮（守护线程），因此这里把监控周期设得足够大，
 * 只验证登记/日志/周期字段，不让任务真正执行；线程池在用例结束后 shutdown。
 */
@DisplayName("异步线程池监控")
class AsyncThreadPoolMonitorTest {

    private final List<ThreadPoolTaskExecutor> executors = new ArrayList<>();

    private ListAppender<ILoggingEvent> appender;

    @AfterEach
    void tearDown() {
        if (appender != null) {
            ((Logger) LoggerFactory.getLogger(AsyncThreadPoolMonitor.class)).detachAppender(appender);
            appender.stop();
            appender = null;
        }
        executors.forEach(ThreadPoolTaskExecutor::shutdown);
        executors.clear();
    }

    @Test
    @DisplayName("监控周期：默认 0，可显式设置")
    void monitoringPeriod() {
        AsyncThreadPoolMonitor monitor = new AsyncThreadPoolMonitor();

        assertThat(monitor.getMonitorPeriod()).isZero();
        monitor.setMonitoringPeriod(3600L);
        assertThat(monitor.getMonitorPeriod()).isEqualTo(3600L);
    }

    @Test
    @DisplayName("addTaskExecutor：按 前缀+核心数+最大数 去重，不同配置各自登记")
    void addTaskExecutorDeduplicates() {
        AsyncThreadPoolMonitor monitor = new AsyncThreadPoolMonitor();
        ThreadPoolTaskExecutor first = newExecutor("swak-", 1, 2, 10);
        ThreadPoolTaskExecutor sameKey = newExecutor("swak-", 1, 2, 10);
        ThreadPoolTaskExecutor otherCore = newExecutor("swak-", 2, 2, 10);

        monitor.addTaskExecutor(first);
        monitor.addTaskExecutor(sameKey);
        assertThat(monitor.getTaskExecutors()).hasSize(1);

        monitor.addTaskExecutor(otherCore);
        assertThat(monitor.getTaskExecutors()).hasSize(2);
    }

    @Test
    @DisplayName("setConfigurers：null / 空集合被忽略，非空集合全部登记")
    void setConfigurers() {
        AsyncThreadPoolMonitor monitor = new AsyncThreadPoolMonitor();

        monitor.setConfigurers(null);
        assertThat(monitor.getTaskExecutors()).isEmpty();

        monitor.setConfigurers(Collections.emptyList());
        assertThat(monitor.getTaskExecutors()).isEmpty();

        monitor.setConfigurers(Arrays.asList(
                newExecutor("one-", 1, 1, 1),
                newExecutor("two-", 2, 2, 2)));
        assertThat(monitor.getTaskExecutors()).hasSize(2);
    }

    @Test
    @DisplayName("startup：打印已登记线程池的配置并投递周期任务")
    void startupLogsRegisteredPools() {
        AsyncThreadPoolMonitor monitor = new AsyncThreadPoolMonitor();
        monitor.setMonitoringPeriod(3600L);
        ThreadPoolTaskExecutor executor = newExecutor("startup-", 2, 4, 8);
        monitor.addTaskExecutor(executor);

        appender = new ListAppender<>();
        appender.start();
        ((Logger) LoggerFactory.getLogger(AsyncThreadPoolMonitor.class)).addAppender(appender);

        monitor.startup();

        assertThat(appender.list).isNotEmpty();
        String message = appender.list.get(appender.list.size() - 1).getFormattedMessage();
        assertThat(message)
                .contains("AsyncThreadPool监控系统启动")
                .contains("MonitorPeriod:3600s")
                .contains("线程池名称 : [startup-]")
                .contains("核心线程池大小 : 2")
                .contains("最大线程池大小 : 4")
                .contains("队列容量 : 8")
                .contains("KeepAlive : 60s")
                .contains("RejectedHandler : AbortPolicy");
        assertThat(monitor.getTaskExecutors()).hasSize(1);
    }

    @Test
    @DisplayName("startup：没有登记线程池时仍能正常启动")
    void startupWithoutExecutors() {
        AsyncThreadPoolMonitor monitor = new AsyncThreadPoolMonitor();
        monitor.setMonitoringPeriod(3600L);

        monitor.startup();

        assertThat(monitor.getTaskExecutors()).isEmpty();
    }

    private ThreadPoolTaskExecutor newExecutor(String prefix, int core, int max, int queueCapacity) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setThreadNamePrefix(prefix);
        executor.setCorePoolSize(core);
        executor.setMaxPoolSize(max);
        executor.setQueueCapacity(queueCapacity);
        executor.initialize();
        executors.add(executor);
        return executor;
    }
}
