package com.swak.test.core.monitor;

import com.swak.core.monitor.async.AsyncThreadPoolMonitor;
import com.swak.core.monitor.system.ThreadMonitor;
import com.swak.core.monitor.model.SunThread;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link ThreadMonitor} 通过平台 MBeanServer 采集线程快照。
 * <p>
 * 线程数量与内容随环境变化，因此只断言「结构完整、状态字段有值、包含当前线程」这类稳定条件，
 * 不依赖具体线程数。
 */
@DisplayName("JVM 线程监控")
class ThreadMonitorTest {

    @Test
    @DisplayName("geThreads：返回非空列表，每个线程都有 id / name / state")
    void geThreadsReturnsThreadSnapshots() throws Exception {
        List<SunThread> threads = ThreadMonitor.geThreads();

        assertThat(threads).isNotNull().isNotEmpty();
        assertThat(threads).allSatisfy(thread -> {
            assertThat(thread.getId()).isPositive();
            assertThat(thread.getName()).isNotBlank();
            assertThat(thread.getState()).isNotBlank();
            assertThat(thread.getWaitedCount()).isNotNegative();
            assertThat(thread.getBlockedCount()).isNotNegative();
        });
    }

    @Test
    @DisplayName("geThreads：当前测试线程一定在快照里，且能采集到栈顶执行点")
    void geThreadsContainsCurrentThreadAndExecutionPoint() throws Exception {
        List<SunThread> threads = ThreadMonitor.geThreads();

        Optional<SunThread> current = threads.stream()
                .filter(thread -> thread.getId() == Thread.currentThread().getId())
                .findFirst();

        assertThat(current).isPresent();
        assertThat(current.get().isDeadlocked()).isFalse();
        assertThat(current.get().getExecutionPoint())
                .as("当前线程至少有 geThreads 这一层栈帧")
                .isNotNull();
        assertThat(current.get().getExecutionPoint().getClassName()).isNotBlank();
        assertThat(current.get().getExecutionPoint().getMethodName()).isNotBlank();
    }

    @Test
    @DisplayName("geThreads：连续调用结果稳定，重复线程 id 不重复出现")
    void geThreadsIsRepeatable() throws Exception {
        List<SunThread> first = ThreadMonitor.geThreads();
        List<SunThread> second = ThreadMonitor.geThreads();

        assertThat(first).isNotEmpty();
        assertThat(second).isNotEmpty();
        assertThat(second.stream().map(SunThread::getId).distinct().count())
                .isEqualTo(second.size());
    }
}
