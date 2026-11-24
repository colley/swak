package com.swak.test.common.timer;

import com.swak.common.timer.HashedWheelTimer;
import com.swak.common.timer.Timeout;
import com.swak.common.timer.TimerTask;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@link HashedWheelTimer} 的参数校验、取消与执行回归。
 * <p>
 * 这里不靠 {@code Thread.sleep} 等真实延时：到点执行用 {@link CountDownLatch} 同步等待，
 * 其余用例都用 1 小时的超时来保证「不会被触发」，因此断言结果是确定的。
 */
@DisplayName("HashedWheelTimer 时间轮")
class HashedWheelTimerTest {

    private static final TimerTask NOOP = timeout -> {
    };

    @Test
    @DisplayName("构造器：null 工厂/单位、非正 tick、非正轮盘大小、超过 2^30")
    void constructorValidation() {
        ThreadFactory factory = Executors.defaultThreadFactory();

        assertThatThrownBy(() -> new HashedWheelTimer((ThreadFactory) null))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new HashedWheelTimer(factory, 1, null))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new HashedWheelTimer(0, TimeUnit.MILLISECONDS))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new HashedWheelTimer(1, TimeUnit.MILLISECONDS, 0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new HashedWheelTimer(1, TimeUnit.MILLISECONDS, 1_073_741_825))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("未启动时 stop 返回空集合，之后 start 抛 IllegalStateException")
    void stopBeforeStart() {
        HashedWheelTimer timer = new HashedWheelTimer(10, TimeUnit.MILLISECONDS);

        assertThat(timer.isStop()).isFalse();
        assertThat(timer.stop()).isEmpty();
        assertThat(timer.isStop()).isTrue();
        assertThatThrownBy(timer::start).isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("newTimeout：task / unit 为 null 时抛 NPE")
    void newTimeoutValidation() {
        HashedWheelTimer timer = new HashedWheelTimer(10, TimeUnit.MILLISECONDS);
        try {
            assertThatThrownBy(() -> timer.newTimeout(null, 1, TimeUnit.SECONDS))
                    .isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> timer.newTimeout(NOOP, 1, null))
                    .isInstanceOf(NullPointerException.class);
        } finally {
            timer.stop();
        }
    }

    @Test
    @DisplayName("Timeout：句柄信息、pending 计数与幂等取消")
    void timeoutHandleAndCancel() {
        HashedWheelTimer timer = new HashedWheelTimer(10, TimeUnit.MILLISECONDS);
        try {
            Timeout timeout = timer.newTimeout(NOOP, 1, TimeUnit.HOURS);

            assertThat(timeout.timer()).isSameAs(timer);
            assertThat(timeout.task()).isSameAs(NOOP);
            assertThat(timeout.isCancelled()).isFalse();
            assertThat(timeout.isExpired()).isFalse();
            assertThat(timeout.toString()).contains("deadline").contains("task:");
            assertThat(timer.pendingTimeouts()).isEqualTo(1);

            assertThat(timeout.cancel()).isTrue();
            assertThat(timeout.isCancelled()).isTrue();
            assertThat(timeout.cancel()).as("已取消的任务不能重复取消").isFalse();
        } finally {
            timer.stop();
        }
    }

    @Test
    @DisplayName("maxPendingTimeouts：超过上限抛 RejectedExecutionException")
    void maxPendingTimeouts() {
        HashedWheelTimer timer = new HashedWheelTimer(
                Executors.defaultThreadFactory(), 10, TimeUnit.MILLISECONDS, 64, 1);
        try {
            timer.newTimeout(NOOP, 1, TimeUnit.HOURS);

            assertThatThrownBy(() -> timer.newTimeout(NOOP, 1, TimeUnit.HOURS))
                    .isInstanceOf(RejectedExecutionException.class);
            assertThat(timer.pendingTimeouts()).isEqualTo(1);
        } finally {
            timer.stop();
        }
    }

    @Test
    @DisplayName("任务到点执行：句柄被置为过期并回传给任务")
    void taskExecutes() throws Exception {
        HashedWheelTimer timer = new HashedWheelTimer(10, TimeUnit.MILLISECONDS);
        try {
            CountDownLatch latch = new CountDownLatch(1);
            AtomicReference<Timeout> fired = new AtomicReference<>();

            Timeout timeout = timer.newTimeout(t -> {
                fired.set(t);
                latch.countDown();
            }, 0, TimeUnit.MILLISECONDS);

            assertThat(latch.await(5, TimeUnit.SECONDS)).isTrue();
            assertThat(fired.get()).isSameAs(timeout);
            assertThat(timeout.isExpired()).isTrue();
        } finally {
            timer.stop();
        }
    }

    @Test
    @DisplayName("stop：返回尚未执行的任务")
    void stopReturnsUnprocessed() {
        HashedWheelTimer timer = new HashedWheelTimer(10, TimeUnit.MILLISECONDS);
        Timeout timeout = timer.newTimeout(NOOP, 1, TimeUnit.HOURS);

        Set<Timeout> unprocessed = timer.stop();

        assertThat(unprocessed).containsExactly(timeout);
    }

    @Test
    @DisplayName("在 TimerTask 内调用 stop 抛 IllegalStateException")
    void stopFromWorkerThreadRejected() throws Exception {
        HashedWheelTimer timer = new HashedWheelTimer(10, TimeUnit.MILLISECONDS);
        try {
            AtomicReference<Throwable> error = new AtomicReference<>();
            CountDownLatch latch = new CountDownLatch(1);

            timer.newTimeout(timeout -> {
                try {
                    timer.stop();
                } catch (IllegalStateException e) {
                    error.set(e);
                } finally {
                    latch.countDown();
                }
            }, 0, TimeUnit.MILLISECONDS);

            assertThat(latch.await(5, TimeUnit.SECONDS)).isTrue();
            assertThat(error.get()).isInstanceOf(IllegalStateException.class);
        } finally {
            timer.stop();
        }
    }

    @Test
    @DisplayName("stop 之后 newTimeout 抛 IllegalStateException")
    void newTimeoutAfterStop() {
        HashedWheelTimer timer = new HashedWheelTimer(10, TimeUnit.MILLISECONDS);
        timer.stop();

        assertThatThrownBy(() -> timer.newTimeout(NOOP, 1, TimeUnit.SECONDS))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("默认构造器与 512 槽的默认轮盘")
    void defaultConstructor() {
        HashedWheelTimer timer = new HashedWheelTimer();
        try {
            assertThat(timer.isStop()).isFalse();
            assertThat(timer.newTimeout(NOOP, 1, TimeUnit.HOURS).isCancelled()).isFalse();
        } finally {
            timer.stop();
        }
    }
}
