package com.swak.test.autoconfigure;

import com.swak.common.exception.DistributedLockException;
import com.swak.core.sync.DistributedLock;
import com.swak.lock.annotation.Lockable;
import com.swak.lock.spring.configuration.SwakLockAutoConfiguration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@code @Lockable} 端到端链路：锁实现由 {@code LockableInterceptor#afterSingletonsInstantiated()}
 * 惰性解析，同时验证加锁/释放锁与 SpEL key 真的生效。
 */
class LockableAopTest {

    private static final String DEFAULT_KEY = LockedService.class.getName() + ".doWork";

    private final DistributedLock distributedLock = mock(DistributedLock.class);

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(SwakLockAutoConfiguration.class))
            .withBean(DistributedLock.class, () -> distributedLock)
            .withUserConfiguration(LockedServiceConfig.class);

    @Test
    @DisplayName("未配置 key 时以「类名.方法名」为锁 key，完成加锁/释放锁")
    void lockableMethodUsesDefaultKey() {
        when(distributedLock.acquireLock(anyString(), anyLong(), any(TimeUnit.class))).thenReturn(true);
        runner.run(context -> {
            assertThat(context).hasNotFailed();
            LockedService service = context.getBean(LockedService.class);
            assertThat(AopUtils.isAopProxy(service)).isTrue();

            assertThat(service.doWork("A")).isEqualTo("done-A");

            verify(distributedLock).acquireLock(eq(DEFAULT_KEY), anyLong(), any(TimeUnit.class));
            verify(distributedLock).releaseLock(DEFAULT_KEY);
        });
    }

    @Test
    @DisplayName("配置 key 为 SpEL 时按表达式求值，参数可通过 #a0 引用")
    void lockableMethodUsesSpelKey() {
        when(distributedLock.acquireLock(anyString(), anyLong(), any(TimeUnit.class))).thenReturn(true);
        runner.run(context -> {
            LockedService service = context.getBean(LockedService.class);
            assertThat(service.doWorkWithKey("order-1")).isEqualTo("keyed-order-1");
            verify(distributedLock).acquireLock(eq("order-1"), anyLong(), any(TimeUnit.class));
            verify(distributedLock).releaseLock("order-1");
        });
    }

    @Test
    @DisplayName("抢锁失败且未配置 fallback 时抛 DistributedLockException，且不释放锁")
    void lockNotAcquiredThrows() {
        when(distributedLock.acquireLock(anyString(), anyLong(), any(TimeUnit.class))).thenReturn(false);
        runner.run(context -> {
            LockedService service = context.getBean(LockedService.class);
            assertThatThrownBy(() -> service.doWork("B")).isInstanceOf(DistributedLockException.class);
            verify(distributedLock, never()).releaseLock(anyString());
        });
    }

    @Configuration(proxyBeanMethods = false)
    static class LockedServiceConfig {
        @Bean
        LockedService lockedService() {
            return new LockedService();
        }
    }

    static class LockedService {
        @Lockable
        public String doWork(String id) {
            return "done-" + id;
        }

        @Lockable(key = "#a0")
        public String doWorkWithKey(String id) {
            return "keyed-" + id;
        }
    }
}
