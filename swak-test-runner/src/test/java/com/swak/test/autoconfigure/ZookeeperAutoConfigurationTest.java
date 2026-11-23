package com.swak.test.autoconfigure;

import org.apache.curator.framework.CuratorFramework;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import com.swak.lock.spring.configuration.ZookeeperConfiguration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Zookeeper 连接是「有类 + 有 connectionStr 才连」，两者缺一都必须安静跳过，
 * 否则一个没配 zk 的应用在启动期就会卡在连接超时。
 */
class ZookeeperAutoConfigurationTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(ZookeeperConfiguration.class));

    @Test
    @DisplayName("未配置 swak.zookeeper.connectionStr 时不创建 CuratorFramework")
    void withoutConnectionString() {
        runner.run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context).doesNotHaveBean(CuratorFramework.class);
        });
    }

    @Test
    @DisplayName("类路径没有 Curator 时配置整体跳过")
    void withoutCuratorOnClasspath() {
        runner.withClassLoader(new FilteredClassLoader(CuratorFramework.class)).run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context).doesNotHaveBean(CuratorFramework.class);
        });
    }
}
