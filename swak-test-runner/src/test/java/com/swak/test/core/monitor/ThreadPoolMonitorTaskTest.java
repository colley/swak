package com.swak.test.core.monitor;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.swak.core.monitor.async.AsyncThreadPoolMonitor;
import com.swak.core.monitor.async.ThreadPoolMonitorTask;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link ThreadPoolMonitorTask} 的线程池指标采集与告警输出。
 * <p>
 * 不需要 {@code Thread.sleep}：用 {@link CountDownLatch} 把任务卡在「运行中」，
 * 再直接调用 {@code monitor()} 验证输出。所有线程池在用例结束后 shutdown。
 */
@DisplayName("线程池监控任务")
class ThreadPoolMonitorTaskTest {

    private final List<ThreadPoolTaskExecutor> executors = new ArrayList<>();

    private ListAppender<ILoggingEvent> appender;

    @AfterEach
    void tearDown() {
        if (appender != null) {
            loggerForTest().detachAppender(appender);
            appender.stop();
            appender = null;
        }
        executors.forEach(ThreadPoolTaskExecutor::shutdown);
        executors.clear();
    }

    @Test
    @DisplayName("没有注册任何线程池时 monitor 直接返回，不输出日志")
    void monitorWithoutExecutors() {
        attachAppender();
        ThreadPoolMonitorTask task =
                new ThreadPoolMonitorTask(new AsyncThreadPoolMonitor(), 60L);

        task.monitor();

        assertThat(appender.list).isEmpty();
    }

    @Test
    @DisplayName("monitorV2：空闲线程池输出配置、队列与 Load 指标")
    void monitorIdlePool() {
        attachAppender();
        ThreadPoolTaskExecutor executor = newExecutor(1, 1, 1, "idle-");
        ThreadPoolMonitorTask task =
                new ThreadPoolMonitorTask(new AsyncThreadPoolMonitor(), 60L);

        task.monitorV2(executor, Collections.emptyMap());

        String message = lastMessage();
        assertThat(message)
                .contains("线程池名称 : [idle-]")
                .contains("核心线程池大小: 1")
                .contains("最大线程池大小: 1")
                .contains("队列容量 : 1")
                .contains("Policy : AbortPolicy")
                .contains("Queue : 0/1 (Usage: 0.0%)")
                .contains("Load : 0.0%")
                // 没有活动线程时不打印线程栈
                .doesNotContain("ActiveThreadDetails");
    }

    @Test
    @DisplayName("monitor：活动线程打印栈顶，队列占用超过 80% 输出 ALERT")
    void monitorActivePoolAndAlert() throws Exception {
        attachAppender();
        ThreadPoolTaskExecutor executor = newExecutor(1, 1, 1, "busy-");
        AsyncThreadPoolMonitor monitor = new AsyncThreadPoolMonitor();
        monitor.addTaskExecutor(executor);

        CountDownLatch started = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        executor.execute(() -> {
            started.countDown();
            await(release);
        });
        assertThat(started.await(5, TimeUnit.SECONDS)).isTrue();
        // 核心线程被占满、队列容量 1，第二个任务只能入队 -> usage = 100%
        executor.execute(() -> await(release));

        try {
            new ThreadPoolMonitorTask(monitor, 60L).monitor();
        } finally {
            release.countDown();
        }

        String message = lastMessage();
        assertThat(message)
                .contains("线程池名称 : [busy-]")
                .contains("当前线程数 : 1")
                .contains("活动线程数量 : 1")
                .contains("Queue : 1/1 (Usage: 100.0%)")
                .contains("ALERT: ThreadPool [busy-] queue is critically full")
                // monitor() 抓取 ThreadInfo 时已指定栈深度，活动线程的执行点能正常输出
                .contains(">>> ActiveThreadDetails")
                .contains("[busy-] - [busy-1]")
                .contains("Executing :");
    }

    @Test
    @DisplayName("topStackMonitor：直接传入线程栈也能拼出执行点")
    void topStackMonitor() {
        ThreadPoolMonitorTask task =
                new ThreadPoolMonitorTask(new AsyncThreadPoolMonitor(), 60L);
        ThreadInfo info = ManagementFactory.getThreadMXBean()
                .getThreadInfo(Thread.currentThread().getId(), 1);

        StringBuilder builder = new StringBuilder();
        task.topStackMonitor("swak-", info, builder);

        assertThat(builder.toString())
                .contains(">>> ActiveThreadDetails")
                .contains("[swak-] - [" + Thread.currentThread().getName() + "]")
                .contains("Executing :");
    }

    @Test
    @DisplayName("CycleTask 生命周期：初始可运行，cancel/start 切换状态；invoke 委托给 monitor")
    void cycleTaskLifecycle() throws Exception {
        ThreadPoolMonitorTask task =
                new ThreadPoolMonitorTask(new AsyncThreadPoolMonitor(), 60L);

        assertThat(task.isCancelled()).isFalse();
        task.cancel();
        assertThat(task.isCancelled()).isTrue();
        task.start();
        assertThat(task.isCancelled()).isFalse();

        Method invoke = ThreadPoolMonitorTask.class.getDeclaredMethod("invoke");
        invoke.setAccessible(true);
        invoke.invoke(task);
        assertThat(task.isCancelled()).isFalse();
    }

    private ThreadPoolTaskExecutor newExecutor(int core, int max, int queueCapacity, String prefix) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(core);
        executor.setMaxPoolSize(max);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix(prefix);
        executor.initialize();
        executors.add(executor);
        return executor;
    }

    private static void await(CountDownLatch latch) {
        try {
            latch.await(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void attachAppender() {
        appender = new ListAppender<>();
        appender.start();
        loggerForTest().addAppender(appender);
    }

    private String lastMessage() {
        assertThat(appender.list).isNotEmpty();
        return appender.list.get(appender.list.size() - 1).getFormattedMessage();
    }

    private static Logger loggerForTest() {
        return (Logger) LoggerFactory.getLogger(ThreadPoolMonitorTask.class);
    }
}
