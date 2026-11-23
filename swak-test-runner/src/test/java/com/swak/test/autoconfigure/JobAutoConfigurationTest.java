package com.swak.test.autoconfigure;

import com.swak.easyjob.EasyJobConfig;
import com.swak.easyjob.EasyScheduledConfigurerFactory;
import com.swak.easyjob.spring.configuration.EasyJobAutoConfiguration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 定时任务注册器以业务方提供的 EasyJobConfig 为前提。
 * <p>
 * 正向路径没有放进这里：{@code EasyScheduledConfigurerFactory} 是
 * {@code SmartInitializingSingleton}，容器刷新末尾会建 lock 表、抢占 register_lock
 * 并启动调度器，必须有真实数据库与调度环境，属于集成测试范畴。
 * 这里只固化「缺配置即不装配」这一条，防止升级后 {@code @ConditionalOnBean} 判错导致空转。
 */
class JobAutoConfigurationTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(EasyJobAutoConfiguration.class));

    @Test
    @DisplayName("没有 EasyJobConfig 时不装配任务注册器")
    void withoutConfig() {
        runner.run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context).doesNotHaveBean(EasyScheduledConfigurerFactory.class);
            assertThat(context).doesNotHaveBean(EasyJobConfig.class);
        });
    }
}
