package com.swak.test.autoconfigure;

import com.swak.core.sync.DistributedLock;
import com.swak.lock.parser.LockOperationSource;
import com.swak.lock.spring.configuration.SwakLockAutoConfiguration;
import com.swak.lock.sync.LocalSyncLock;
import com.swak.lock.sync.RedissonLock;
import com.swak.lock.sync.ZookeeperLock;
import org.apache.curator.framework.CuratorFramework;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * 分布式锁的装配条件。
 * <p>
 * 回归点：{@code SwakLockAutoConfiguration} 上曾有类级 {@code @ConditionalOnBean(DistributedLock.class)}，
 * 但 {@code DistributedLock} 恰由它自己 {@code @Import} 的
 * {@code LocalOnlyLockConfiguration}/{@code RedissonLockConfiguration}/{@code ZookeeperLockConfiguration} 提供。
 * Spring 先评估类级条件、再处理 {@code @Import}，条件永不成立 ——
 * 结果是 {@code swak.lock.type} 三个取值全部失效，注解锁链路也不会装配。
 * 现将条件下沉到各 {@code @Bean} 方法，本测试锁定修复后的行为。
 */
class LockAutoConfigurationTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(SwakLockAutoConfiguration.class));

    @Test
    @DisplayName("未配置 swak.lock.type 时不提供任何锁实现")
    void noLockTypeMeansNothing() {
        runner.run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context).doesNotHaveBean(DistributedLock.class);
            assertThat(context).doesNotHaveBean(LockOperationSource.class);
        });
    }

    @Test
    @DisplayName("swak.lock.type=local_only 装配本地锁与注解锁 AOP 链路")
    void localOnlyTypeWiresLocalSyncLock() {
        runner.withPropertyValues("swak.lock.type=local_only").run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context).hasSingleBean(DistributedLock.class);
            assertThat(context.getBean(DistributedLock.class)).isInstanceOf(LocalSyncLock.class);
            assertThat(context).hasSingleBean(LockOperationSource.class);
            assertThat(context).hasBean("internalLockableAdvisor");
        });
    }

    @Test
    @DisplayName("swak.lock.type=redisson 装配 Redisson 锁")
    void redissonTypeWiresRedissonLock() {
        runner.withPropertyValues("swak.lock.type=redisson")
                .withBean(RedissonClient.class, () -> mock(RedissonClient.class))
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context.getBean(DistributedLock.class)).isInstanceOf(RedissonLock.class);
                    assertThat(context).hasBean("internalLockableAdvisor");
                });
    }

    @Test
    @DisplayName("swak.lock.type=zk 且存在 CuratorFramework 时装配 Zookeeper 锁")
    void zookeeperTypeWiresZookeeperLock() {
        runner.withPropertyValues("swak.lock.type=zk")
                .withBean(CuratorFramework.class, () -> mock(CuratorFramework.class))
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context.getBean(DistributedLock.class)).isInstanceOf(ZookeeperLock.class);
                    assertThat(context).hasBean("internalLockableAdvisor");
                });
    }

    @Test
    @DisplayName("swak.lock.type=zk 但缺少 CuratorFramework 时不装配锁")
    void zookeeperTypeWithoutCuratorDoesNothing() {
        runner.withPropertyValues("swak.lock.type=zk").run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context).doesNotHaveBean(DistributedLock.class);
            assertThat(context).doesNotHaveBean(LockOperationSource.class);
        });
    }

    @Test
    @DisplayName("使用方自行提供 DistributedLock 时，注解锁 AOP 链路正常装配")
    void userProvidedLockEnablesAop() {
        runner.withBean(DistributedLock.class, () -> mock(DistributedLock.class)).run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context).hasSingleBean(DistributedLock.class);
            assertThat(context).hasSingleBean(LockOperationSource.class);
            assertThat(context).hasBean("internalLockableAdvisor");
        });
    }
}
