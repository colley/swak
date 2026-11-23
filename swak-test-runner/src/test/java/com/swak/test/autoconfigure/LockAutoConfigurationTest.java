package com.swak.test.autoconfigure;

import com.swak.core.sync.DistributedLock;
import com.swak.lock.parser.LockOperationSource;
import com.swak.lock.spring.configuration.SwakLockAutoConfiguration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * 分布式锁的装配条件。
 * <p>
 * <b>已知缺陷（本测试即在固化该现状，修复后需同步修改）</b>：
 * {@code SwakLockAutoConfiguration} 类上直接标注 {@code @ConditionalOnBean(DistributedLock.class)}，
 * 而 {@code DistributedLock} 恰恰由它自己 {@code @Import} 进来的
 * {@code LocalOnlyLockConfiguration}/{@code RedissonLockConfiguration}/{@code ZookeeperLockConfiguration} 提供。
 * Spring 在解析配置类时会先评估类级条件、再处理 {@code @Import}，因此条件永远不成立 ——
 * 结果是 {@code swak.lock.type} 三个取值全部失效，注解锁链路也不会装配。
 * <p>
 * 当前唯一可用的方式是使用方自己声明 {@code DistributedLock} bean（见 userProvidedLockEnablesAop）。
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
    @DisplayName("已知缺陷：swak.lock.type=local_only 不会装配 DistributedLock")
    void localOnlyTypeIsIgnored() {
        runner.withPropertyValues("swak.lock.type=local_only").run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context).doesNotHaveBean(DistributedLock.class);
            assertThat(context).doesNotHaveBean(SwakLockAutoConfiguration.class);
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
